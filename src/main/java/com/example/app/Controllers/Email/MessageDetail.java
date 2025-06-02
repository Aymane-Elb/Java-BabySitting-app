package com.example.app.Controllers.Email;

import com.example.app.Controllers.Contact.Contact;
import com.example.app.Models.Message;
import com.example.app.Models.User;
import com.example.app.Services.MessageService;
import com.example.app.Services.UserService;

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
import java.util.Optional;
import java.util.ResourceBundle;

public class MessageDetail implements Initializable {

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
    @FXML private Button archiveButton;

    private Message message;
    private User currentUser;
    private MessageService messageService;
    private UserService userService;
    private MessageDetailCallback callback;

    /**
     * Interface for callback to the main EmailController.
     */
    public interface MessageDetailCallback {
        void onBackToList();
        void onMessageDeleted(int messageId);
        void onMessageSent();
        void onMessageArchived(int messageId, boolean isArchived);
    }

    /**
     * Sets the callback interface for communication with the parent controller.
     * @param callback The implementation of MessageDetailCallback.
     */
    public void setCallback(MessageDetailCallback callback) {
        this.callback = callback;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        messageService = new MessageService();
        userService = new UserService();
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

    /**
     * Sets up action listeners for all UI buttons.
     */
    private void setupButtonActions() {
        if (backButton != null) {
            backButton.setOnAction(event -> {
                if (callback != null) callback.onBackToList();
            });
        }

        if (replyButton != null) {
            replyButton.setOnAction(this::handleReply);
        }

        if (forwardButton != null) {
            forwardButton.setOnAction(this::handleForward);
        }

        if (deleteButton != null) {
            deleteButton.setOnAction(this::handleDelete);
        }

        if (starButton != null) {
            starButton.setOnAction(this::handleStarToggle);
        }

        if (archiveButton != null) {
            archiveButton.setOnAction(this::handleArchiveToggle);
        }
    }

    /**
     * Updates the UI elements with the details of the currently set message.
     * Also handles marking the message as read if applicable.
     */
    private void updateUI() {
        if (message != null && currentUser != null) {
            String fromName = getUserDisplayName(message.getSenderId());
            String toName = getUserDisplayName(message.getReceiverId());

            if (fromLabel != null) fromLabel.setText("From: " + fromName);
            if (toLabel != null) toLabel.setText("To: " + toName);
            if (subjectLabel != null) subjectLabel.setText(message.getSubject());
            if (dateLabel != null) dateLabel.setText("Date: " + message.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            if (contentTextArea != null) {
                contentTextArea.setText(message.getContent());
                contentTextArea.setEditable(false); // Make sure it's read-only
            }

            updateStarButtonAppearance();
            updateArchiveButtonAppearance();

            // Mark as read if current user is receiver and message is unread
            if (message.getReceiverId() == currentUser.getId() && !message.isRead()) {
                if (messageService.updateMessageReadStatus(message.getId(), true)) {
                    message.setRead(true);
                }
            }
        } else {
            // Clear UI if message or currentUser is null
            if (fromLabel != null) fromLabel.setText("From:");
            if (toLabel != null) toLabel.setText("To:");
            if (subjectLabel != null) subjectLabel.setText("Subject:");
            if (dateLabel != null) dateLabel.setText("Date:");
            if (contentTextArea != null) contentTextArea.setText("");
        }
    }

    /**
     * Retrieves a user's display name based on their ID using UserService.
     * @param userId The ID of the user.
     * @return The user's name or "Unknown User (ID: [id])" if not found.
     */
    private String getUserDisplayName(int userId) {
        try {
            Optional<User> userOptional = userService.getUserById(userId);
            return userOptional.map(User::getName).orElse("Unknown User (ID: " + userId + ")");
        } catch (Exception e) {
            System.err.println("Error getting user display name for ID " + userId + ": " + e.getMessage());
            return "Unknown User (ID: " + userId + ")";
        }
    }

    /**
     * Handles the action for the reply button. Opens a compose window pre-filled for a reply.
     * @param event The ActionEvent that triggered this method.
     */
    private void handleReply(ActionEvent event) {
        if (message != null && currentUser != null) {
            openComposeWindow("/Fxml/Messages/writeToSomeone.fxml", "Reply Message",
                    loader -> {
                        try {
                            Contact controller = (Contact) loader.getController();
                            controller.prepareReply(message, currentUser);
                        } catch (Exception e) {
                            System.err.println("Error preparing reply: " + e.getMessage());
                        }
                    });
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Cannot reply: message or current user is null.");
        }
    }

    /**
     * Handles the action for the forward button. Opens a compose window pre-filled for a forward.
     * @param event The ActionEvent that triggered this method.
     */
    private void handleForward(ActionEvent event) {
        if (message != null && currentUser != null) {
            openComposeWindow("/Fxml/Messages/writeToSomeone.fxml", "Forward Message",
                    loader -> {
                        try {
                            Contact controller = (Contact) loader.getController();
                            controller.prepareForward(message, currentUser);
                        } catch (Exception e) {
                            System.err.println("Error preparing forward: " + e.getMessage());
                        }
                    });
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Cannot forward: message or current user is null.");
        }
    }

    /**
     * Generic method to open a compose window with specified FXML and title,
     * and apply a pre-preparation logic to its controller.
     * @param fxmlPath The path to the FXML file for the compose window.
     * @param title The title for the new stage.
     * @param controllerPreparer A functional interface to prepare the Contact controller.
     */
    private void openComposeWindow(String fxmlPath, String title, java.util.function.Consumer<FXMLLoader> controllerPreparer) {
        try {
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "Could not find FXML file: " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            controllerPreparer.accept(loader); // Prepare the controller using the provided logic

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.show();

            Contact contactController = (Contact) loader.getController();
            contactController.setComposeCallback(() -> {
                stage.close();
                if (callback != null) callback.onMessageSent();
            });

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open compose window: " + e.getMessage());
        }
    }

    /**
     * Handles the action for the delete button. Confirms deletion and then deletes the message.
     * @param event The ActionEvent that triggered this method.
     */
    private void handleDelete(ActionEvent event) {
        if (message != null) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this message?", ButtonType.YES, ButtonType.NO);
            confirmAlert.setTitle("Confirm Deletion");
            confirmAlert.setHeaderText("Delete Message");
            Optional<ButtonType> result = confirmAlert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.YES) {
                try {
                    if (messageService.deleteMessage(message.getId())) {
                        if (callback != null) callback.onMessageDeleted(message.getId());
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete message.");
                    }
                } catch (Exception e) {
                    System.err.println("Error deleting message: " + e.getMessage());
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete message: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Handles toggling the starred status of the message.
     * @param event The ActionEvent that triggered this method.
     */
    private void handleStarToggle(ActionEvent event) {
        if (message != null) {
            try {
                boolean newStarredStatus = !message.isStarred();
                if (messageService.updateMessageStarredStatus(message.getId(), newStarredStatus)) {
                    message.setStarred(newStarredStatus);
                    updateStarButtonAppearance();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Message " + (newStarredStatus ? "starred" : "unstarred") + ".");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to update starred status.");
                }
            } catch (Exception e) {
                System.err.println("Error toggling star status: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update starred status: " + e.getMessage());
            }
        }
    }

    /**
     * Handles toggling the archived status of the message.
     * @param event The ActionEvent that triggered this method.
     */
    private void handleArchiveToggle(ActionEvent event) {
        if (message != null) {
            try {
                boolean newArchivedStatus = !message.isArchived();
                if (messageService.updateMessageArchivedStatus(message.getId(), newArchivedStatus)) {
                    message.setArchived(newArchivedStatus);
                    updateArchiveButtonAppearance();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Message " + (newArchivedStatus ? "archived" : "unarchived") + ".");
                    if (callback != null) {
                        callback.onMessageArchived(message.getId(), newArchivedStatus); // Notify parent of archive change
                        callback.onBackToList(); // Return to list and refresh it
                    }
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to update archived status.");
                }
            } catch (Exception e) {
                System.err.println("Error toggling archive status: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update archived status: " + e.getMessage());
            }
        }
    }

    /**
     * Updates the text of the star button based on the message's starred status.
     */
    private void updateStarButtonAppearance() {
        if (message != null && starButton != null) {
            starButton.setText(message.isStarred() ? "Unstar" : "Star");
            // Update color based on status
            if (message.isStarred()) {
                starButton.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white;");
            } else {
                starButton.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
            }
        }
    }

    /**
     * Updates the text of the archive button based on the message's archived status.
     */
    private void updateArchiveButtonAppearance() {
        if (message != null && archiveButton != null) {
            archiveButton.setText(message.isArchived() ? "Unarchive" : "Archive");
            // Update color based on status
            if (message.isArchived()) {
                archiveButton.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");
            } else {
                archiveButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;");
            }
        }
    }

    /**
     * Displays a generic alert dialog.
     * @param alertType The type of alert to display (e.g., ERROR, INFORMATION).
     * @param title The title of the alert dialog.
     * @param content The main content message of the alert.
     */
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}