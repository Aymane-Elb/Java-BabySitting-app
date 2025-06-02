package com.example.app.Controllers.Babysitter;

import com.example.app.Controllers.Client.DashboardController;
import com.example.app.Models.Client;
import com.example.app.Models.model;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.example.app.Controllers.Client.DashboardController.ClientWithLocation;
import com.example.app.Controllers.Client.map;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.ResourceBundle;
import com.example.app.Models.Database;
public class babysitterController implements Initializable {
    // Search related
    @FXML public TextField searchInput;
    @FXML public Button searchBtn0;  // Search button in top bar
    @FXML public Button searchBtn1;  // Search button in bottom nav

    // Navigation buttons
    @FXML public Button mapBtn;
    @FXML public Button messageBtn;
    @FXML public Button accountBtn;

    // Client list
    @FXML private ListView<Client> clientList;

    // Original client data
    private ObservableList<Client> allClients;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize client data
        initializeClientData();

        // Setup client list cell display
        setupClientListCellFactory();

        // Set click listener for client selection
        clientList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                showClientDetails(newVal);
            }
        });

        System.out.println("Babysitter interface initialized for user: " +
                (model.getInstance() != null ? model.getInstance().getUsername() : "Unknown"));
    }
    private double[] geocodeAddress(String address) {
        if (address == null) return null;

        String lowerAddress = address.toLowerCase();

        // Morocco locations (since you're in Casablanca)
        if (lowerAddress.contains("karama") || lowerAddress.contains("hay karama")) {
            return new double[]{33.5731, -7.5898}; // Casablanca - Hay Karama area
        }
        if (lowerAddress.contains("casablanca")) {
            return new double[]{33.5731, -7.5898}; // Casablanca center
        }
        if (lowerAddress.contains("rabat")) {
            return new double[]{34.0209, -6.8416};
        }
        if (lowerAddress.contains("morocco") || lowerAddress.contains("maroc")) {
            return new double[]{33.5731, -7.5898}; // Default to Casablanca
        }

        // International locations (for your test data)
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

        // Default location if no match found (Casablanca)
        return new double[]{33.5731, -7.5898};
    }
    private double[] parseOrGeocodeLocation(String location, String address) {
        // First try to parse if location contains coordinates (format: "lat,lng")
        if (location != null && location.contains(",")) {
            try {
                String[] parts = location.split(",");
                if (parts.length == 2) {
                    double lat = Double.parseDouble(parts[0].trim());
                    double lng = Double.parseDouble(parts[1].trim());
                    return new double[]{lat, lng};
                }
            } catch (NumberFormatException e) {
                // Location string is not coordinates, continue to geocoding
            }
        }

        // If location parsing failed, try geocoding the address
        return geocodeAddress(address != null ? address : location);
    }
    private ObservableList<Client> fetchClientsFromDatabaseForList() {
        ObservableList<Client> clients = FXCollections.observableArrayList();

        String query = """
        SELECT username, address, rating, price 
        FROM users 
        WHERE user_type = 'client' AND is_active = true
        ORDER BY rating DESC
        """;

        try (Connection connection = Database.connect();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String username = resultSet.getString("username");
                String address = resultSet.getString("address");
                double rating = resultSet.getDouble("rating");
                double price = resultSet.getDouble("price");

                clients.add(new Client(
                        username,
                        address != null ? address : "Non spécifiée",
                        rating,
                        price
                ));
            }

            // If no clients found in database, add some default ones for testing
            if (clients.isEmpty()) {
                System.out.println("No clients found in database, using sample data");
                clients.addAll(
                        new Client("yamen", "karama II", 0.0, 0.0),
                        new Client("aymane", "HAY Karama II", 0.0, 0.0)
                );
            }

        } catch (SQLException e) {
            System.err.println("Database error while fetching clients for list: " + e.getMessage());
            e.printStackTrace();

            // Fallback to sample data if database error
            clients.addAll(
                    new Client("yamen", "karama II", 0.0, 0.0),
                    new Client("aymane", "HAY Karama II", 0.0, 0.0)
            );
        }

        return clients;
    }
    /**
     * Initialize the client data list
     */
    private void initializeClientData() {
        // Fetch real client data from database
        allClients = fetchClientsFromDatabaseForList();

        // Set the items in the list view
        clientList.setItems(allClients);
    }


    /**
     * Setup the cell factory for client list display
     */
    private void setupClientListCellFactory() {
        clientList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Client client, boolean empty) {
                super.updateItem(client, empty);
                if (empty || client == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox content = new VBox(5); // 5px spacing between elements
                    content.getStyleClass().add("client-cell");

                    Label nameLabel = new Label("Nom: " + client.getName());
                    nameLabel.getStyleClass().add("client-name");

                    Label locationLabel = new Label("Location: " + client.getLocation());
                    Label ratingLabel = new Label(String.format("Évaluation: %.1f/5.0", client.getRating()));
                    Label priceLabel = new Label("Tarif: " + client.getPrice() + "$/Hr");

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
     * Search for clients based on query
     */
    private void searchForClients(String query) {
        if (query == null || query.trim().isEmpty()) {
            // If query is empty, show all clients
            clientList.setItems(allClients);
            return;
        }

        // Filter clients based on name or location
        String lowerCaseQuery = query.toLowerCase().trim();
        ObservableList<Client> filteredClients = allClients.filtered(client ->
                client.getName().toLowerCase().contains(lowerCaseQuery) ||
                        client.getLocation().toLowerCase().contains(lowerCaseQuery)
        );

        // Update the list view
        clientList.setItems(filteredClients);
        System.out.println("Found " + filteredClients.size() + " clients matching: " + query);

        // Show notification if no results found
        if (filteredClients.isEmpty()) {
            showAlert("Aucun résultat", "Aucun client ne correspond à votre recherche: " + query);
        }
    }

    /**
     * Show detailed information about a selected client
     */
    private void showClientDetails(Client client) {
        try {
            // Create a dialog to show client details
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Détails du client");
            dialog.setHeaderText("Informations sur " + client.getName());

            // Create content
            VBox content = new VBox(10);
            content.getChildren().addAll(
                    new Label("Nom: " + client.getName()),
                    new Label("Location: " + client.getLocation()),
                    new Label(String.format("Évaluation: %.1f/5.0", client.getRating())),
                    new Label("Tarif: " + client.getPrice() + "$/Hr"),
                    new Label("Distance: 3.2 km"),  // Example fixed data
                    new Label("Disponibilité: Lundi - Vendredi")  // Example fixed data
            );

            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

            // Add contact button
            ButtonType contactButtonType = new ButtonType("Contacter", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().add(contactButtonType);

            // Show dialog and handle result
            dialog.showAndWait().ifPresent(response -> {
                if (response == contactButtonType) {
                    openMessagingWithClient(client);
                }
            });

        } catch (Exception e) {
            System.err.println("Error showing client details: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Open messaging with a specific client
     */
    private void openMessagingWithClient(Client client) {
        try {
            // Ensure this path matches the actual FXML file name and location EXACTLY
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Messages/writeToSomeone.fxml"));
            Parent root = loader.load();

            // ... rest of your code
        } catch (IOException e) {
            System.err.println("Error opening messaging: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la messagerie: " + e.getMessage());
        }
    }

    /**
     * Show a simple alert dialog
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Search button in top bar handler
     */
    @FXML
    public void onSearchBtn0(ActionEvent actionEvent) {
        String query = searchInput.getText();
        searchForClients(query);
    }

    /**
     * Search button in bottom navigation handler
     */
    @FXML
    public void onSearchBtn1(ActionEvent actionEvent) {
        // Focus the search field and show client list
        searchInput.requestFocus();

        // If there's already text, perform the search
        if (!searchInput.getText().isEmpty()) {
            searchForClients(searchInput.getText());
        }
    }
    private ObservableList<DashboardController.ClientWithLocation> fetchClientsFromDatabase() {
        ObservableList<DashboardController.ClientWithLocation> clientsWithLocation = FXCollections.observableArrayList();

        String query = """
        SELECT username, address, rating, price, location 
        FROM users 
        WHERE user_type = 'client' AND is_active = true
        """;

        try (Connection connection = Database.connect();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String username = resultSet.getString("username");
                String address = resultSet.getString("address");
                double rating = resultSet.getDouble("rating");
                double price = resultSet.getDouble("price");
                String location = resultSet.getString("location");

                // Parse coordinates from location string or geocode address
                double[] coordinates = parseOrGeocodeLocation(location, address);

                if (coordinates != null) {
                    clientsWithLocation.add(new DashboardController.ClientWithLocation(
                            username,
                            address != null ? address : "Non spécifiée",
                            rating,
                            price,
                            coordinates[0], // latitude
                            coordinates[1]  // longitude
                    ));
                }
            }

        } catch (SQLException e) {
            System.err.println("Database error while fetching clients: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur Base de Données", "Erreur lors de la récupération des clients: " + e.getMessage());
        }

        return clientsWithLocation;
    }


    /**
     * Map button handler
     */
    @FXML
    public void openMap(ActionEvent actionEvent) {
        try {
            // Fetch real client data from database
            ObservableList<DashboardController.ClientWithLocation> clientsWithLocation = fetchClientsFromDatabase();

            if (clientsWithLocation.isEmpty()) {
                showAlert("Information", "Aucun client trouvé dans la base de données.");
                return;
            }

            // Use the existing map class to display the map with real clients
            map mapController = new map();
            mapController.openMapWithClients(clientsWithLocation);

        } catch (Exception e) {
            System.err.println("Error opening map view: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la carte: " + e.getMessage());
        }
    }

    /**
     * Message button handler*/
     @FXML
     public void onMessageButtonClick(ActionEvent actionEvent) {
     try {
     // Ensure this path matches the actual FXML file name and location EXACTLY
     Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/Fxml/Messages/archive.fxml")));
     Stage stage = new Stage();
     stage.setScene(new Scene(root));
     stage.setTitle("Messages");
     stage.show();

     // Close current window
     Stage currentStage = (Stage) ((javafx.scene.Node) actionEvent.getSource()).getScene().getWindow();
     currentStage.close();
     } catch (Exception e) {
     System.err.println("Error opening messages: " + e.getMessage());
     e.printStackTrace();
     showAlert("Erreur", "Impossible d'ouvrir la messagerie: " + e.getMessage());
     }
     }
    /**
     * Account button handler
     */
    @FXML
    public void openAccontWindow(ActionEvent actionEvent) {
        try {
            // Close current window
            Stage currentStage = (Stage) ((javafx.scene.Node) actionEvent.getSource()).getScene().getWindow();
            currentStage.close();
        } catch (Exception e) {
            System.err.println("Error opening account: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le compte: " + e.getMessage());
        }
    }
}