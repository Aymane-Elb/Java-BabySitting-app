package com.example.app.Services;

import com.example.app.Models.Database; // Assuming this connects to your DB
import com.example.app.Models.User;
import com.example.app.Models.hashPasswd; // Assuming this is your password hashing utility

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UserService {

    // You might want to pass DB connection details or use a singleton Database class
    // For now, assuming Database.connect() handles connection details internally.

    /**
     * Authenticates a user by email and password.
     * @param email The user's email.
     * @param password The user's plain-text password.
     * @return An Optional containing the User object if authentication is successful,
     * otherwise an empty Optional.
     */
    public Optional<User> authenticateUser(String email, String password) {
        String hashedPassword = hashPasswd.hashPassword(password);
        String sql = "SELECT id, username, email, user_type, phone_number, address, profile_picture, bio, created_at, last_login, is_active, location, rating, price FROM users WHERE email = ? AND password = ?";

        try (java.sql.Connection conn = Database.connect(); // Assuming Database.connect() provides a connection
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, hashedPassword);

            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error during authentication: " + e.getMessage());
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * Retrieves a user by their ID.
     * @param userId The ID of the user.
     * @return An Optional containing the User object if found, otherwise an empty Optional.
     */
    public Optional<User> getUserById(int userId) {
        // Assuming Database.getUserById returns a Map<String, Object>
        Map<String, Object> userData = Database.getUserById(userId);
        if (userData != null && !userData.isEmpty()) {
            return Optional.of(mapMapToUser(userData));
        }
        return Optional.empty();
    }

    /**
     * Retrieves a user by their username or email.
     * @param identifier The username or email.
     * @return An Optional containing the User object if found, otherwise an empty Optional.
     */
    public Optional<User> getUserByUsernameOrEmail(String identifier) {
        String sql = "SELECT id, username, email, user_type, phone_number, address, profile_picture, bio, created_at, last_login, is_active, location, rating, price FROM users WHERE username = ? OR email = ?";

        try (java.sql.Connection conn = Database.connect();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, identifier);
            stmt.setString(2, identifier);

            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error during user lookup by username/email: " + e.getMessage());
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * Retrieves a list of users by their user_type.
     * @param userType The type of user to retrieve (e.g., "babysitter", "client").
     * @return A list of User objects.
     */
    public List<User> getUsersByType(String userType) {
        List<User> users = new ArrayList<>();
        // Assuming Database.getUsersByType returns a List<Map<String, Object>>
        List<Map<String, Object>> userDataList = Database.getUsersByType(userType);
        for (Map<String, Object> userData : userDataList) {
            users.add(mapMapToUser(userData));
        }
        return users;
    }

    /**
     * Helper method to map a ResultSet row to a User object.
     */
    private User mapResultSetToUser(java.sql.ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String username = rs.getString("username");
        String email = rs.getString("email");
        String userType = rs.getString("user_type");
        String phoneNumber = rs.getString("phone_number");
        String address = rs.getString("address");
        String profilePicture = rs.getString("profile_picture");
        String bio = rs.getString("bio");
        Timestamp createdAtTs = rs.getTimestamp("created_at");
        LocalDateTime createdAt = (createdAtTs != null) ? createdAtTs.toLocalDateTime() : null;
        Timestamp lastLoginTs = rs.getTimestamp("last_login");
        LocalDateTime lastLogin = (lastLoginTs != null) ? lastLoginTs.toLocalDateTime() : null;
        boolean isActive = rs.getBoolean("is_active");

        String location = rs.getString("location");
        double rating = rs.getDouble("rating");
        double price = rs.getDouble("price");

        return new User(id, username, email, userType, phoneNumber, address,
                profilePicture, bio, createdAt, lastLogin, isActive,
                location, rating, price);
    }

    /**
     * Helper method to map a Map<String, Object> to a User object.
     * This is useful if Database methods return Maps.
     */
    private User mapMapToUser(Map<String, Object> userData) {
        int id = (int) userData.get("id");
        String username = (String) userData.get("username");
        String email = (String) userData.get("email");
        String userType = (String) userData.get("user_type");
        String phoneNumber = (String) userData.get("phone_number");
        String address = (String) userData.get("address");
        String profilePicture = (String) userData.get("profile_picture");
        String bio = (String) userData.get("bio");
        // Handle conversion from Timestamp to LocalDateTime for created_at and last_login
        LocalDateTime createdAt = (userData.get("created_at") instanceof Timestamp) ?
                ((Timestamp) userData.get("created_at")).toLocalDateTime() : null;
        LocalDateTime lastLogin = (userData.get("last_login") instanceof Timestamp) ?
                ((Timestamp) userData.get("last_login")).toLocalDateTime() : null;
        boolean isActive = (boolean) userData.get("is_active");

        String location = (String) userData.get("location");
        Double rating = (Double) userData.get("rating");
        Double price = (Double) userData.get("price");

        return new User(id, username, email, userType, phoneNumber, address,
                profilePicture, bio, createdAt, lastLogin, isActive,
                location, rating != null ? rating : 0.0, price != null ? price : 0.0);
    }
}