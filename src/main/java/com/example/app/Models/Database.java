package com.example.app.Models;

import java.sql.*;
import java.time.LocalDateTime; // Added for compatibility with Timestamp.toLocalDateTime()
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class responsible for interactions with the PostgreSQL database.
 * IMPORTANT: Hardcoding credentials is a security risk. For production, use environment variables or config files.
 */
public class Database {

    // It's better to load these from a configuration file or environment variables
    // For demonstration, they are kept here as provided.
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/babysitting";
    private static final String DB_USER = "Aymane";
    private static final String DB_PASSWORD = "RACHIDAx@436550"; // SECURITY RISK: Hardcoded password

    private static final Logger LOGGER = Logger.getLogger(Database.class.getName());

    private Database() {
        // Private constructor to prevent instantiation, as all methods are static
    }

    public static String getDbPassword() {
        return DB_PASSWORD;
    }
    public static String getDbUrl(){
        return DB_URL;
    }
    public static String getDbUser(){
        return DB_USER;
    }

    /**
     * Establishes a connection to the database.
     * @return A usable Connection object for queries.
     * @throws SQLException If a database access error occurs.
     */
    public static Connection connect() throws SQLException {
        try {
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "PostgreSQL connection failed: " + e.getMessage(), e);
            throw e; // Re-throw the SQLException to allow calling code to handle it
        }
    }

    /**
     * Inserts a new user into the database.
     * @param username Username
     * @param accountType Account type (e.g., "client", "babysitter", "admin") - Assumed to be "user_type" in DB
     * @param email Email address
     * @param phone Phone number
     * @param address Address
     * @param passwd Password (will be hashed before insertion)
     * @return The ID of the new user, or -1 if insertion fails without throwing an exception.
     * @throws SQLException In case of an SQL error.
     */
    public static int insertUser(String username, String accountType, String email, String phone, String address, String passwd) throws SQLException {
        // Assuming hashPasswd is a class that provides password hashing functionality
        // This class was not provided, but it's used in your code.
        String hashedPassword = hashPasswd.hashPassword(passwd);

        // Updated SQL to include new columns, they will be null for non-babysitters
        // The `?::user_type` cast is already correctly applied here for insertion.
        String sql = "INSERT INTO users(username, email, password, user_type, phone_number, address, is_active, created_at, location, rating, price) " +
                "VALUES (?, ?, ?, ?::user_type, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?, ?) RETURNING id";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, hashedPassword);
            stmt.setString(4, accountType); // This should map to your 'user_type' enum values
            stmt.setString(5, phone);
            stmt.setString(6, address);
            stmt.setBoolean(7, true); // Assuming new users are active by default

            // Set new fields based on accountType
            if ("babysitter".equalsIgnoreCase(accountType)) {
                // You might have a separate registration for babysitters to get these details
                // For now, setting defaults or nulls for initial registration
                stmt.setString(8, null); // location - can be updated later
                stmt.setDouble(9, 0.0);   // rating - will be updated later by reviews
                stmt.setDouble(10, 0.0);  // price - can be updated later
            } else {
                stmt.setString(8, null);
                stmt.setNull(9, Types.NUMERIC); // Or 0.0, depending on your DB column NULL-ability
                stmt.setNull(10, Types.NUMERIC); // Or 0.0
            }


            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1); // Return the generated ID
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "User insertion failed: " + e.getMessage(), e);
            throw e; // Re-throw the SQLException for the caller to handle
        }
        return -1; // Should ideally not be reached if insertion is successful or an exception is thrown
    }


    /**
     * Checks if a user exists with the specified email.
     * @param email Email to check.
     * @return true if the email already exists, false otherwise.
     */
    public static boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        // Catch specific exceptions if needed, otherwise general SQLException is fine
        catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Email check failed: " + e.getMessage(), e);
        }
        return false;
    }

    /**
     * Retrieves possible values for the user_type enum.
     * @return List of allowed values for user_type.
     */
    public static List<String> getUserTypes() {
        List<String> types = new ArrayList<>();
        // Assumes you have a custom enum type named 'user_type' in your PostgreSQL database
        String sql = "SELECT unnest(enum_range(NULL::user_type))::text AS user_type";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                types.add(rs.getString("user_type"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to get user types: " + e.getMessage(), e);
        }
        return types;
    }

    /**
     * Retrieves a user by their ID.
     * @param userId User ID.
     * @return Map containing user information. Returns an empty map if not found.
     * NOTE: It's better to return a User object directly, or an Optional<User>
     */
    public static Map<String, Object> getUserById(int userId) {
        // Updated SQL to include new columns
        String sql = "SELECT id, username, user_type, email, phone_number, address, profile_picture, bio, created_at, last_login, is_active, location, rating, price " +
                "FROM users WHERE id = ?";
        Map<String, Object> user = new HashMap<>();

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    user.put("id", rs.getInt("id"));
                    user.put("username", rs.getString("username"));
                    user.put("user_type", rs.getString("user_type"));
                    user.put("email", rs.getString("email"));
                    user.put("phone_number", rs.getString("phone_number"));
                    user.put("address", rs.getString("address"));
                    user.put("profile_picture", rs.getString("profile_picture")); // Can be null
                    user.put("bio", rs.getString("bio")); // Can be null
                    user.put("created_at", rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
                    user.put("last_login", rs.getTimestamp("last_login") != null ? rs.getTimestamp("last_login").toLocalDateTime() : null); // Can be null
                    user.put("is_active", rs.getBoolean("is_active"));
                    user.put("location", rs.getString("location")); // New
                    user.put("rating", rs.getDouble("rating"));     // New
                    user.put("price", rs.getDouble("price"));       // New
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "User retrieval by ID failed: " + e.getMessage(), e);
        }
        return user;
    }

    /**
     * Retrieves users by their user_type.
     * @param userType The type of user to retrieve (e.g., "babysitter", "client").
     * @return A list of Maps, where each Map represents a user.
     * NOTE: It's better to return a List<User> directly from UserService.
     */
    public static List<Map<String, Object>> getUsersByType(String userType) {
        // Updated SQL to include new columns
        // FIX APPLIED HERE: Added explicit cast to 'user_type' ENUM
        String sql = "SELECT id, username, email, user_type, phone_number, address, profile_picture, bio, created_at, last_login, is_active, location, rating, price FROM users WHERE user_type = ?::user_type";
        List<Map<String, Object>> users = new ArrayList<>();

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userType);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> user = new HashMap<>();
                    user.put("id", rs.getInt("id"));
                    user.put("username", rs.getString("username"));
                    user.put("email", rs.getString("email"));
                    user.put("user_type", rs.getString("user_type"));
                    user.put("phone_number", rs.getString("phone_number"));
                    user.put("address", rs.getString("address"));
                    user.put("profile_picture", rs.getString("profile_picture"));
                    user.put("bio", rs.getString("bio"));
                    // Convert Timestamp to LocalDateTime for consistency
                    user.put("created_at", rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
                    user.put("last_login", rs.getTimestamp("last_login") != null ? rs.getTimestamp("last_login").toLocalDateTime() : null);
                    user.put("is_active", rs.getBoolean("is_active"));
                    user.put("location", rs.getString("location")); // New
                    user.put("rating", rs.getDouble("rating"));     // New
                    user.put("price", rs.getDouble("price"));       // New
                    users.add(user);
                }
            }
        } catch (SQLException e) {
            // Changed Level.WARNING to Level.SEVERE because this is a critical error preventing data retrieval.
            LOGGER.log(Level.SEVERE, "Failed to get users by type: " + e.getMessage(), e);
        }
        return users;
    }


    /**
     * Updates user information.
     * @param userId User ID.
     * @param username New username (can be null/empty if not updating)
     * @param email New email (can be null/empty if not updating)
     * @param phone New phone (can be null/empty if not updating)
     * @param address New address (can be null/empty if not updating)
     * @param bio New bio (can be null if not updating)
     * @param profilePicture New profile picture (can be null if not updating)
     * @param location New location (can be null if not updating)
     * @param rating New rating (use negative value to indicate not updating)
     * @param price New price (use negative value to indicate not updating)
     * @return true if the update was successful, false otherwise.
     */
    public static boolean updateUser(int userId, String username, String email, String phone, String address, String bio, String profilePicture, String location, double rating, double price) {
        // Build the SQL dynamically to only update provided fields
        StringBuilder sqlBuilder = new StringBuilder("UPDATE users SET ");
        List<Object> params = new ArrayList<>();
        boolean first = true;

        if (username != null) {
            if (!first) sqlBuilder.append(", ");
            sqlBuilder.append("username = ?");
            params.add(username);
            first = false;
        }
        if (email != null) {
            if (!first) sqlBuilder.append(", ");
            sqlBuilder.append("email = ?");
            params.add(email);
            first = false;
        }
        if (phone != null) {
            if (!first) sqlBuilder.append(", ");
            sqlBuilder.append("phone_number = ?");
            params.add(phone);
            first = false;
        }
        if (address != null) {
            if (!first) sqlBuilder.append(", ");
            sqlBuilder.append("address = ?");
            params.add(address);
            first = false;
        }
        if (bio != null) {
            if (!first) sqlBuilder.append(", ");
            sqlBuilder.append("bio = ?");
            params.add(bio);
            first = false;
        }
        if (profilePicture != null) {
            if (!first) sqlBuilder.append(", ");
            sqlBuilder.append("profile_picture = ?");
            params.add(profilePicture);
            first = false;
        }
        if (location != null) {
            if (!first) sqlBuilder.append(", ");
            sqlBuilder.append("location = ?");
            params.add(location);
            first = false;
        }
        if (rating >= 0) { // Assuming 0 or positive is a valid rating, use -1 to skip
            if (!first) sqlBuilder.append(", ");
            sqlBuilder.append("rating = ?");
            params.add(rating);
            first = false;
        }
        if (price >= 0) { // Assuming 0 or positive is a valid price, use -1 to skip
            if (!first) sqlBuilder.append(", ");
            sqlBuilder.append("price = ?");
            params.add(price);
            first = false;
        }

        if (first) { // No fields to update
            LOGGER.log(Level.INFO, "No fields provided for user update for ID: " + userId);
            return false;
        }

        sqlBuilder.append(" WHERE id = ?");
        params.add(userId); // Add user ID as the last parameter

        String sql = sqlBuilder.toString();

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "User update failed: " + e.getMessage(), e);
            return false;
        }
    }
}