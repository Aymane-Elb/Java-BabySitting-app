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
            dialog.setHeaderText("Information sur " + user.getName());

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
            // Fetch real babysitter data from database and convert to map format
            ObservableList<DashboardController.ClientWithLocation> babysittersWithLocation = fetchBabysittersFromDatabaseForMap();

            if (babysittersWithLocation.isEmpty()) {
                showAlert("Information", "Aucun babysitter trouvé dans la base de données.");
                return;
            }

            // Use the existing map class to display the map with real babysitters
            map mapController = new map();
            mapController.openMapWithClients(babysittersWithLocation);

        } catch (Exception e) {
            System.err.println("Error opening map view: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la carte: " + e.getMessage());
        }
    }
    private double[] getDefaultCoordinates() {
        return new double[]{33.5731, -7.5898}; // Casablanca center
    }
    public void refreshBabysitterData() {
        try {
            System.out.println("Refreshing babysitter data from database...");

            // Re-fetch babysitters from database
            List<User> babysitters = userService.getUsersByType("babysitter");
            allBabysitterUsers.clear();
            allBabysitterUsers.addAll(babysitters);

            // Update the list view
            babysitterList.setItems(allBabysitterUsers);

            System.out.println("Refreshed " + babysitters.size() + " babysitters from database");

            if (babysitters.isEmpty()) {
                showAlert("Information", "Aucun babysitter trouvé dans la base de données.");
            }

        } catch (Exception e) {
            System.err.println("Error refreshing babysitter data: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la mise à jour des données: " + e.getMessage());
        }
    }
    public void updateUserCoordinatesInDatabase() {
        try {
            List<User> allUsers = userService.getAllUsers();

            for (User user : allUsers) {
                double[] coordinates = geocodeAddress(user.getAddress(), user.getName());
                if (coordinates != null) {
                    String locationCoordinates = coordinates[0] + "," + coordinates[1];

                    // Update the user's location field with coordinates
                    // You'll need to add this method to your UserService
                    userService.updateUserLocation(user.getId(), locationCoordinates);

                    System.out.println("Updated coordinates for user " + user.getName() +
                            ": " + locationCoordinates);
                }
            }

            System.out.println("Finished updating user coordinates in database");

        } catch (Exception e) {
            System.err.println("Error updating user coordinates: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private double[] geocodeAddress(String address, String userName) {
        if (address == null || address.trim().isEmpty()) {
            System.out.println("No address available for user: " + userName);
            return getDefaultCoordinates(); // Return default coordinates
        }

        String lowerAddress = address.toLowerCase().trim();
        System.out.println("Geocoding address: '" + address + "' for user: " + userName);

        // Morocco locations (since you're in Casablanca)
        if (lowerAddress.contains("karama") || lowerAddress.contains("hay karama")) {
            return new double[]{33.5731, -7.5898}; // Casablanca - Hay Karama area
        }
        if (lowerAddress.contains("casablanca") || lowerAddress.contains("casa")) {
            return new double[]{33.5731, -7.5898}; // Casablanca center
        }
        if (lowerAddress.contains("rabat")) {
            return new double[]{34.0209, -6.8416}; // Rabat
        }
        if (lowerAddress.contains("fez") || lowerAddress.contains("fès")) {
            return new double[]{34.0181, -5.0078}; // Fez
        }
        if (lowerAddress.contains("marrakech") || lowerAddress.contains("marrakesh")) {
            return new double[]{31.6295, -7.9811}; // Marrakech
        }
        if (lowerAddress.contains("tangier") || lowerAddress.contains("tanger")) {
            return new double[]{35.7595, -5.8340}; // Tangier
        }
        if (lowerAddress.contains("agadir")) {
            return new double[]{30.4278, -9.5981}; // Agadir
        }
        if (lowerAddress.contains("meknes") || lowerAddress.contains("meknès")) {
            return new double[]{33.8935, -5.5473}; // Meknes
        }
        if (lowerAddress.contains("oujda")) {
            return new double[]{34.6814, -1.9086}; // Oujda
        }
        if (lowerAddress.contains("tetouan") || lowerAddress.contains("tétouan")) {
            return new double[]{35.5889, -5.3626}; // Tetouan
        }
        if (lowerAddress.contains("morocco") || lowerAddress.contains("maroc")) {
            return new double[]{33.5731, -7.5898}; // Default to Casablanca
        }

        // International locations (for test data)
        if (lowerAddress.contains("paris")) {
            return new double[]{48.8566, 2.3522};
        }
        if (lowerAddress.contains("new york")) {
            return new double[]{40.7128, -74.0060};
        }
        if (lowerAddress.contains("boston")) {
            return new double[]{42.3601, -71.0589};
        }
        if (lowerAddress.contains("chicago")) {
            return new double[]{41.8781, -87.6298};
        }
        if (lowerAddress.contains("los angeles")) {
            return new double[]{34.0522, -118.2437};
        }
        if (lowerAddress.contains("san francisco")) {
            return new double[]{37.7749, -122.4194};
        }
        if (lowerAddress.contains("seattle")) {
            return new double[]{47.6062, -122.3321};
        }
        if (lowerAddress.contains("miami")) {
            return new double[]{25.7617, -80.1918};
        }
        if (lowerAddress.contains("denver")) {
            return new double[]{39.7392, -104.9903};
        }
        if (lowerAddress.contains("london")) {
            return new double[]{51.5074, -0.1278};
        }
        if (lowerAddress.contains("madrid")) {
            return new double[]{40.4168, -3.7038};
        }
        if (lowerAddress.contains("rome")) {
            return new double[]{41.9028, 12.4964};
        }
        if (lowerAddress.contains("berlin")) {
            return new double[]{52.5200, 13.4050};
        }

        // Default location if no match found (Casablanca)
        System.out.println("No specific match found for address: '" + address + "', using default coordinates");
        return getDefaultCoordinates();
    }
    private double[] parseOrGeocodeUserLocation(User user) {
        // First try to parse if location field contains coordinates (format: "lat,lng")
        if (user.getLocation() != null && user.getLocation().contains(",")) {
            try {
                String[] parts = user.getLocation().split(",");
                if (parts.length == 2) {
                    double lat = Double.parseDouble(parts[0].trim());
                    double lng = Double.parseDouble(parts[1].trim());
                    return new double[]{lat, lng};
                }
            } catch (NumberFormatException e) {
                // Location string is not coordinates, continue to geocoding
                System.out.println("Location field is not coordinates for user: " + user.getName());
            }
        }

        // If location parsing failed, try geocoding the address
        String addressToGeocode = user.getAddress() != null ? user.getAddress() : user.getLocation();
        return geocodeAddress(addressToGeocode, user.getName());
    }

    private ObservableList<DashboardController.ClientWithLocation> fetchBabysittersFromDatabaseForMap() {
        ObservableList<DashboardController.ClientWithLocation> babysittersWithLocation = FXCollections.observableArrayList();

        try {
            // Use the existing UserService to get babysitters
            List<User> babysitters = userService.getUsersByType("babysitter");

            for (User babysitter : babysitters) {
                // Skip inactive babysitters
                if (!babysitter.isActive()) {
                    continue;
                }

                // Parse coordinates from user's location or address
                double[] coordinates = parseOrGeocodeUserLocation(babysitter);

                if (coordinates != null) {
                    babysittersWithLocation.add(new DashboardController.ClientWithLocation(
                            babysitter.getName(),
                            babysitter.getAddress() != null ? babysitter.getAddress() : "Non spécifiée",
                            babysitter.getRating() >= 0 ? babysitter.getRating() : 0.0,
                            babysitter.getPrice() >= 0 ? babysitter.getPrice() : 0.0,
                            coordinates[0], // latitude
                            coordinates[1]  // longitude
                    ));

                    System.out.println("Added babysitter to map: " + babysitter.getName() +
                            " at coordinates [" + coordinates[0] + ", " + coordinates[1] + "]");
                } else {
                    System.out.println("Could not determine coordinates for babysitter: " + babysitter.getName());
                }
            }

            System.out.println("Total babysitters added to map: " + babysittersWithLocation.size());

        } catch (Exception e) {
            System.err.println("Error fetching babysitters for map: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur Base de Données", "Erreur lors de la récupération des babysitters: " + e.getMessage());
        }

        return babysittersWithLocation;
    }



    @FXML
    public void onMessageButtonClick(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/Fxml/Messages/archive.fxml")));
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

            Stage currentStage = (Stage) ((javafx.scene.Node) actionEvent.getSource()).getScene().getWindow();
            currentStage.close();
        } catch (Exception e) {
            System.err.println("Error closing account: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le compte: " + e.getMessage());
        }
    }
}

