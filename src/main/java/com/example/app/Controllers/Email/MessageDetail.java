package com.example.app.Controllers.Email;

import com.example.app.Controllers.Contact.Contact; // For opening reply/forward window
import com.example.app.Models.Message;
import com.example.app.Models.User;
import com.example.app.Services.MessageService;
import com.example.app.Services.SessionManager; // For getting current user for new messages
import com.example.app.Services.UserService; // To get sender/receiver names

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

public class MessageDetail implements Initializable { // Renamed from MessageDetailController for brevity

    @FXML private Label fromLabel;
    @FXML private Label toLabel;
    @FXML private Label subjectLabel;
    @FXML private Label dateLabel;
    @FXML private TextArea contentTextArea;
    @FXML private Button backButton;
    @FXML private Button replyButton;
    @FXML private Button forwardButton;
    @FXML private Button deleteButton;
    @FXML private Button starButton;
    @FXML private Button archiveButton; // Added archive button

    private Message message;
    private User currentUser;
    private MessageService messageService;
    private UserService userService; // To resolve user names
    private MessageDetailCallback callback; // Callback to EmailController

    // Interface for callback to the main EmailController
    public interface MessageDetailCallback {
        void onBackToList();
        void onMessageDeleted(int messageId);
        void onMessageSent(); // For replies/forwards
    }

    public void setCallback(MessageDetailCallback callback) {
        this.callback = callback;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        messageService = new MessageService(); // Initialize with default credentials
        userService = new UserService(); // Initialize UserService
        setupButtonActions();
    }

    /**
     * Sets the message to be displayed and updates the UI.
     * This method is called from EmailController.
     * @param message The Message object to display.
     */
    public void setMessage(Message message) {
        this.message = message;
        updateUI();
    }

    /**
     * Sets the current logged-in user.
     * This method is called from EmailController.
     * @param currentUser The current logged-in User object.
     */
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    private void setupButtonActions() {
        backButton.setOnAction(event -> {
            if (callback != null) callback.onBackToList();
        });
        replyButton.setOnAction(this::handleReply);
        forwardButton.setOnAction(this::handleForward);
        deleteButton.setOnAction(this::handleDelete);
        starButton.setOnAction(this::handleStarToggle);
        archiveButton.setOnAction(this::handleArchiveToggle); // Action for archive button
    }

    private void updateUI() {
        if (message != null && currentUser != null) {
            String fromName = getUserDisplayName(message.getSenderId());
            String toName = getUserDisplayName(message.getReceiverId());

            fromLabel.setText("From: " + fromName);
            toLabel.setText("To: " + toName);
            subjectLabel.setText("Subject: " + message.getSubject());
            dateLabel.setText("Date: " + message.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            contentTextArea.setText(message.getContent());

            // Update star/archive button text/icon based on current state
            updateStarButtonAppearance();
            updateArchiveButtonAppearance();

            // Mark as read if current user is receiver and message is unread
            if (message.getReceiverId() == currentUser.getId() && !message.isRead()) {
                if (messageService.updateMessageReadStatus(message.getId(), true)) {
                    message.setRead(true); // Update local model
                    // No need to refresh ListView here, EmailController's callback handles it
                }
            }
        } else {
            // Clear UI if message or currentUser is null
            fromLabel.setText("From:");
            toLabel.setText("To:");
            subjectLabel.setText("Subject:");
            dateLabel.setText("Date:");
            contentTextArea.setText("");
        }
    }

    /**
     * Gets a user's name based on their ID using UserService.
     * @param userId The ID of the user.
     * @return The user's name or "Unknown User".
     */
    private String getUserDisplayName(int userId) {
        Optional<User> userOptional = userService.getUserById(userId);
        return userOptional.map(User::getName).orElse("Unknown User (ID: " + userId + ")");
    }

    private void handleReply(ActionEvent event) {
        if (message != null && currentUser != null) {
            openComposeWindowForReply(message, currentUser); // Call the specific reply window opener
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Cannot reply: message or current user is null.");
        }
    }

    private void handleForward(ActionEvent event) {
        if (message != null && currentUser != null) {
            openComposeWindowForForward(message, currentUser); // Call the specific forward window opener
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Cannot forward: message or current user is null.");
        }
    }

    // New method for opening compose window specifically for replies
    private void openComposeWindowForReply(Message originalMessage, User currentUser) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Messages/writeToSomeone.fxml"));
            Parent root = loader.load();
            Contact contactController = loader.getController();

            // Call the prepareReply method in Contact controller
            contactController.prepareReply(originalMessage, currentUser);

            Stage stage = new Stage();
            stage.setTitle("Reply Message");
            stage.setScene(new Scene(root));
            stage.show();

            // Set a callback for the compose controller
            contactController.setComposeCallback(() -> {
                stage.close(); // Close compose window
                if (callback != null) callback.onMessageSent(); // Notify main EmailController
            });

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open reply window: " + e.getMessage());
        }
    }

    // New method for opening compose window specifically for forwards
    private void openComposeWindowForForward(Message originalMessage, User currentUser) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Messages/writeToSomeone.fxml"));
            Parent root = loader.load();
            Contact contactController = loader.getController();

            // Call the prepareForward method in Contact controller
            contactController.prepareForward(originalMessage, currentUser);

            Stage stage = new Stage();
            stage.setTitle("Forward Message");
            stage.setScene(new Scene(root));
            stage.show();

            // Set a callback for the compose controller
            contactController.setComposeCallback(() -> {
                stage.close(); // Close compose window
                if (callback != null) callback.onMessageSent(); // Notify main EmailController
            });

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open forward window: " + e.getMessage());
        }
    }

    private void handleDelete(ActionEvent event) {
        if (message != null) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this message?", ButtonType.YES, ButtonType.NO);
            confirmAlert.setTitle("Confirm Deletion");
            confirmAlert.setHeaderText("Delete Message");
            Optional<ButtonType> result = confirmAlert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.YES) {
                if (messageService.deleteMessage(message.getId())) {
                    if (callback != null) callback.onMessageDeleted(message.getId());
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete message.");
                }
            }
        }
    }

    private void handleStarToggle(ActionEvent event) {
        if (message != null) {
            boolean newStarredStatus = !message.isStarred();
            if (messageService.updateMessageStarredStatus(message.getId(), newStarredStatus)) {
                message.setStarred(newStarredStatus); // Update local model
                updateStarButtonAppearance(); // Update button UI
                showAlert(Alert.AlertType.INFORMATION, "Success", "Message " + (newStarredStatus ? "starred" : "unstarred") + ".");
                // Potentially, you might want to refresh the main list view here if it's currently showing favorites.
                // This would be handled by the EmailController's callback, but only if the action directly affects the list content.
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update starred status.");
            }
        }
    }

    private void handleArchiveToggle(ActionEvent event) {
        if (message != null) {
            boolean newArchivedStatus = !message.isArchived();
            if (messageService.updateMessageArchivedStatus(message.getId(), newArchivedStatus)) {
                message.setArchived(newArchivedStatus); // Update local model
                updateArchiveButtonAppearance(); // Update button UI
                showAlert(Alert.AlertType.INFORMATION, "Success", "Message " + (newArchivedStatus ? "archived" : "unarchived") + ".");
                // If archiving means it should disappear from inbox, you'd need to refresh the main list view
                // This would be handled by EmailController's callback, e.g., onBackToList() which reloads the list.
                if (callback != null) callback.onBackToList(); // Return to list and refresh it
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update archived status.");
            }
        }
    }

    private void updateStarButtonAppearance() {
        if (message != null) {
            starButton.setText(message.isStarred() ? "Unstar" : "Star");
            // You can also change graphics here, e.g., starButton.setGraphic(new ImageView(...));
        }
    }

    private void updateArchiveButtonAppearance() {
        if (message != null) {
            archiveButton.setText(message.isArchived() ? "Unarchive" : "Archive");
            // You can also change graphics here
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}