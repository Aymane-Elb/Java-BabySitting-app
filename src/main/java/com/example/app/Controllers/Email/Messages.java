package com.example.app.Controllers.Email;

import com.example.app.Models.Message; // Import your Message model
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable; // Import Initializable
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.time.format.DateTimeFormatter; // For formatting timestamp
import java.util.Objects;
import java.util.ResourceBundle;
import java.time.LocalDateTime; // For dummy data

// --- IMPORTANT NOTE ---
// The existence of 'Messages.java' alongside 'MessageList.java' (in Controllers.Contact)
// and 'EmailController.java' (which also seems to handle message lists) indicates
// potential redundancy or architectural confusion.
// It is highly recommended to consolidate message listing/viewing functionality
// into fewer, more clearly defined controllers (e.g., MessageListController and MessageDetailController).
// If 'Messages.java' is intended as a simple inbox overview, consider renaming it
// or integrating its functionality into EmailController or MessageList.

public class Messages implements Initializable { // Implements Initializable

    @FXML public Button menuBtn;
    @FXML public Button writeBtn;
    @FXML public Button messageBtn;
    @FXML public Button favoriteBtn;
    @FXML public Button homeBtn;
    @FXML public Button settingBtn;
    @FXML public Button searchBtn;
    @FXML public FontIcon menuBrtn; // Corrected typo from menuBrtn to menuBtn if it's the graphic for menuBtn
    @FXML public Button inboxBtn;
    @FXML public TextField searchInput;
    @FXML public VBox btnContainer; // This is the VBox that expands/collapses

    // Changed ListView to use your Message model instead of a custom inboxEmail
    @FXML private ListView<Message> emailListView;
    private boolean isToggled = false;

    // --- Deprecate or refactor inboxEmail if Message is the main model ---
    // If you intend to use the Message model for all messages, then this class might be redundant.
    // If 'inboxEmail' is a distinct simplified view model, keep it, but ensure conversion from Message.
    // For now, I'm adapting to use 'Message'.
    // public static class inboxEmail { // Consider moving this to a specific ViewModel package if needed
    //     private String sender;
    //     private String subject;
    //     private String snippet;
    //     private String date; // Changed to String, but LocalDateTime from Message is better
    //     // Constructor, getters, etc.
    // }


    @Override // Correct method signature for Initializable
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Load dummy data using the Message model
        emailListView.setItems(FXCollections.observableArrayList(
                new Message(1, 100, 1, "client", "babysitter", "Oracle University", "Start a new discussion. Check the latest news on APEX.", LocalDateTime.now().minusDays(1), false),
                new Message(2, 101, 1, "babysitter", "client", "Printify", "Here's where you should sell. Find your niche in a few steps", LocalDateTime.now().minusDays(4), true)
        ));

        // Customize cells to display Message objects
        emailListView.setCellFactory(list -> new ListCell<Message>() {
            @Override
            protected void updateItem(Message message, boolean empty) {
                super.updateItem(message, empty);
                if (empty || message == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox content = new VBox();
                    content.getChildren().addAll(
                            new Label("📧 " + getUserName(message.getSenderId(), message.getSenderType()) + " - " + message.getSubject()),
                            new Label(message.getContent().substring(0, Math.min(message.getContent().length(), 50)) + "..."), // Snippet
                            new Label(message.getTimestamp().format(DateTimeFormatter.ofPattern("MMM dd"))) // Formatted date
                    );
                    if (!message.isRead()) {
                        content.setStyle("-fx-font-weight: bold;"); // Unread messages in bold
                    }
                    setGraphic(content);
                }
            }
        });

        // Add action handlers if needed here or in the FXML
        // Example: menuBtn.setOnAction(this::openCloseMenu);
        // ... other button actions if this controller handles them
    }

    // Helper method for dummy user name - replace with actual UserService call if needed
    private String getUserName(int userId, String userType) {
        // In a real application, you'd fetch user names from your UserService or a cache
        // For now, simple placeholder
        return userType + " User " + userId;
    }


    // Fixed the incomplete method body
    @FXML // Assuming this method is called from FXML onAction
    public void openCloseMenu(ActionEvent actionEvent) {
        // Implement your menu toggle logic here, similar to EmailController
        isToggled = !isToggled;
        if (isToggled) {
            btnContainer.setPrefWidth(200); // Example expanded width
            // Logic to show text/expand content
        } else {
            btnContainer.setPrefWidth(50); // Example collapsed width
            // Logic to hide text/collapse content
        }
        System.out.println("Menu toggled: " + (isToggled ? "Open" : "Closed"));
    }

    // You might also want methods for opening write, inbox, etc. as seen in EmailController
    // @FXML
    // public void openWriteBtn(ActionEvent event) { /* ... */ }
    // @FXML
    // public void openInboxBtn(ActionEvent event) { /* ... */ }
    // etc.
}