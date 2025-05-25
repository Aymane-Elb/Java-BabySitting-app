package com.example.app.Controllers.Contact;

import com.example.app.Models.Message;
import com.example.app.Models.User;
import com.example.app.Services.MessageService;
import com.example.app.Services.SessionManager;
import com.example.app.Services.UserService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType; // Correct import for ButtonType

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

public class MessageList implements Initializable {

    @FXML private ListView<Message> messageListView;
    @FXML private Label senderLabel;
    @FXML private Label receiverLabel;
    @FXML private Label subjectLabel;
    @FXML private Label timestampLabel;
    @FXML private TextArea contentTextArea;
    @FXML private Button markAsReadButton;
    @FXML private Button deleteButton;
    @FXML private Button replyButton;
    @FXML private Button forwardButton;

    private MessageService messageService;
    private UserService userService;
    private ObservableList<Message> displayedMessages;
    private Map<Integer, User> userCache = new HashMap<>(); // Cache for user objects to avoid repeated DB calls

    // To track the last message timestamp for polling (for INBOX, not necessarily SENT)
    private LocalDateTime lastPolledMessageTimestamp;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize services
        // IMPORTANT: Provide your actual DB credentials here, or ensure MessageService
        // is configured to load them from a suitable source.
        this.messageService = new MessageService();
        this.userService = new UserService();

        // Check if a user is logged in
        if (!SessionManager.isLoggedIn()) {
            showAlert(Alert.AlertType.ERROR, "Authentication Error", "No user is logged in. Please log in to view your messages.");
            disableMessageView();
            return;
        }

        // Initialize displayedMessages as an empty ObservableList
        displayedMessages = FXCollections.observableArrayList();
        messageListView.setItems(displayedMessages);

        // Load messages specifically sent by the babysitter to clients
        loadBabysitterSentMessagesToClients();

        // Add listener for message selection
        messageListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showMessageDetails(newValue));

        // Note: For a "Sent Messages" view, polling for *new incoming* messages
        // isn't typically necessary. If you want new *sent* messages to appear
        // in real-time, you'd need to refresh this list after the user sends a message
        // from a separate compose window.
        // The `messageService.addMessageListener` and `startMessagePolling` would be
        // more relevant for an "Inbox" view.
        // However, if you intend to reuse this controller for general message management
        // and want real-time notifications for incoming messages too, you could uncomment
        // and adapt these. For now, focusing on the "sent to client" requirement.

        // Initialize last polled timestamp for potential future use or if this controller
        // also displays inbox messages with polling.
        lastPolledMessageTimestamp = LocalDateTime.now();

        // Initial state for buttons
        setButtonsDisabled(true); // Disable buttons until a message is selected
    }

    private void loadBabysitterSentMessagesToClients() {
        int userId = SessionManager.getCurrentUserId();
        String userType = SessionManager.getCurrentUserType(); // e.g., "babysitter"

        if (userType != null && userType.equalsIgnoreCase("babysitter")) {
            // Use the new service method to get messages sent by babysitter to clients
            List<Message> sentToClients = messageService.getSentMessagesToReceiverType(userId, "client");
            displayedMessages.setAll(sentToClients); // Update the ObservableList
            if (sentToClients.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "No Messages Sent", "You haven't sent any messages to clients yet.");
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Role Mismatch", "This view is designed for babysitters to see messages sent to clients. You are logged in as a " + userType + ".");
            disableMessageView();
        }
    }

    private void showMessageDetails(Message message) {
        if (message == null) {
            clearMessageDetails();
            setButtonsDisabled(true);
            return;
        }

        // Use the userCache or fetch from UserService
        senderLabel.setText("From: " + getUserName(message.getSenderId()));
        receiverLabel.setText("To: " + getUserName(message.getReceiverId()));
        subjectLabel.setText("Subject: " + message.getSubject());
        timestampLabel.setText("Date: " + message.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        contentTextArea.setText(message.getContent());

        // For sent messages, marking as read/unread usually pertains to the receiver's status.
        // You can decide if the 'is_read' flag on a sent message indicates if *you* have reviewed it.
        // For simplicity, we'll keep the button logic as is, assuming it toggles *your* perceived read status.
        setButtonsDisabled(false); // Enable buttons when a message is selected
        updateMarkAsReadButtonText(message);
    }

    private String getUserName(int userId) {
        // Check cache first
        if (userCache.containsKey(userId)) {
            return userCache.get(userId).getName();
        }

        // Fetch from UserService if not in cache
        Optional<User> userOptional = userService.getUserById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            userCache.put(userId, user); // Add to cache
            return user.getName();
        }
        return "Unknown User"; // Fallback
    }

    private void clearMessageDetails() {
        senderLabel.setText("From:");
        receiverLabel.setText("To:");
        subjectLabel.setText("Subject:");
        timestampLabel.setText("Date:");
        contentTextArea.setText("");
    }

    private void setButtonsDisabled(boolean disabled) {
        markAsReadButton.setDisable(disabled);
        deleteButton.setDisable(disabled);
        replyButton.setDisable(disabled);
        forwardButton.setDisable(disabled);
    }

    private void disableMessageView() {
        messageListView.setDisable(true);
        clearMessageDetails();
        setButtonsDisabled(true);
    }

    // This method would be used if you start polling for incoming messages
    // (e.g., if this controller also serves as an inbox)
    private void handleNewMessageNotification(Message newMessage) {
        // Ensure the new message is actually for the current user and not already displayed
        if (newMessage.getReceiverId() == SessionManager.getCurrentUserId() &&
                !displayedMessages.contains(newMessage)) {
            // Decide where to add it in the list (e.g., at the top)
            displayedMessages.add(0, newMessage);
            showAlert(Alert.AlertType.INFORMATION, "New Message", "You've received a new message from " + getUserName(newMessage.getSenderId()));
            messageListView.scrollTo(0);
            messageListView.getSelectionModel().select(0);
        }
    }

    @FXML
    private void handleMarkAsReadUnread() {
        Message selectedMessage = messageListView.getSelectionModel().getSelectedItem();
        if (selectedMessage != null) {
            boolean success;
            if (selectedMessage.isRead()) {
                success = messageService.markMessageAsUnread(selectedMessage.getId());
            } else {
                success = messageService.markMessageAsRead(selectedMessage.getId());
            }

            if (success) {
                selectedMessage.setRead(!selectedMessage.isRead()); // Toggle local object status
                messageListView.refresh(); // Refresh ListView to reflect change (if cell factory uses read status)
                updateMarkAsReadButtonText(selectedMessage);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update message status.");
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Selection Error", "Please select a message.");
        }
    }

    private void updateMarkAsReadButtonText(Message message) {
        if (message != null) {
            markAsReadButton.setText(message.isRead() ? "Mark as Unread" : "Mark as Read");
        } else {
            markAsReadButton.setText("Mark as Read/Unread");
        }
    }

    @FXML
    private void handleDeleteMessage() {
        Message selectedMessage = messageListView.getSelectionModel().getSelectedItem();
        if (selectedMessage != null) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this message?", ButtonType.YES, ButtonType.NO);
            confirmAlert.setHeaderText("Delete Confirmation");
            confirmAlert.showAndWait();

            if (confirmAlert.getResult() == ButtonType.YES) {
                if (messageService.deleteMessage(selectedMessage.getId())) {
                    displayedMessages.remove(selectedMessage); // Remove from ObservableList
                    clearMessageDetails();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Message deleted.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete message.");
                }
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Selection Error", "Please select a message to delete.");
        }
    }

    @FXML
    private void handleReplyMessage() {
        Message selectedMessage = messageListView.getSelectionModel().getSelectedItem();
        if (selectedMessage != null) {
            // Ideally, you'd open a new message composition window (e.g., ContactController's view)
            // and pass data to pre-populate it for a reply.
            // Example:
            // FXML loader, pass selectedMessage for reply context:
            // FXMLLoader loader = new FXMLLoader(getClass().getResource("/path/to/Contact.fxml"));
            // Parent root = loader.load();
            // ContactController controller = loader.getController();
            // controller.initForReply(selectedMessage); // A method you'd add to ContactController
            // Stage stage = new Stage();
            // stage.setScene(new Scene(root));
            // stage.show();

            showAlert(Alert.AlertType.INFORMATION, "Reply Functionality", "Reply feature ready for: " + selectedMessage.getSubject());
            // TODO: Implement actual UI navigation to a new message composition window
        } else {
            showAlert(Alert.AlertType.WARNING, "Selection Error", "Please select a message to reply to.");
        }
    }

    @FXML
    private void handleForwardMessage() {
        Message selectedMessage = messageListView.getSelectionModel().getSelectedItem();
        if (selectedMessage != null) {
            // Similar to reply, open a new message composition window and pre-populate for forwarding.
            // The receiverId for forward will typically be unset, requiring user input.
            showAlert(Alert.AlertType.INFORMATION, "Forward Functionality", "Forward feature ready for: " + selectedMessage.getSubject());
            // TODO: Implement actual UI navigation to a new message composition window
        } else {
            showAlert(Alert.AlertType.WARNING, "Selection Error", "Please select a message to forward.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Don't forget to stop polling when the view is closed or application exits
    public void shutdown() {
        messageService.stopMessagePolling();
    }
}