package com.example.app.Models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import com.example.app.Services.SessionManager; // Required for createForwardTemplate if using current user

/**
 * Represents a message exchanged between users in the babysitting application.
 * Messages can be sent between clients and babysitters.
 */
public class Message {
    private int id;
    private int senderId;
    private int receiverId;
    private String senderType;    // "client" or "babysitter"
    private String receiverType;  // "client" or "babysitter"
    private String subject;
    private String content;
    private LocalDateTime timestamp;
    private boolean read;
    private boolean archived;     // Flag for archiving messages
    private boolean starred;      // Flag for favoriting/starring messages

    /**
     * Full constructor for creating a Message object.
     *
     * @param id The unique ID of the message.
     * @param senderId The ID of the user who sent the message.
     * @param receiverId The ID of the user who received the message.
     * @param senderType The type of the sender (e.g., "client", "babysitter").
     * @param receiverType The type of the receiver (e.g., "client", "babysitter").
     * @param subject The subject line of the message.
     * @param content The main body of the message.
     * @param timestamp The date and time the message was sent.
     * @param read True if the message has been read by the receiver, false otherwise.
     * @param archived True if the message is archived, false otherwise.
     * @param starred True if the message is starred, false otherwise.
     */
    public Message(int id, int senderId, int receiverId, String senderType, String receiverType,
                   String subject, String content, LocalDateTime timestamp, boolean read,
                   boolean archived, boolean starred) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.senderType = senderType;
        this.receiverType = receiverType;
        this.subject = subject;
        this.content = content;
        this.timestamp = timestamp;
        this.read = read;
        this.archived = archived;
        this.starred = starred;
    }

    /**
     * Constructor for creating a new message when the initial status (archived, starred) is false.
     *
     * @param id The unique ID of the message.
     * @param senderId The ID of the user who sent the message.
     * @param receiverId The ID of the user who received the message.
     * @param senderType The type of the sender.
     * @param receiverType The type of the receiver.
     * @param subject The subject line of the message.
     * @param content The main body of the message.
     * @param timestamp The date and time the message was sent.
     * @param read True if the message has been read, false otherwise.
     */
    public Message(int id, int senderId, int receiverId, String senderType, String receiverType,
                   String subject, String content, LocalDateTime timestamp, boolean read) {
        this(id, senderId, receiverId, senderType, receiverType, subject, content, timestamp, read, false, false);
    }

    /**
     * Constructor for creating a brand new message, typically from UI input,
     * where the ID is not yet known (set to 0 for database auto-generation),
     * and the timestamp is set to the current time. Read, archived, and starred statuses
     * are initialized to false.
     *
     * @param senderId The ID of the user sending the message.
     * @param receiverId The ID of the user receiving the message.
     * @param senderType The type of the sender.
     * @param receiverType The type of the receiver.
     * @param subject The subject line of the message.
     * @param content The main body of the message.
     */
    public Message(int senderId, int receiverId, String senderType, String receiverType,
                   String subject, String content) {
        this(0, senderId, receiverId, senderType, receiverType, subject, content, LocalDateTime.now(), false, false, false);
    }

    // Getters and setters (standard boilerplate)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getSenderId() { return senderId; }
    public void setSenderId(int senderId) { this.senderId = senderId; }

    public int getReceiverId() { return receiverId; }
    public void setReceiverId(int receiverId) { this.receiverId = receiverId; }

    public String getSenderType() { return senderType; }
    public void setSenderType(String senderType) { this.senderType = senderType; }

    public String getReceiverType() { return receiverType; }
    public void setReceiverType(String receiverType) { this.receiverType = receiverType; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public LocalDateTime getSentDate() { return timestamp; }
    public void setSentDate(LocalDateTime sentDate) { this.timestamp = sentDate; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public boolean isArchived() { return archived; }
    public void setArchived(boolean archived) { this.archived = archived; }

    public boolean isStarred() { return starred; }
    public void setStarred(boolean starred) { this.starred = starred; }

    /**
     * Marks the message as read.
     *
     * @return The updated Message object (for method chaining).
     */
    public Message markAsRead() {
        this.read = true;
        return this;
    }

    /**
     * Marks the message as unread.
     *
     * @return The updated Message object (for method chaining).
     */
    public Message markAsUnread() {
        this.read = false;
        return this;
    }

    /**
     * Toggles the starred status of the message.
     *
     * @return The updated Message object (for method chaining).
     */
    public Message toggleStar() {
        this.starred = !this.starred;
        return this;
    }

    /**
     * Sets the archived status of the message.
     *
     * @param archive True to archive, false to unarchive.
     * @return The updated Message object (for method chaining).
     */
    public Message setArchiveStatus(boolean archive) {
        this.archived = archive;
        return this;
    }

    /**
     * Creates a new Message object pre-filled as a reply to the current message.
     * The sender and receiver are swapped, the subject is prefixed with "RE:",
     * and the content is empty, awaiting user input. The ID is 0 for a new message,
     * and timestamp is set to now.
     * This method assumes the reply is being sent by the *receiver* of the original message.
     *
     * @return A new Message object ready to be sent as a reply.
     */
    public Message createReplyTemplate() {
        String replySubject = subject.startsWith("RE: ") ? subject : "RE: " + subject;
        return new Message(
                0, this.receiverId, this.senderId, this.receiverType, this.senderType,
                replySubject, "", LocalDateTime.now(), false, false, false
        );
    }

    /**
     * Creates a new Message object pre-filled as a forward of the current message.
     * The subject is prefixed with "FW: ", and the receiver ID/type are initially unset (0, empty string),
     * requiring the user to choose a recipient. The content will typically be the original content.
     *
     * IMPORTANT: This method assumes the *current logged-in user* is the one forwarding the message.
     * It uses SessionManager to get the current user's ID and type for the sender fields.
     * Ensure SessionManager is properly initialized and holds the current user.
     *
     * @return A new Message object ready to be sent as a forward.
     */
    public Message createForwardTemplate() {
        User currentUser = SessionManager.getCurrentUser();
        int currentUserId = (currentUser != null) ? currentUser.getId() : 0;
        String currentUserType = (currentUser != null) ? currentUser.getUserType() : "";

        String fwdSubject = subject.startsWith("FW: ") ? subject : "FW: " + subject;

        // Basic placeholder for display name in forward header.
        // In a real app, you'd likely use a UserService here.
        String originalSenderDisplayName = this.senderType + " (ID:" + this.senderId + ")";
        String originalReceiverDisplayName = this.receiverType + " (ID:" + this.receiverId + ")";

        return new Message(
                0, currentUserId, 0, currentUserType, "", // Sender is current user, receiver is unset
                fwdSubject, "\n\n----- Original Message -----\nFrom: " + originalSenderDisplayName +
                "\nTo: " + originalReceiverDisplayName +
                "\nSubject: " + this.subject +
                "\nDate: " + this.timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) +
                "\n\n" + this.content, // Include original content with header
                LocalDateTime.now(), false, false, false
        );
    }

    /**
     * Overrides the `equals` method to compare Message objects based on their unique ID.
     *
     * @param o The object to compare with.
     * @return True if the objects are equal (have the same ID), false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return id == message.id;
    }

    /**
     * Overrides the `hashCode` method, consistent with `equals`, to return a hash based on the message ID.
     *
     * @return The hash code for the message.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Returns a string representation of the Message object, useful for debugging and logging.
     *
     * @return A string containing key details of the message.
     */
    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", subject='" + subject + '\'' +
                ", timestamp=" + timestamp +
                ", read=" + read +
                '}';
    }
}