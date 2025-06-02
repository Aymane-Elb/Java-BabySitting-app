package com.example.app.Controllers.Email;

import com.example.app.Controllers.Contact.Contact;
import com.example.app.Models.Message;
import com.example.app.Models.User;
import com.example.app.Services.MessageService;
import com.example.app.Services.SessionManager;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class EmailController implements Initializable {

    @FXML private VBox menuBar;
    @FXML private Button menuBtn;
    @FXML private Button writeBtn;
    @FXML private Button inboxBtn;
    @FXML private Button messageBtn;
    @FXML private Button favoriteBtn;
    @FXML private Button archivedBtn;
    @FXML private Button homeBtn;
    @FXML private Button settingBtn;

    @FXML private StackPane contentStackPane;
    @FXML private VBox messageListViewContainer;
    @FXML private VBox messageDetailViewContainer;

    @FXML private ListView<Message> emailListView;
    @FXML private Button backToListBtn;
    @FXML private Button searchBtn;
    @FXML private TextField searchInput;

    private MessageService messageService;
    private User currentUser;
    private String currentViewType = "Inbox";
    private boolean menuVisible = true;
    private List<Message> currentMessages;

    public EmailController() {
        // Default constructor
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        messageService = new MessageService();
        currentUser = SessionManager.getCurrentUser();

        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Session Error", "No user logged in. Please log in first.");
            return;
        }

        setupEmailListView();
        setupButtonActions();
        initializeView();

        // Load initial messages (inbox by default)
        openInbox(null);
    }

    private void initializeView() {
        // Initially hide detail view, show message list
        if (messageDetailViewContainer != null) {
            messageDetailViewContainer.setVisible(false);
            messageDetailViewContainer.setManaged(false);
        }

        if (messageListViewContainer != null) {
            messageListViewContainer.setVisible(true);
            messageListViewContainer.setManaged(true);
        }

        // Set initial button states
        updateButtonStates();
    }

    private void setupButtonActions() {
        // Navigation buttons
        if (menuBtn != null) menuBtn.setOnAction(this::toggleMenu);
        if (writeBtn != null) writeBtn.setOnAction(this::openWriteToSomeone);
        if (inboxBtn != null) inboxBtn.setOnAction(this::openInbox);
        if (messageBtn != null) messageBtn.setOnAction(this::openAllMessages);
        if (favoriteBtn != null) favoriteBtn.setOnAction(this::openFavorite);
        if (archivedBtn != null) archivedBtn.setOnAction(this::openArchived);
        if (homeBtn != null) homeBtn.setOnAction(this::openClientSideEnhanced);
        if (settingBtn != null) settingBtn.setOnAction(this::openSettings);

        // Action buttons
        if (backToListBtn != null) backToListBtn.setOnAction(this::showMessageList);
        if (searchBtn != null) searchBtn.setOnAction(this::handleSearch);

        // Search input enter key support
        if (searchInput != null) {
            searchInput.setOnAction(this::handleSearch);
        }
    }

    private void setupEmailListView() {
        if (emailListView == null) return;

        emailListView.setCellFactory(lv -> new ListCell<Message>() {
            @Override
            protected void updateItem(Message message, boolean empty) {
                super.updateItem(message, empty);
                if (empty || message == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle(null);
                } else {
                    updateMessageCell(message);
                }
            }

            private void updateMessageCell(Message message) {
                String displayParticipant;
                String subjectPrefix = "";
                boolean isSentByCurrentUser = (message.getSenderId() == currentUser.getId() &&
                        message.getSenderType().equals(currentUser.getUserType()));

                if (isSentByCurrentUser) {
                    displayParticipant = "To: " + messageService.getUserFullName(message.getReceiverId(), message.getReceiverType());
                    subjectPrefix = "Sent: ";
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #3498db;");
                } else {
                    displayParticipant = "From: " + messageService.getUserFullName(message.getSenderId(), message.getSenderType());
                    subjectPrefix = message.isRead() ? "" : "New: ";
                    setStyle(message.isRead() ? "-fx-font-weight: normal; -fx-text-fill: black;" : "-fx-font-weight: bold; -fx-text-fill: green;");
                }

                if (message.isArchived()) {
                    subjectPrefix = "[Archived] " + subjectPrefix;
                    setStyle(getStyle() + "; -fx-text-fill: gray;");
                }

                if (message.isStarred()) {
                    subjectPrefix = "★ " + subjectPrefix;
                }

                setText(subjectPrefix + message.getSubject() + " - " + displayParticipant +
                        " (" + message.getTimestamp().format(DateTimeFormatter.ofPattern("MMM d, HH:mm")) + ")");
            }
        });

        emailListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                showMessageDetail(newValue);
            }
        });
    }

    private void updateButtonStates() {
        // Reset all button styles first
        resetButtonStyles();

        // Highlight current active view
        Button activeButton = null;
        switch (currentViewType) {
            case "Inbox":
                activeButton = inboxBtn;
                break;
            case "AllMessages":
                activeButton = messageBtn;
                break;
            case "Favorites":
                activeButton = favoriteBtn;
                break;
            case "Archived":
                activeButton = archivedBtn;
                break;
        }

        if (activeButton != null) {
            activeButton.getStyleClass().add("active-btn");
        }
    }

    private void resetButtonStyles() {
        Button[] navButtons = {inboxBtn, messageBtn, favoriteBtn, archivedBtn};
        for (Button btn : navButtons) {
            if (btn != null) {
                btn.getStyleClass().remove("active-btn");
            }
        }
    }

    // ===========================================
    // NAVIGATION BUTTON HANDLERS
    // ===========================================

    @FXML
    private void toggleMenu(ActionEvent event) {
        menuVisible = !menuVisible;
        if (menuBar != null) {
            menuBar.setVisible(menuVisible);
            menuBar.setManaged(menuVisible);
        }
        System.out.println("Menu toggled: " + (menuVisible ? "Visible" : "Hidden"));
    }

    @FXML
    private void openWriteToSomeone(ActionEvent event) {
        try {
            URL fxmlUrl = getClass().getResource("/Fxml/Messages/writeToSomeone.fxml");
            if (fxmlUrl == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "Could not find writeToSomeone.fxml file.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            Contact contactController = loader.getController();
            if (contactController != null) {
                contactController.setComposeCallback(() -> {
                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Message sent successfully!");
                        refreshCurrentMessageList();
                        showMessageList(null);
                    });
                });
            }

            Stage stage = new Stage();
            stage.setTitle("Compose Message");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(getCurrentStage());
            stage.show();

            System.out.println("Opening compose message window...");

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open compose window: " + e.getMessage());
        }
    }

    @FXML
    private void openInbox(ActionEvent event) {
        currentViewType = "Inbox";
        showMessageList(event);

        if (currentUser != null) {
            try {
                List<Message> inboxMessages = messageService.getInboxMessagesForUser(currentUser.getId());
                currentMessages = inboxMessages;
                if (emailListView != null) {
                    emailListView.setItems(FXCollections.observableArrayList(inboxMessages));
                }
                updateButtonStates();
                System.out.println("Loaded " + inboxMessages.size() + " inbox messages");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load inbox messages: " + e.getMessage());
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "User Error", "Current user not set.");
        }
    }

    @FXML
    private void openAllMessages(ActionEvent event) {
        currentViewType = "AllMessages";
        showMessageList(event);

        if (currentUser != null) {
            try {
                List<Message> allMessages = messageService.getAllMessagesForUser(currentUser.getId());
                currentMessages = allMessages;
                if (emailListView != null) {
                    emailListView.setItems(FXCollections.observableArrayList(allMessages));
                }
                updateButtonStates();
                System.out.println("Loaded " + allMessages.size() + " messages (excluding archived)");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load messages: " + e.getMessage());
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "User Error", "Current user not set.");
        }
    }

    @FXML
    private void openFavorite(ActionEvent event) {
        currentViewType = "Favorites";
        showMessageList(event);

        if (currentUser != null) {
            try {
                List<Message> starredMessages = messageService.getStarredMessagesForUser(currentUser.getId());
                currentMessages = starredMessages;
                if (emailListView != null) {
                    emailListView.setItems(FXCollections.observableArrayList(starredMessages));
                }
                updateButtonStates();
                System.out.println("Loaded " + starredMessages.size() + " favorite messages");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load favorite messages: " + e.getMessage());
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "User Error", "Current user not set.");
        }
    }

    @FXML
    private void openArchived(ActionEvent event) {
        currentViewType = "Archived";
        showMessageList(event);

        if (currentUser != null) {
            try {
                List<Message> archivedMessages = messageService.getArchivedMessagesForUser(currentUser.getId());
                currentMessages = archivedMessages;
                if (emailListView != null) {
                    emailListView.setItems(FXCollections.observableArrayList(archivedMessages));
                }
                updateButtonStates();
                System.out.println("Loaded " + archivedMessages.size() + " archived messages");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load archived messages: " + e.getMessage());
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "User Error", "Current user not set.");
        }
    }
    private String getDashboardPathForUserRole(String userRole) {
        switch (userRole.toLowerCase()) {
            case "admin":
                return "/Fxml/Admin/AdminDashboard.fxml";
            case "babysitter":
                return "/Fxml/Babysitters/babysitters.fxml";
            case "parent":
            case "client":
                return "/Fxml/Babysitters/ClientSide.fxml";
            default:
                return "/Fxml/Client/client.fxml";
        }
    }
    private String getWindowTitleForUserRole(String userRole, String userName) {
        String roleDisplayName;
        switch (userRole.toLowerCase()) {
            case "admin":
                roleDisplayName = "Admin Dashboard";
                break;
            case "babysitter":
                roleDisplayName = "Babysitter Dashboard";
                break;
            case "parent":
            case "client":
                roleDisplayName = "Client Dashboard";
                break;
            default:
                roleDisplayName = "Dashboard";
        }
        return roleDisplayName + " - " + userName;
    }
    @FXML
    private void openClientSideEnhanced(ActionEvent event) {
        try {
            User currentUser = SessionManager.getCurrentUser();

            if (currentUser == null) {
                showAlert(Alert.AlertType.ERROR, "Session Error", "No user logged in. Please log in again.");
                return;
            }

            String userRole = currentUser.getUserType();
            String fxmlPath = getDashboardPathForUserRole(userRole);
            String windowTitle = getWindowTitleForUserRole(userRole, currentUser.getName());

            System.out.println("Navigating back to dashboard for user: " + currentUser.getName() +
                    " (" + userRole + ")");

            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                showAlert(Alert.AlertType.WARNING, "Navigation",
                        "Dashboard not found. Feature may not be implemented yet.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            Stage currentStage = getCurrentStage();
            if (currentStage != null) {
                currentStage.getScene().setRoot(root);
                currentStage.setTitle(windowTitle);
                currentStage.centerOnScreen();
            }

            System.out.println("Successfully navigated to: " + windowTitle);

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Could not load dashboard: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Unexpected Error",
                    "Navigation failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void openSettings(ActionEvent event) {
        try {
            URL fxmlUrl = getClass().getResource("/Fxml/Settings/Settings.fxml");
            if (fxmlUrl == null) {
                showAlert(Alert.AlertType.WARNING, "Settings", "Settings window not found. Feature not implemented yet.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Settings");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(getCurrentStage());
            stage.show();

            System.out.println("Opening Settings window...");

        } catch (IOException e) {
            showAlert(Alert.AlertType.WARNING, "Settings", "Could not open settings: " + e.getMessage());
        }
    }

    // ===========================================
    // ACTION BUTTON HANDLERS
    // ===========================================

    @FXML
    private void showMessageList(ActionEvent event) {
        if (messageDetailViewContainer != null) {
            messageDetailViewContainer.setVisible(false);
            messageDetailViewContainer.setManaged(false);
        }

        if (messageListViewContainer != null) {
            messageListViewContainer.setVisible(true);
            messageListViewContainer.setManaged(true);
        }

        // Clear selection when returning to list
        if (emailListView != null) {
            emailListView.getSelectionModel().clearSelection();
        }

        System.out.println("Showing message list view");
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        if (searchInput == null || currentMessages == null) {
            showAlert(Alert.AlertType.WARNING, "Search", "Search not available at this time.");
            return;
        }

        String searchTerm = searchInput.getText().trim();
        if (searchTerm.isEmpty()) {
            // If search is empty, show all current messages
            if (emailListView != null) {
                emailListView.setItems(FXCollections.observableArrayList(currentMessages));
            }
            return;
        }

        try {
            // Filter messages based on search term
            List<Message> filteredMessages = currentMessages.stream()
                    .filter(message ->
                            message.getSubject().toLowerCase().contains(searchTerm.toLowerCase()) ||
                                    message.getContent().toLowerCase().contains(searchTerm.toLowerCase()) ||
                                    messageService.getUserFullName(message.getSenderId(), message.getSenderType())
                                            .toLowerCase().contains(searchTerm.toLowerCase()) ||
                                    messageService.getUserFullName(message.getReceiverId(), message.getReceiverType())
                                            .toLowerCase().contains(searchTerm.toLowerCase())
                    )
                    .collect(Collectors.toList());

            if (emailListView != null) {
                emailListView.setItems(FXCollections.observableArrayList(filteredMessages));
            }

            System.out.println("Search for '" + searchTerm + "' found " + filteredMessages.size() + " results");

            if (filteredMessages.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Search Results",
                        "No messages found matching '" + searchTerm + "'");
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Search Error", "Error performing search: " + e.getMessage());
        }
    }

    // ===========================================
    // UTILITY METHODS
    // ===========================================

    private void refreshCurrentMessageList() {
        switch (currentViewType) {
            case "Inbox":
                openInbox(null);
                break;
            case "AllMessages":
                openAllMessages(null);
                break;
            case "Favorites":
                openFavorite(null);
                break;
            case "Archived":
                openArchived(null);
                break;
            default:
                openInbox(null);
                break;
        }
    }

    private Stage getCurrentStage() {
        if (menuBtn != null && menuBtn.getScene() != null) {
            Window window = menuBtn.getScene().getWindow();
            if (window instanceof Stage) {
                return (Stage) window;
            }
        }
        return null;
    }

    public void showMessageDetail(Message selectedMessage) {
        if (selectedMessage == null) return;

        if (messageListViewContainer != null) {
            messageListViewContainer.setVisible(false);
            messageListViewContainer.setManaged(false);
        }

        if (messageDetailViewContainer != null) {
            messageDetailViewContainer.setVisible(true);
            messageDetailViewContainer.setManaged(true);
            messageDetailViewContainer.getChildren().clear();
        }

        try {
            URL fxmlUrl = getClass().getResource("/Fxml/Messages/MessageDetail.fxml");
            if (fxmlUrl == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "Could not find MessageDetail.fxml file.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            VBox detailViewContent = loader.load();
            MessageDetail messageDetailController = loader.getController();

            if (messageDetailController != null) {
                messageDetailController.setMessage(selectedMessage);
                messageDetailController.setCurrentUser(currentUser);

                messageDetailController.setCallback(new MessageDetail.MessageDetailCallback() {
                    @Override
                    public void onBackToList() {
                        showMessageList(null);
                        refreshCurrentMessageList();
                    }

                    @Override
                    public void onMessageDeleted(int messageId) {
                        if (emailListView != null) {
                            emailListView.getItems().removeIf(m -> m.getId() == messageId);
                        }
                        showMessageList(null);
                        refreshCurrentMessageList();
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Message deleted successfully.");
                    }

                    @Override
                    public void onMessageSent() {
                        showMessageList(null);
                        refreshCurrentMessageList();
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Message sent successfully!");
                    }

                    @Override
                    public void onMessageArchived(int messageId, boolean isArchived) {
                        if (emailListView != null) {
                            Message updatedMessage = emailListView.getItems().stream()
                                    .filter(m -> m.getId() == messageId)
                                    .findFirst()
                                    .orElse(null);
                            if (updatedMessage != null) {
                                updatedMessage.setArchived(isArchived);
                            }
                        }
                        showMessageList(null);
                        refreshCurrentMessageList();
                        showAlert(Alert.AlertType.INFORMATION, "Success",
                                "Message " + (isArchived ? "archived" : "unarchived") + " successfully.");
                    }
                });
            }

            if (messageDetailViewContainer != null) {
                messageDetailViewContainer.getChildren().setAll(detailViewContent);
            }

            // Mark as read if current user is receiver and message is unread
            if (selectedMessage.getReceiverId() == currentUser.getId() && !selectedMessage.isRead()) {
                try {
                    boolean updated = messageService.updateMessageReadStatus(selectedMessage.getId(), true);
                    if (updated) {
                        selectedMessage.setRead(true);
                        if (emailListView != null) {
                            emailListView.refresh();
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Failed to update read status: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load message details: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.initOwner(getCurrentStage());
        alert.showAndWait();
    }

    // Legacy method for backward compatibility
    @FXML
    public void openCloseMenu(ActionEvent actionEvent) {
        toggleMenu(actionEvent);
    }
}