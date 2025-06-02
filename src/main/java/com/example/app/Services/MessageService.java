package com.example.app.Services;

import com.example.app.Models.Message;
import com.example.app.Models.User;
import java.sql.*;
import java.time.LocalDateTime; // Added for compatibility with Timestamp.toLocalDateTime()
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageService {
    private String jdbcUrl;
    private String dbUser;
    private String dbPassword;
    private UserService userService; // Added UserService to resolve user names

    // Constructor with database credentials
    public MessageService(String jdbcUrl, String dbUser, String dbPassword) {
        this.jdbcUrl = jdbcUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        this.userService = new UserService(); // Initialize UserService
    }

    // Default constructor (used in EmailController)
    public MessageService() {
        this.jdbcUrl = "jdbc:postgresql://localhost:5432/babysitting";
        this.dbUser = "Aymane";
        this.dbPassword = "RACHIDAx@436550";
        this.userService = new UserService(); // Initialize UserService
    }

    /**
     * Saves a new message to the database. This method is now effectively `createMessage`.
     * @param message The Message object to save. Its ID will be updated upon successful insertion.
     * @return true if the message was saved successfully, false otherwise.
     */
    public boolean createMessage(Message message) {
        String sql = "INSERT INTO messages (sender_id, receiver_id, sender_type, receiver_type, subject, content, timestamp, is_read, is_archived, is_starred) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
        try (Connection conn = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, message.getSenderId());
            pstmt.setInt(2, message.getReceiverId());
            pstmt.setString(3, message.getSenderType());
            pstmt.setString(4, message.getReceiverType());
            pstmt.setString(5, message.getSubject());
            pstmt.setString(6, message.getContent());
            pstmt.setTimestamp(7, Timestamp.valueOf(message.getTimestamp()));
            pstmt.setBoolean(8, message.isRead());
            pstmt.setBoolean(9, message.isArchived());
            pstmt.setBoolean(10, message.isStarred());

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                message.setId(rs.getInt(1)); // Set the generated ID back to the message object
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error creating message: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Retrieves all messages (sent and received) for a given user.
     * @param userId The ID of the user.
     * @return A list of Message objects.
     */
    public List<Message> getAllMessagesForUser(int userId) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT id, sender_id, receiver_id, sender_type, receiver_type, subject, content, timestamp, is_read, is_archived, is_starred " +
                "FROM messages WHERE (sender_id = ? OR receiver_id = ?) AND is_archived = FALSE ORDER BY timestamp DESC"; // Exclude archived from 'All Messages'

        try (Connection conn = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                messages.add(mapResultSetToMessage(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving all messages for user: " + e.getMessage());
            e.printStackTrace();
        }
        return messages;
    }

    /**
     * Retrieves inbox messages (where the current user is the receiver) for a given user.
     * @param userId The ID of the user.
     * @return A list of Message objects in the inbox.
     */
    public List<Message> getInboxMessagesForUser(int userId) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT id, sender_id, receiver_id, sender_type, receiver_type, subject, content, timestamp, is_read, is_archived, is_starred " +
                "FROM messages WHERE receiver_id = ? AND is_archived = FALSE ORDER BY timestamp DESC";

        try (Connection conn = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                messages.add(mapResultSetToMessage(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving inbox messages for user: " + e.getMessage());
            e.printStackTrace();
        }
        return messages;
    }

    /**
     * Retrieves starred messages for a given user (either sender or receiver).
     *
     * @param userId The ID of the user.
     * @return A list of starred Message objects.
     */
    public List<Message> getStarredMessagesForUser(int userId) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT id, sender_id, receiver_id, sender_type, receiver_type, subject, content, timestamp, is_read, is_archived, is_starred " +
                "FROM messages WHERE (sender_id = ? OR receiver_id = ?) AND is_starred = TRUE AND is_archived = FALSE ORDER BY timestamp DESC"; // Exclude archived

        try (Connection conn = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                messages.add(mapResultSetToMessage(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving starred messages for user: " + e.getMessage());
            e.printStackTrace();
        }
        return messages;
    }

    /**
     * Retrieves archived messages for a given user (either sender or receiver).
     *
     * @param userId The ID of the user.
     * @return A list of archived Message objects.
     */
    public List<Message> getArchivedMessagesForUser(int userId) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT id, sender_id, receiver_id, sender_type, receiver_type, subject, content, timestamp, is_read, is_archived, is_starred " +
                "FROM messages WHERE (sender_id = ? OR receiver_id = ?) AND is_archived = TRUE ORDER BY timestamp DESC";

        try (Connection conn = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                messages.add(mapResultSetToMessage(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving archived messages for user: " + e.getMessage());
            e.printStackTrace();
        }
        return messages;
    }

    /**
     * Updates the read status of a message.
     * @param messageId The ID of the message to update.
     * @param isRead The new read status.
     * @return true if the status was updated, false otherwise.
     */
    public boolean updateMessageReadStatus(int messageId, boolean isRead) {
        String sql = "UPDATE messages SET is_read = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, isRead);
            pstmt.setInt(2, messageId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating message read status: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Updates the starred status of a message.
     * @param messageId The ID of the message to update.
     * @param isStarred The new starred status.
     * @return true if the status was updated, false otherwise.
     */
    public boolean updateMessageStarredStatus(int messageId, boolean isStarred) {
        String sql = "UPDATE messages SET is_starred = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, isStarred);
            pstmt.setInt(2, messageId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating message starred status: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Updates the archived status of a message.
     * @param messageId The ID of the message to update.
     * @param isArchived The new archived status.
     * @return true if the status was updated, false otherwise.
     */
    public boolean updateMessageArchivedStatus(int messageId, boolean isArchived) {
        String sql = "UPDATE messages SET is_archived = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, isArchived);
            pstmt.setInt(2, messageId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating message archived status: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Deletes a message from the database.
     * @param messageId The ID of the message to delete.
     * @return true if the message was deleted, false otherwise.
     */
    public boolean deleteMessage(int messageId) {
        String sql = "DELETE FROM messages WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, messageId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting message: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Retrieves messages sent by a specific user to a receiver of a given type.
     *
     * @param senderId The ID of the sending user.
     * @param receiverType The type of the receiver (e.g., "client", "babysitter", "admin").
     * @return A list of Message objects.
     */
    public List<Message> getSentMessagesToReceiverType(int senderId, String receiverType) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT id, sender_id, receiver_id, sender_type, receiver_type, subject, content, timestamp, is_read, is_archived, is_starred " +
                "FROM messages WHERE sender_id = ? AND receiver_type = ? ORDER BY timestamp DESC";

        try (Connection conn = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, senderId);
            pstmt.setString(2, receiverType);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                messages.add(mapResultSetToMessage(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving sent messages by receiver type: " + e.getMessage());
            e.printStackTrace();
        }
        return messages;
    }

    /**
     * Helper method to map a ResultSet row to a Message object.
     */
    private Message mapResultSetToMessage(ResultSet rs) throws SQLException {
        return new Message(
                rs.getInt("id"),
                rs.getInt("sender_id"),
                rs.getInt("receiver_id"),
                rs.getString("sender_type"),
                rs.getString("receiver_type"),
                rs.getString("subject"),
                rs.getString("content"),
                rs.getTimestamp("timestamp").toLocalDateTime(),
                rs.getBoolean("is_read"),
                rs.getBoolean("is_archived"),
                rs.getBoolean("is_starred")
        );
    }

    /**
     * Retrieves the full name of a user given their ID and type.
     * This relies on the UserService to fetch user details.
     *
     * @param userId The ID of the user.
     * @param userType The type of the user (e.g., "client", "babysitter").
     * @return The user's full name, or a default string if not found.
     */
    public String getUserFullName(int userId, String userType) {
        Optional<User> userOptional = userService.getUserById(userId);
        return userOptional.map(User::getName).orElse("Unknown User (ID: " + userId + ")");
    }

    /**
     * Marks a message as unread.
     * @param messageId The ID of the message to mark as unread.
     * @return true if the status was updated, false otherwise.
     */
    public boolean markMessageAsUnread(int messageId) {
        return updateMessageReadStatus(messageId, false);
    }

    /**
     * Marks a message as read.
     * @param messageId The ID of the message to mark as read.
     * @return true if the status was updated, false otherwise.
     */
    public boolean markMessageAsRead(int messageId) {
        return updateMessageReadStatus(messageId, true);
    }

    /**
     * Placeholder for stopping message polling.
     * Implement actual polling termination logic here if applicable.
     */
    public void stopMessagePolling() {
        // TODO: Implement logic to stop any background threads or timers related to message polling.
        System.out.println("stopMessagePolling called. No polling mechanism implemented yet.");
    }
}