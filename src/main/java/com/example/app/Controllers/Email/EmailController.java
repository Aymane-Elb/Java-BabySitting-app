package com.example.app.Controllers.Email;

import com.example.app.Controllers.Contact.Contact; // Import the Contact controller
import com.example.app.Models.Message;
import com.example.app.Models.User;
import com.example.app.Services.MessageService;
import com.example.app.Services.SessionManager; // Import SessionManager

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane; // Assuming you use StackPane for view switching
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class EmailController implements Initializable {

    @FXML private VBox menuBar;
    @FXML private Button menuBtn;
    @FXML private Button writeBtn;
    @FXML private Button inboxBtn;
    @FXML private Button messageBtn; // This will show all messages (sent/received)
    @FXML private Button favoriteBtn;
    @FXML private Button homeBtn;
    @FXML private Button settingBtn;

    @FXML private StackPane contentStackPane; // Assuming this is your main content area
    @FXML private VBox messageListView; // The VBox containing the ListView
    @FXML private VBox composeView;     // The VBox for composing messages (e.g., writeToSomeone.fxml content)
    @FXML private VBox messageDetailView; // The VBox for message details (messageDetail.fxml content)

    @FXML private ListView<Message> emailListView;
    @FXML private Button backToListBtn; // Button visible in detail view to go back to list
    @FXML private Button searchBtn; // Assuming you have a search button

    private MessageService messageService;
    private User currentUser; // The currently logged-in user

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize services and retrieve the current user
        messageService = new MessageService("jdbc:postgresql://localhost:5432/babysitting", "Aymane", "RACHIDAx@436550");
        currentUser = SessionManager.getCurrentUser();

        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Session Error", "No user logged in. Please log in first.");
            // Consider redirecting to login or disabling features
            return;
        }

        configureMenuButtons();
        setupEmailListView(); // Configure how messages are displayed in the list

        // Set up action handlers
        menuBtn.setOnAction(e -> toggleMenu());
        writeBtn.setOnAction(this::openWriteToSomeone);
        inboxBtn.setOnAction(this::openInbox);
        messageBtn.setOnAction(this::openAllMessages); // Renamed for clarity to show all messages
        favoriteBtn.setOnAction(this::openFavorite);
        homeBtn.setOnAction(this::openClientSide); // Assuming this navigates to another part of the app
        settingBtn.setOnAction(this::openSettings); // Assuming this navigates to another part of the app
        backToListBtn.setOnAction(this::showMessageList); // Now correctly switches StackPane views
        searchBtn.setOnAction(this::handleSearch); // Assuming this triggers a search logic

        // Initialize view states (show message list by default)
        composeView.setVisible(false);
        messageDetailView.setVisible(false);
        messageListView.setVisible(true);

        // Load initial messages (e.g., inbox by default)
        openInbox(null);
    }

    private void configureMenuButtons() {
    }

    /**
     * Configures the appearance and behavior of items in the message list view.
     */
    private void setupEmailListView() {
        emailListView.setCellFactory(lv -> new ListCell<Message>() {
            @Override
            protected void updateItem(Message message, boolean empty) {
                super.updateItem(message, empty);
                if (empty || message == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle(null); // Clear previous styles
                } else {
                    String displayParticipant;
                    String subjectPrefix = "";
                    boolean isSentByCurrentUser = (message.getSenderId() == currentUser.getId() &&
                            message.getSenderType().equals(currentUser.getUserType()));

                    if (isSentByCurrentUser) {
                        displayParticipant = "To: " + messageService.getUserFullName(message.getReceiverId(), message.getReceiverType());
                        subjectPrefix = "Sent: ";
                        // Apply specific style for sent messages
                        setStyle("-fx-font-weight: bold; -fx-text-fill: #3498db;"); // Blue for sent
                    } else { // Current user is the receiver
                        displayParticipant = "From: " + messageService.getUserFullName(message.getSenderId(), message.getSenderType());
                        // Add "New: " prefix if unread and received by current user
                        subjectPrefix = message.isRead() ? "" : "New: ";
                        setStyle(message.isRead() ? "-fx-font-weight: normal; -fx-text-fill: black;" : "-fx-font-weight: bold; -fx-text-fill: green;"); // Green for unread
                    }

                    // Display subject, participant, and formatted timestamp
                    setText(subjectPrefix + message.getSubject() + " - " + displayParticipant +
                            " (" + message.getTimestamp().format(DateTimeFormatter.ofPattern("MMM d, HH:mm")) + ")");
                }
            }
        });

        // Add selection listener to show message detail when an item is selected
        emailListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                showMessageDetail(newValue); // Pass the selected Message object
            }
        });
    }

    /**
     * Toggles the visibility of the side menu bar.
     */
    private void toggleMenu() {
        menuBar.setVisible(!menuBar.isVisible());
        menuBar.setManaged(menuBar.isVisible());
    }

    /**
     * Displays the message list view and hides other views.
     * @param event The ActionEvent (can be null if called programmatically).
     */
    @FXML
    private void showMessageList(ActionEvent event) {
        composeView.setVisible(false);
        composeView.setManaged(false); // No longer managed when hidden
        messageDetailView.setVisible(false);
        messageDetailView.setManaged(false); // No longer managed when hidden
        messageListView.setVisible(true);
        messageListView.setManaged(true); // Managed when visible
    }

    /**
     * Opens the "Write to Someone" message composition window.
     * @param event The ActionEvent.
     */
    @FXML
    private void openWriteToSomeone(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Messages/writeToSomeone.fxml"));
            Parent root = loader.load();

            Contact contactController = loader.getController();
            // Set a callback for the compose controller to refresh the list after a message is sent
            contactController.setComposeCallback(() -> {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Message sent successfully!");
                openAllMessages(null); // Refresh all messages in the list
                showMessageList(null); // Return to list view
            });

            Stage stage = new Stage();
            stage.setTitle("Compose Message");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open compose window.");
        }
    }

    /**
     * Loads and displays inbox messages for the current user.
     * @param event The ActionEvent.
     */
    @FXML
    private void openInbox(ActionEvent event) {
        showMessageList(event); // Ensure list view is visible
        if (currentUser != null) {
            List<Message> inboxMessages = messageService.getInboxMessagesForUser(currentUser.getId());
            emailListView.setItems(FXCollections.observableArrayList(inboxMessages));
            System.out.println("Opening Inbox...");
        } else {
            showAlert(Alert.AlertType.ERROR, "User Error", "Current user not set.");
        }
    }

    /**
     * Loads and displays all messages (sent and received) for the current user.
     * @param event The ActionEvent.
     */
    @FXML
    private void openAllMessages(ActionEvent event) {
        showMessageList(event); // Ensure list view is visible
        if (currentUser != null) {
            List<Message> allMessages = messageService.getAllMessagesForUser(currentUser.getId());
            emailListView.setItems(FXCollections.observableArrayList(allMessages));
            System.out.println("Opening All Messages...");
        } else {
            showAlert(Alert.AlertType.ERROR, "User Error", "Current user not set.");
        }
    }

    /**
     * Loads and displays starred messages for the current user.
     * @param event The ActionEvent.
     */
    @FXML
    private void openFavorite(ActionEvent event) {
        showMessageList(event); // Ensure list view is visible
        if (currentUser != null) {
            List<Message> starredMessages = messageService.getStarredMessagesForUser(currentUser.getId());
            emailListView.setItems(FXCollections.observableArrayList(starredMessages));
            System.out.println("Opening Favorites...");
        } else {
            showAlert(Alert.AlertType.ERROR, "User Error", "Current user not set.");
        }
    }

    /**
     * Handles the search action (placeholder).
     * @param event The ActionEvent.
     */
    @FXML
    private void handleSearch(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Search", "Search functionality not yet implemented.");
    }

    /**
     * Placeholder for navigating to client side/dashboard.
     * @param event The ActionEvent.
     */
    @FXML
    private void openClientSide(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Navigation", "Navigating to Client Dashboard.");
        // Implement actual navigation logic here
    }

    /**
     * Placeholder for navigating to settings.
     * @param event The ActionEvent.
     */
    @FXML
    private void openSettings(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Navigation", "Navigating to Settings.");
        // Implement actual navigation logic here
    }

    /**
     * Displays the detailed view of a selected message.
     * This method is called from the ListView's selection listener.
     * @param selectedMessage The Message object to display.
     */
    public void showMessageDetail(Message selectedMessage) {
        composeView.setVisible(false);
        composeView.setManaged(false);
        messageListView.setVisible(false);
        messageListView.setManaged(false);
        messageDetailView.setVisible(true); // Ensure this VBox is visible
        messageDetailView.setManaged(true);

        try {
            // Clear any old content from messageDetailView first
            messageDetailView.getChildren().clear();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Email/messageDetail.fxml"));
            VBox detailViewContent = loader.load(); // Load the content of MessageDetail.fxml
            MessageDetail messageDetailController = loader.getController();

            // Pass the selected message and current user to the MessageDetail controller
            messageDetailController.setMessage(selectedMessage);
            messageDetailController.setCurrentUser(currentUser); // Pass the current user

            // Set up the callback to handle actions from MessageDetail
            messageDetailController.setCallback(new MessageDetail.MessageDetailCallback() {
                @Override
                public void onBackToList() {
                    showMessageList(null); // Switch back to message list
                    // Re-load messages to ensure the list is fresh (e.g., if a message was deleted or read status changed)
                    openAllMessages(null); // Or openInbox if that's the default
                }

                @Override
                public void onMessageDeleted(int messageId) {
                    // Remove the deleted message from the ListView directly
                    emailListView.getItems().removeIf(m -> m.getId() == messageId);
                    showMessageList(null); // Switch back to message list
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Message deleted successfully.");
                }

                @Override
                public void onMessageSent() {
                    showMessageList(null); // Switch back to message list
                    openAllMessages(null); // Refresh all messages after sending a reply/forward
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Message sent successfully!");
                }
            });

            // Set the loaded FXML content into the messageDetailView VBox
            messageDetailView.getChildren().setAll(detailViewContent);

            // Mark message as read if it's a received message for the current user and not already read
            if (selectedMessage.getReceiverId() == currentUser.getId() && !selectedMessage.isRead()) {
                boolean updated = messageService.updateMessageReadStatus(selectedMessage.getId(), true);
                if (updated) {
                    selectedMessage.setRead(true); // Update the model in memory
                    emailListView.refresh(); // Refresh the item in the list view to update its style (e.g., remove "New:")
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load message details.");
        }
    }

    /**
     * Shows an alert dialog.
     */
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void openCloseMenu(ActionEvent actionEvent) {
    }
}