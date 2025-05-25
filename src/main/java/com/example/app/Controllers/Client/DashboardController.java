package com.example.app.Controllers.Client;

import com.example.app.Models.Client;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

import java.util.Arrays;

public class DashboardController {

    @FXML
    private Button mapButton;

    private map mapController = new map();

    // Exemple de données clients avec coordonnées géographiques
    private ObservableList<ClientWithLocation> clients = FXCollections.observableArrayList(
            new ClientWithLocation("Jakie", "New York", 4.5, 15, 48.8584, 2.2945),
            new ClientWithLocation("Thomas", "Boston", 3.8, 12, 48.8606, 2.3376),
            new ClientWithLocation("Sophie", "Chicago", 4.2, 14, 48.8737, 2.2950),
            new ClientWithLocation("Michel", "Los Angeles", 3.9, 13, 48.8417, 2.3185)
    );

    @FXML
    public void initialize() {
        // Any initialization code can go here
    }

    @FXML
    public void openMap(ActionEvent event) {
        // Passer la liste des clients à la carte
        mapController.openMapWithClients(clients);
    }

    // Classe étendue pour ajouter des coordonnées géographiques aux clients
    public static class ClientWithLocation extends Client {
        private double latitude;
        private double longitude;

        public ClientWithLocation(String name, String location, double rating, double price, double latitude, double longitude) {
            super(name, location, rating, price);
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }
    }
}