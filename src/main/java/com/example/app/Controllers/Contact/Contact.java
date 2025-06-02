package com.example.app.Controllers.Contact;

import com.example.app.Models.Message;
import com.example.app.Models.User;
import com.example.app.Services.MessageService;
import com.example.app.Services.SessionManager;
import com.example.app.Services.UserService;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Contact implements Initializable {

    // FXML elements for the main message composition area
    @FXML private TextField toInput;
    @FXML private ComboBox<String> recipientTypeComboBox;
    @FXML private Button addRecipientBtn;
    @FXML private ListView<String> recipientListView;
    @FXML private TextField subjectInput;
    @FXML private TextArea informationInput;
    @FXML private Label newMessageLabel;
    @FXML private Label statusLabel;
    @FXML private Button sendBtn;
    @FXML private Button discardBtn;

    // FXML elements for the left sidebar buttons
    @FXML private VBox btnContainer; // Assuming you might want to control the whole container
    @FXML private Button menuBtn;
    @FXML private Button writeBtn;
    @FXML private Button inboxBtn;
    @FXML private Button messageBtn; // This one is likely for a list view of messages
    @FXML private Button favoriteBtn;
    @FXML private Button homeBtn;
    @FXML private Button settingBtn;

    // Service dependencies
    private UserService userService;
    private MessageService messageService;
    private User currentUser;

    // List to hold the actual User objects for selected recipients
    private List<User> selectedRecipients = new ArrayList<>();

    // Callback interface for parent controllers (e.g., EmailController, MessageDetail)
    public interface ComposeMessageCallback {
        void onMessageSentSuccessfully();
    }
    private ComposeMessageCallback composeCallback;

    public void setComposeCallback(ComposeMessageCallback callback) {
        this.composeCallback = callback;
    }

    // Constructor to initialize services
    public Contact() {
        this.messageService = new MessageService();
        this.userService = new UserService();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Retrieve the current user from the session
        this.currentUser = SessionManager.getCurrentUser();

        // Handle case where no user is logged in
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Session Error", "No user logged in. Please log in to compose messages.");
            disableForm(true); // Disable all input fields and buttons
            return; // Exit initialization
        }

        // Initialize recipient type ComboBox
        recipientTypeComboBox.setItems(FXCollections.observableArrayList("client", "babysitter", "admin"));
        recipientTypeComboBox.getSelectionModel().selectFirst(); // Default to "client"

        // Set up action handlers for message composition buttons (with null checks)
        if (addRecipientBtn != null) addRecipientBtn.setOnAction(this::addRecipient);
        if (sendBtn != null) sendBtn.setOnAction(this::handleSendMessage);
        if (discardBtn != null) discardBtn.setOnAction(this::confirmDiscard);

        // Set up action handlers for sidebar navigation buttons (with null checks)
        // These handlers will typically navigate to different FXML views
        if (menuBtn != null) menuBtn.setOnAction(event -> System.out.println("Menu button clicked - Implement menu logic here"));
        if (writeBtn != null) writeBtn.setOnAction(this::handleWriteButtonClick); // This controller already handles 'write'
        if (inboxBtn != null) inboxBtn.setOnAction(this::handleInboxButtonClick);
        if (messageBtn != null) messageBtn.setOnAction(this::handleMessageListButtonClick);
        if (favoriteBtn != null) favoriteBtn.setOnAction(event -> System.out.println("Favorite button clicked - Implement favorite messages view"));
        if (homeBtn != null) homeBtn.setOnAction(this::handleHomeButtonClick);
        if (settingBtn != null) settingBtn.setOnAction(this::handleSettingsButtonClick);

        // Allow removing recipients from the list on double-click
        recipientListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selectedItem = recipientListView.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    confirmRemoveRecipient(selectedItem);
                }
            }
        });

        // Initialize status label and form state
        statusLabel.setText("");
        newMessageLabel.setText("New Message"); // Default label
        disableForm(false); // Ensure form is enabled if user is logged in
    }

    /**
     * Enables or disables all editable elements of the message composition form.
     * @param disable True to disable, false to enable.
     */
    private void disableForm(boolean disable) {
        // It's good practice to ensure elements are not null before calling methods on them,
        // especially immediately after FXML loading or in cases where FXML might be incomplete.
        if (toInput != null) toInput.setDisable(disable);
        if (recipientTypeComboBox != null) recipientTypeComboBox.setDisable(disable);
        if (addRecipientBtn != null) addRecipientBtn.setDisable(disable);
        if (recipientListView != null) recipientListView.setDisable(disable);
        if (subjectInput != null) subjectInput.setDisable(disable);
        if (informationInput != null) informationInput.setDisable(disable);
        if (sendBtn != null) sendBtn.setDisable(disable);
        if (discardBtn != null) discardBtn.setDisable(disable);

        // You might also disable sidebar buttons if no user is logged in
        if (menuBtn != null) menuBtn.setDisable(disable);
        if (writeBtn != null) writeBtn.setDisable(disable);
        if (inboxBtn != null) inboxBtn.setDisable(disable);
        if (messageBtn != null) messageBtn.setDisable(disable);
        if (favoriteBtn != null) favoriteBtn.setDisable(disable);
        if (homeBtn != null) homeBtn.setDisable(disable);
        if (settingBtn != null) settingBtn.setDisable(disable);
    }

    /**
     * Handles the 'Write' button click from the sidebar.
     * Since this FXML (`writeToSomeone.fxml`) is for composing messages,
     * this action simply clears the form for a new message.
     * @param event The ActionEvent that triggered this method.
     */
    @FXML
    private void handleWriteButtonClick(ActionEvent event) {
        clearForm();
        newMessageLabel.setText("New Message");
        statusLabel.setText("Ready to compose a new message.");
    }

    /**
     * Handles the 'Inbox' button click from the sidebar.
     * This method loads and displays an FXML for an inbox/message list view.
     * @param event The ActionEvent that triggered this method.
     */
    @FXML
    private void handleInboxButtonClick(ActionEvent event) {
        try {
            // Define the path to your MessageList FXML. Adjust if necessary.
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/Fxml/Messages/archive.fxml")));
            Parent root = loader.load();

            // Optionally, get the controller of MessageList.fxml and pass data (e.g., current user)
            // MessageListController messageListController = loader.getController();
            // messageListController.setLoggedInUser(currentUser);

            // Get the current stage from the event source and set the new scene
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Inbox"); // Set window title
            stage.show(); // Show the new scene
            // No need to close the current stage if you are replacing the scene
        } catch (IOException e) {
            System.err.println("Error opening Inbox: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not open Inbox. Please check the FXML path and controller.");
        }
    }

    /**
     * Handles the 'Messages' button click from the sidebar.
     * For now, it delegates to the inbox view. You could make this open a different view
     * if "Messages" and "Inbox" have distinct functionalities in your application.
     * @param event The ActionEvent that triggered this method.
     */
    @FXML
    private void handleMessageListButtonClick(ActionEvent event) {
        // Currently, it re-uses the Inbox view. Change this if "Messages" should be different.
        handleInboxButtonClick(event);
        System.out.println("Messages button clicked (currently opens Inbox view).");
    }

    /**
     * Handles the 'Go Back' (Home) button click from the sidebar.
     * This method typically navigates back to the main client dashboard.
     * @param event The ActionEvent that triggered this method.
     */
    @FXML
    private void handleHomeButtonClick(ActionEvent event) {
        try {
            String fxmlPath;
            String title;

            if (currentUser == null) {
                showAlert(Alert.AlertType.ERROR, "Session Error", "No user logged in. Cannot determine home page.");
                return;
            }

            // Determine the FXML path and title based on user type
            if ("client".equalsIgnoreCase(currentUser.getUserType())) {
                fxmlPath = "/Fxml/Babysitters/ClientSide.fxml";
                title = "Client Dashboard";
            } else if ("babysitter".equalsIgnoreCase(currentUser.getUserType())) {
                fxmlPath = "/Fxml/Babysitters/babysitters.fxml"; // Assuming this is your babysitter dashboard FXML
                title = "Babysitter Dashboard";
            } else {
                // Default or handle other user types (e.g., admin)
                showAlert(Alert.AlertType.WARNING, "Navigation Warning", "Unsupported user type for home navigation: " + currentUser.getUserType());
                return; // Prevent navigation for unhandled types
            }

            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource(fxmlPath)));
            Parent root = loader.load();

            // Optionally, pass data to the dashboard controller if needed
            // Example for ClientDashboardController:
            // if ("client".equalsIgnoreCase(currentUser.getUserType())) {
            //     ClientDashboardController dashboardController = loader.getController();
            //     dashboardController.setLoggedInUser(currentUser);
            // } else if ("babysitter".equalsIgnoreCase(currentUser.getUserType())) {
            //     BabysitterDashboardController dashboardController = loader.getController();
            //     dashboardController.setLoggedInUser(currentUser);
            // }


            // Get the current stage and set the new scene
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title); // Set window title
            stage.show();
        } catch (IOException e) {
            System.err.println("Error opening Dashboard: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not open Home Dashboard. Please check the FXML path and controller for " + currentUser.getUserType() + ".");
        }
    }

    /**
     * Handles the 'Settings' button click from the sidebar.
     * This method loads and displays the user's account settings FXML.
     * @param event The ActionEvent that triggered this method.
     */
    @FXML
    private void handleSettingsButtonClick(ActionEvent event) {
        try {
            // Define the path to your Account Settings FXML. Adjust if necessary.
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/Fxml/User/Account.fxml")));
            Parent root = loader.load();

            // Get the current stage and set the new scene
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Account Settings"); // Set window title
            stage.show();
        } catch (IOException e) {
            System.err.println("Error opening Settings: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not open Settings. Please check the FXML path and controller.");
        }
    }

    /**
     * Handles adding a recipient to the ListView based on input from 'toInput' and 'recipientTypeComboBox'.
     * @param event The ActionEvent that triggered this method.
     */
    @FXML
    private void addRecipient(ActionEvent event) {
        String recipientInputText = toInput.getText().trim();
        String selectedType = recipientTypeComboBox.getSelectionModel().getSelectedItem();

        if (recipientInputText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Information", "Please enter a recipient username or email.");
            return;
        }
        if (selectedType == null || selectedType.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Information", "Please select a recipient type (client, babysitter, admin).");
            return;
        }

        try {
            Optional<User> foundUserOptional = userService.getUserByUsernameOrEmail(recipientInputText);

            if (foundUserOptional.isPresent()) {
                User user = foundUserOptional.get();
                // Ensure the found user matches the selected type
                if (user.getUserType().equalsIgnoreCase(selectedType)) {
                    // Prevent adding the same user twice
                    if (selectedRecipients.stream().noneMatch(r -> r.getId() == user.getId())) {
                        selectedRecipients.add(user);
                        String recipientDisplay = user.getName() + " (" + user.getUserType() + " - ID: " + user.getId() + ")";
                        recipientListView.getItems().add(recipientDisplay);
                        toInput.clear(); // Clear input after adding
                        statusLabel.setText("Recipient added: " + user.getName());
                    } else {
                        statusLabel.setText("Recipient already in list.");
                    }
                } else {
                    statusLabel.setText("User type mismatch for '" + recipientInputText + "'. Expected: " + selectedType + ", Found: " + user.getUserType());
                    showAlert(Alert.AlertType.WARNING, "Type Mismatch", "User '" + user.getName() + "' is of type '" + user.getUserType() + "', not '" + selectedType + "'.");
                }
            } else {
                statusLabel.setText("No user found for: " + recipientInputText);
                showAlert(Alert.AlertType.WARNING, "User Not Found", "No user found with the provided username/email for the selected type.");
            }
        } catch (Exception e) {
            statusLabel.setText("Error adding recipient.");
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to add recipient: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Confirms with the user before removing a recipient from the list.
     * @param recipientDisplayString The string representation of the recipient to remove.
     */
    private void confirmRemoveRecipient(String recipientDisplayString) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Remove Recipient");
        alert.setHeaderText("Remove this recipient?");
        alert.setContentText("Are you sure you want to remove " + recipientDisplayString + " from the recipient list?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                int userIdToRemove = parseIdFromRecipientDisplay(recipientDisplayString);
                selectedRecipients.removeIf(user -> user.getId() == userIdToRemove);
                recipientListView.getItems().remove(recipientDisplayString);
                statusLabel.setText("Recipient removed.");
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Could not remove recipient due to ID parsing error.");
            }
        }
    }

    /**
     * Confirms with the user before discarding the current message.
     * @param event The ActionEvent that triggered this method.
     */
    @FXML
    private void confirmDiscard(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Discard");
        alert.setHeaderText("Discard Message?");
        alert.setContentText("Are you sure you want to discard this message? All unsaved changes will be lost.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            clearForm();
            statusLabel.setText("Message discarded.");
        }
    }

    /**
     * Handles the action of sending a message (new, reply, or forward).
     * Iterates through selected recipients and creates a message for each.
     * @param event The ActionEvent that triggered this method.
     */
    @FXML
    private void handleSendMessage(ActionEvent event) {
        if (currentUser == null || !SessionManager.isLoggedIn()) {
            showAlert(Alert.AlertType.ERROR, "Authentication Error", "Please log in to send messages.");
            return;
        }

        int senderId = currentUser.getId();
        String senderType = currentUser.getUserType();

        String subject = subjectInput.getText().trim();
        String content = informationInput.getText().trim();

        if (selectedRecipients.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Recipients", "Please add at least one recipient.");
            return;
        }
        if (subject.isEmpty() || content.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Fields", "Subject and content cannot be empty.");
            return;
        }

        boolean allMessagesSentSuccessfully = true;
        for (User receiverUser : selectedRecipients) {
            try {
                Message newMessage = new Message(
                        senderId,
                        receiverUser.getId(),
                        senderType,
                        receiverUser.getUserType(),
                        subject,
                        content
                );

                if (messageService.createMessage(newMessage)) {
                    System.out.println("Message sent successfully to " + receiverUser.getName() + " (ID: " + receiverUser.getId() + ")");
                } else {
                    allMessagesSentSuccessfully = false;
                    showAlert(Alert.AlertType.ERROR, "Error Sending", "Failed to send message to " + receiverUser.getName() + ".");
                    System.err.println("Failed to send message to ID: " + receiverUser.getId());
                }
            } catch (Exception e) {
                allMessagesSentSuccessfully = false;
                showAlert(Alert.AlertType.ERROR, "Sending Error", "An unexpected error occurred while sending to " + receiverUser.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        if (allMessagesSentSuccessfully) {
            clearForm();
            statusLabel.setText("All messages sent successfully!");
            showAlert(Alert.AlertType.INFORMATION, "Success", "All messages have been sent successfully.");
            if (composeCallback != null) {
                composeCallback.onMessageSentSuccessfully(); // Notify parent controller
            }
        } else {
            statusLabel.setText("Some messages failed to send. Check alerts for details.");
            // Individual showAlerts for failures are handled in the loop.
        }
    }

    /**
     * Prepares the form for replying to an existing message.
     * @param originalMessage The message to which a reply is being composed.
     * @param currentUser The currently logged-in user.
     */
    public void prepareReply(Message originalMessage, User currentUser) {
        if (originalMessage == null || currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Initialization Error", "Cannot prepare reply: original message or current user is null.");
            return;
        }
        clearForm(); // Clear any existing content

        // The sender of the original message becomes the recipient of the reply
        Optional<User> originalSenderOptional = userService.getUserById(originalMessage.getSenderId());
        originalSenderOptional.ifPresent(user -> {
            // Avoid adding self as recipient if the user is both sender and receiver (e.g., test messages to self)
            if (user.getId() != currentUser.getId() || !user.getUserType().equalsIgnoreCase(currentUser.getUserType())) {
                selectedRecipients.add(user);
                String recipientDisplay = user.getName() + " (" + user.getUserType() + " - ID: " + user.getId() + ")";
                recipientListView.getItems().add(recipientDisplay);
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Self-Reply", "You are the sender of the original message. If you wish to send a message to yourself, please use a new message.");
                // Optionally disable recipient fields if replying to self is prevented.
                if (toInput != null) toInput.setDisable(true);
                if (addRecipientBtn != null) addRecipientBtn.setDisable(true);
            }
        });

        // Pre-fill subject and content based on reply template
        Message replyTemplate = originalMessage.createReplyTemplate();
        if (subjectInput != null) subjectInput.setText(replyTemplate.getSubject());
        if (informationInput != null) informationInput.setText(replyTemplate.getContent());

        if (newMessageLabel != null) newMessageLabel.setText("Reply Message");
        statusLabel.setText("Ready to reply.");
    }

    /**
     * Prepares the form for forwarding an existing message.
     * @param originalMessage The message being forwarded.
     * @param currentUser The currently logged-in user.
     */
    public void prepareForward(Message originalMessage, User currentUser) {
        if (originalMessage == null || currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Initialization Error", "Cannot prepare forward: original message or current user is null.");
            return;
        }
        clearForm(); // Clear recipients for a forward, as new ones will be added

        // Pre-fill subject and content based on forward template
        Message forwardTemplate = originalMessage.createForwardTemplate();
        if (subjectInput != null) subjectInput.setText(forwardTemplate.getSubject());
        if (informationInput != null) informationInput.setText(forwardTemplate.getContent());

        if (newMessageLabel != null) newMessageLabel.setText("Forward Message");
        statusLabel.setText("Ready to forward. Please add recipients.");
    }

    /**
     * Prepares the form for a new message, optionally pre-filling a recipient.
     * Used when initiating a message from a user profile (e.g., from a babysitter profile).
     * @param recipientUser The user to pre-fill as a recipient, or null for a completely new message.
     */
    public void prepareNewMessage(User recipientUser) {
        clearForm(); // Clear any existing content

        if (recipientUser != null) {
            // Prevent composing to self if clicking on own profile
            if (currentUser != null && (recipientUser.getId() == currentUser.getId() && recipientUser.getUserType().equalsIgnoreCase(currentUser.getUserType()))) {
                showAlert(Alert.AlertType.INFORMATION, "Self-Message", "You cannot send a new message to yourself from your own profile. Use a new message composition if you wish to send to yourself.");
                // Disable recipient fields to prevent adding self accidentally
                if (toInput != null) toInput.setDisable(true);
                if (addRecipientBtn != null) addRecipientBtn.setDisable(true);
            } else {
                selectedRecipients.add(recipientUser);
                String recipientDisplay = recipientUser.getName() + " (" + recipientUser.getUserType() + " - ID: " + recipientUser.getId() + ")";
                recipientListView.getItems().add(recipientDisplay);
                if (toInput != null) toInput.setText(recipientUser.getName()); // Pre-fill but allow adding others
                statusLabel.setText("Composing message to " + recipientUser.getName());
            }
        } else {
            statusLabel.setText("Ready to compose new message.");
        }
        if (newMessageLabel != null) newMessageLabel.setText("New Message");
    }

    /**
     * Retrieves a user's display name (username or email) by ID.
     * @param userId The ID of the user.
     * @return The user's name, email, or a fallback string.
     */
    private String getUserDisplayName(int userId) {
        if (userId == 0) return "Unknown/Unassigned"; // Placeholder for unassigned receiver in forward template
        return userService.getUserById(userId)
                .map(user -> {
                    if (user.getName() != null && !user.getName().isEmpty()) {
                        return user.getName();
                    } else if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                        return user.getEmail();
                    } else {
                        return "Unknown User (" + userId + ")";
                    }
                })
                .orElse("Unknown User (" + userId + ")");
    }

    /**
     * Parses the user ID from the recipient display string in the ListView.
     * Uses a regex pattern for more robust parsing.
     * @param displayString The string from the recipient ListView (e.g., "John Doe (client - ID: 123)").
     * @return The parsed integer ID.
     * @throws NumberFormatException if the ID cannot be parsed.
     */
    private int parseIdFromRecipientDisplay(String displayString) {
        Pattern pattern = Pattern.compile("ID: (\\d+)\\)");
        Matcher matcher = pattern.matcher(displayString);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        throw new NumberFormatException("Could not parse user ID from recipient string: " + displayString);
    }

    /**
     * Clears all input fields and resets the form to its initial state.
     */
    private void clearForm() {
        if (toInput != null) toInput.clear();
        if (recipientListView != null) recipientListView.getItems().clear();
        selectedRecipients.clear(); // Clear the actual User objects too
        if (subjectInput != null) subjectInput.clear();
        if (informationInput != null) informationInput.clear();
        if (newMessageLabel != null) newMessageLabel.setText("New Message");
        if (statusLabel != null) statusLabel.setText("");
        if (recipientTypeComboBox != null) recipientTypeComboBox.getSelectionModel().selectFirst(); // Reset to default type
    }

    /**
     * Closes the current window associated with the send button.
     */
    private void closeWindow() {
        // Ensure sendBtn is not null before attempting to get its scene
        if (sendBtn != null && sendBtn.getScene() != null) {
            Stage stage = (Stage) sendBtn.getScene().getWindow();
            if (stage != null) {
                stage.close();
            }
        }
    }

    /**
     * Displays an alert dialog.
     * @param type The type of alert (e.g., INFORMATION, ERROR).
     * @param title The title of the alert window.
     * @param message The main message content of the alert.
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /*
     * DEPRECATED METHOD: buildQuotedMessage
     * This method is marked as deprecated because its functionality is now
     * ideally handled by the Message model's `createReplyTemplate` and
     * `createForwardTemplate` methods for better encapsulation and reusability.
     * Keep this method only if direct string manipulation is specifically required
     * outside the Message model's templating logic.
     */
    @Deprecated
    private String buildQuotedMessage(Message msg, String header) {
        String senderName = getUserDisplayName(msg.getSenderId());
        String receiverName = getUserDisplayName(msg.getReceiverId());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm"); // Corrected pattern

        return "\n\n--- " + header + " ---\n" +
                "From: " + senderName + "\n" +
                "To: " + receiverName + "\n" +
                "Date: " + msg.getTimestamp().format(formatter) + "\n" +
                "Subject: " + msg.getSubject() + "\n\n" +
                msg.getContent();
    }
}