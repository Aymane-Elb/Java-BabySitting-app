package com.example.app.Controllers.Client;

import com.example.app.Controllers.Contact.Contact;
import com.example.app.Models.User; // Use the User model for babysitters
// import com.example.app.Models.model; // Remove this if no longer used for session data
import com.example.app.Services.UserService; // To fetch User data (babysitters)
import com.example.app.Services.SessionManager; // For debugging and consistency

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;



public class clientController implements Initializable {
    // Search related
    @FXML public TextField searchInput;
    @FXML public Button searchBtn0;
    @FXML public Button searchBtn1;

    // Navigation buttons
    @FXML public Button mapBtn;
    @FXML public Button messageBtn;
    @FXML public Button accountBtn;

    // Babysitter list - NOW LISTS USER OBJECTS (who are babysitters)
    @FXML private ListView<User> babysitterList;

    private ObservableList<User> allBabysitterUsers;
    private UserService userService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        userService = new UserService();

        initializeBabysitterData();

        setupBabysitterListCellFactory();

        babysitterList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                showBabysitterDetails(newVal);
            }
        });

        System.out.println("Client interface initialized for user: " +
                (SessionManager.isLoggedIn() ? SessionManager.getCurrentUser() : "Unknown (Not logged in)"));
    }

    /**
     * Initialize the babysitter data list by fetching actual 'babysitter' users from the database.
     */
    private void initializeBabysitterData() {
        List<User> babysitters = userService.getUsersByType("babysitter");
        allBabysitterUsers = FXCollections.observableArrayList(babysitters);

        babysitterList.setItems(allBabysitterUsers);

        if (babysitters.isEmpty()) {
            System.out.println("No babysitter users found in the database.");
            // Optionally, show a message to the user
            showAlert("No Babysitters", "No babysitter users are currently registered.");
        }
    }

    /**
     * Setup the cell factory for babysitter list display (now displaying User objects)
     */
    private void setupBabysitterListCellFactory() {
        babysitterList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox content = new VBox(5);
                    content.getStyleClass().add("babysitter-cell");

                    Label nameLabel = new Label("Nom: " + user.getName());
                    nameLabel.getStyleClass().add("babysitter-name");

                    // Use getters from the extended User model
                    String location = (user.getLocation() != null && !user.getLocation().isEmpty()) ? user.getLocation() : "N/A";
                    // Ensure rating and price are not negative if your DB returns defaults as 0.0 or handles nulls
                    double rating = (user.getRating() >= 0) ? user.getRating() : 0.0;
                    double price = (user.getPrice() >= 0) ? user.getPrice() : 0.0;

                    Label locationLabel = new Label("Location: " + location);
                    Label ratingLabel = new Label(String.format("Évaluation: %.1f/5.0", rating));
                    Label priceLabel = new Label("Tarif: " + price + "$/Hr");

                    content.getChildren().addAll(
                            nameLabel,
                            locationLabel,
                            ratingLabel,
                            priceLabel
                    );

                    setGraphic(content);
                }
            }
        });
    }

    /**
     * Search for babysitters based on query (now searching User objects)
     */
    private void searchForBabysitters(String query) {
        if (query == null || query.trim().isEmpty()) {
            babysitterList.setItems(allBabysitterUsers);
            return;
        }

        String lowerCaseQuery = query.toLowerCase().trim();
        ObservableList<User> filteredUsers = allBabysitterUsers.filtered(user ->
                user.getName().toLowerCase().contains(lowerCaseQuery) ||
                        user.getEmail().toLowerCase().contains(lowerCaseQuery) ||
                        (user.getLocation() != null && user.getLocation().toLowerCase().contains(lowerCaseQuery))
        );

        babysitterList.setItems(filteredUsers);
        System.out.println("Found " + filteredUsers.size() + " babysitters matching: " + query);

        if (filteredUsers.isEmpty()) {
            showAlert("Aucun résultat", "Aucun babysitter ne correspond à votre recherche: " + query);
        }
    }

    /**
     * Show detailed information about a selected babysitter (now displaying User details)
     */
    private void showBabysitterDetails(User user) {
        try {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Détails du Babysitter");
            dialog.setHeaderText("Informations sur " + user.getName());

            VBox content = new VBox(10);
            content.getChildren().addAll(
                    new Label("Nom: " + user.getName()),
                    new Label("Email: " + user.getEmail()),
                    new Label("Type: " + user.getUserType())
            );

            // Add new fields from the extended User model
            content.getChildren().addAll(
                    new Label("Location: " + (user.getLocation() != null ? user.getLocation() : "N/A")),
                    new Label(String.format("Évaluation: %.1f/5.0", user.getRating() >= 0 ? user.getRating() : 0.0)),
                    new Label("Tarif: " + (user.getPrice() >= 0 ? user.getPrice() : "N/A") + "$/Hr"),
                    new Label("Distance: 3.2 km"),  // Example fixed data, calculate in real app
                    new Label("Disponibilité: Lundi - Vendredi")  // Example fixed data, fetch in real app
            );

            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

            // Add contact button
            ButtonType contactButtonType = new ButtonType("Contacter", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().add(contactButtonType);

            dialog.showAndWait().ifPresent(response -> {
                if (response == contactButtonType) {
                    openMessagingWithBabysitter(user);
                }
            });

        } catch (Exception e) {
            System.err.println("Error showing babysitter details: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'afficher les détails du babysitter: " + e.getMessage());
        }
    }

    /**
     * Open messaging with a specific babysitter (now receiving User object)
     */
    private void openMessagingWithBabysitter(User babysitterUser) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Messages/writeToSomeone.fxml"));
            Parent root = loader.load();

            Contact contactController = loader.getController();
            if (contactController != null) {
                contactController.prepareNewMessage(babysitterUser);
            }

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Message à " + babysitterUser.getName());
            stage.show();

            Stage currentStage = (Stage) mapBtn.getScene().getWindow(); // Use any FXML element on current scene
            currentStage.close();

        } catch (IOException e) {
            System.err.println("Error opening messaging: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la messagerie: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void onSearchBtn0(ActionEvent actionEvent) {
        String query = searchInput.getText();
        searchForBabysitters(query);
    }

    @FXML
    public void onSearchBtn1(ActionEvent actionEvent) {
        searchInput.requestFocus();
        if (!searchInput.getText().isEmpty()) {
            searchForBabysitters(searchInput.getText());
        }
    }

    @FXML
    public void openMap(ActionEvent actionEvent) {
        try {
            // This is using the dummy data. If you have real coordinates in User model,
            // you should adapt this to use those.
            ObservableList<DashboardController.ClientWithLocation> babysittersWithLocation = FXCollections.observableArrayList(
                    new DashboardController.ClientWithLocation("Jakie", "New York", 4.5, 15, 48.8584, 2.2945),
                    new DashboardController.ClientWithLocation("Thomas", "Boston", 3.8, 12, 48.8606, 2.3376),
                    new DashboardController.ClientWithLocation("Sophie", "Chicago", 4.2, 14, 48.8737, 2.2950),
                    new DashboardController.ClientWithLocation("Michel", "Los Angeles", 3.9, 13, 48.8417, 2.3185),
                    new DashboardController.ClientWithLocation("Emma", "San Francisco", 4.7, 18, 48.8697, 2.3080),
                    new DashboardController.ClientWithLocation("Noah", "Seattle", 4.0, 16, 48.8505, 2.3488),
                    new DashboardController.ClientWithLocation("Olivia", "Miami", 4.3, 15, 48.8566, 2.3522),
                    new DashboardController.ClientWithLocation("Liam", "Denver", 3.7, 13, 48.8530, 2.3499)
            );

            // Assuming 'map' class is accessible and has 'openMapWithClients'
            map mapController = new map(); // This 'map' class needs to be defined or imported
            mapController.openMapWithClients(babysittersWithLocation);

        } catch (Exception e) {
            System.err.println("Error opening map view: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la carte: " + e.getMessage());
        }
    }

    @FXML
    public void onMessageButtonClick(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/Fxml/Messages/writeToSomeone.fxml")));
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Messages");
            stage.show();

            Stage currentStage = (Stage) ((javafx.scene.Node) actionEvent.getSource()).getScene().getWindow();
            currentStage.close();
        } catch (Exception e) {
            System.err.println("Error opening messages: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la messagerie: " + e.getMessage());
        }
    }

    @FXML
    public void openAccontWindow(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/Fxml/User/Account.fxml")));
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Mon Compte");
            stage.show();

            Stage currentStage = (Stage) ((javafx.scene.Node) actionEvent.getSource()).getScene().getWindow();
            currentStage.close();
        } catch (Exception e) {
            System.err.println("Error opening account: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le compte: " + e.getMessage());
        }
    }
}

