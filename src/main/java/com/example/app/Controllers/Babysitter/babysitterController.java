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
import java.util.Objects;
import java.util.ResourceBundle;

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

    /**
     * Initialize the client data list
     */
    private void initializeClientData() {
        // Create sample client data (in a real app, this would come from a database)
        allClients = FXCollections.observableArrayList(
                new Client("Jakie", "New York", 4.5, 15),
                new Client("Thomas", "Boston", 3.8, 12),
                new Client("Sophie", "Chicago", 4.2, 14),
                new Client("Michel", "Los Angeles", 3.9, 13),
                new Client("Emma", "San Francisco", 4.7, 18),
                new Client("Noah", "Seattle", 4.0, 16),
                new Client("Olivia", "Miami", 4.3, 15),
                new Client("Liam", "Denver", 3.7, 13)
        );

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

    /**
     * Map button handler
     */
    @FXML
    public void openMap(ActionEvent actionEvent) {
        try {
            // Créer des données de clients avec localisation (dans un cas réel, ces données viendraient d'une base de données)
            ObservableList<DashboardController.ClientWithLocation> clientsWithLocation = FXCollections.observableArrayList(
                    new DashboardController.ClientWithLocation("Jakie", "New York", 4.5, 15, 48.8584, 2.2945),
                    new DashboardController.ClientWithLocation("Thomas", "Boston", 3.8, 12, 48.8606, 2.3376),
                    new DashboardController.ClientWithLocation("Sophie", "Chicago", 4.2, 14, 48.8737, 2.2950),
                    new DashboardController.ClientWithLocation("Michel", "Los Angeles", 3.9, 13, 48.8417, 2.3185),
                    new DashboardController.ClientWithLocation("Emma", "San Francisco", 4.7, 18, 48.8697, 2.3080),
                    new DashboardController.ClientWithLocation("Noah", "Seattle", 4.0, 16, 48.8505, 2.3488),
                    new DashboardController.ClientWithLocation("Olivia", "Miami", 4.3, 15, 48.8566, 2.3522),
                    new DashboardController.ClientWithLocation("Liam", "Denver", 3.7, 13, 48.8530, 2.3499)
            );

            // Utiliser directement la classe map existante pour afficher la carte avec les clients
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
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/Fxml/User/Account.fxml")));
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Mon Compte");
            stage.show();

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