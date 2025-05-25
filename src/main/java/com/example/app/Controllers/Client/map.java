package com.example.app.Controllers.Client;

import com.example.app.Controllers.Client.DashboardController.ClientWithLocation;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class map {

    // Le début du HTML de la carte Leaflet (sans les marqueurs)
    private final String leafletMapHtmlStart = "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "    <meta charset=\"utf-8\" />\n" +
            "    <title>Leaflet Map</title>\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
            "    <link rel=\"stylesheet\" href=\"https://unpkg.com/leaflet@1.9.4/dist/leaflet.css\"\n" +
            "          integrity=\"sha256-p4NxAoJBhIIN+hmNHrzRCf9tD/miZyoHS5obTRR9BMY=\"\n" +
            "          crossorigin=\"\"/>\n" +
            "    <style>\n" +
            "        #map {\n" +
            "            height: 100vh;\n" +
            "            width: 100%;\n" +
            "        }\n" +
            "        body, html {\n" +
            "            margin: 0;\n" +
            "            padding: 0;\n" +
            "        }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "<div id=\"map\"></div>\n" +
            "<script src=\"https://unpkg.com/leaflet@1.9.4/dist/leaflet.js\"\n" +
            "        integrity=\"sha256-20nQCchB9co0qIjJZRGuk2/Z9VM+kNiyxNV1lvTlZBo=\"\n" +
            "        crossorigin=\"\"></script>\n" +
            "<script>\n" +
            "    var map = L.map('map').setView([48.8584, 2.2945], 13); // Eiffel Tower coords\n" +
            "    \n" +
            "    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {\n" +
            "        attribution: '© OpenStreetMap contributors'\n" +
            "    }).addTo(map);\n";

    // La fin du HTML de la carte Leaflet
    private final String leafletMapHtmlEnd = "</script>\n" +
            "</body>\n" +
            "</html>";

    // Le HTML complet par défaut (avec un marqueur pour la Tour Eiffel)
    private String getDefaultLeafletMapHtml() {
        return leafletMapHtmlStart +
                "    L.marker([48.8584, 2.2945]).addTo(map)\n" +
                "        .bindPopup('Eiffel Tower')\n" +
                "        .openPopup();\n" +
                leafletMapHtmlEnd;
    }

    // Générer le HTML avec des marqueurs pour les clients
    private String generateLeafletMapHtmlWithClients(List<ClientWithLocation> clients) {
        StringBuilder markersCode = new StringBuilder();

        // Ajouter un marqueur pour chaque client
        for (ClientWithLocation client : clients) {
            String markerCode = String.format(
                    "    L.marker([%f, %f]).addTo(map)\n" +
                            "        .bindPopup('%s<br>%s<br>Note: %.1f/5.0<br>Prix: %s$/Hr')\n" +
                            "        .openPopup();\n",
                    client.getLatitude(), client.getLongitude(),
                    client.getName(), client.getLocation(),
                    client.getRating(), client.getPrice()
            );
            markersCode.append(markerCode);
        }

        return leafletMapHtmlStart + markersCode.toString() + leafletMapHtmlEnd;
    }

    /**
     * Ouvre la carte dans une nouvelle fenêtre
     */
    public void openMap() {
        openMapWithClients(null);
    }

    /**
     * Ouvre la carte dans une nouvelle fenêtre avec les clients
     */
    public void openMapWithClients(List<ClientWithLocation> clients) {
        try {
            // Créer une nouvelle fenêtre
            Stage mapStage = new Stage();
            mapStage.setTitle("Carte des Clients");

            // Créer un WebView pour afficher la carte
            WebView webView = new WebView();
            WebEngine webEngine = webView.getEngine();

            // Déterminer le HTML à charger
            String htmlToLoad;
            if (clients != null && !clients.isEmpty()) {
                htmlToLoad = generateLeafletMapHtmlWithClients(clients);
            } else {
                htmlToLoad = getDefaultLeafletMapHtml();
            }

            // Charger le HTML de la carte
            webEngine.loadContent(htmlToLoad);

            // Créer la scène et l'ajouter à la fenêtre
            Scene scene = new Scene(webView, 800, 600);
            mapStage.setScene(scene);

            // Afficher la fenêtre
            mapStage.show();

            System.out.println("Carte ouverte avec succès");

        } catch (Exception e) {
            System.err.println("Erreur lors de l'ouverture de la carte: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Alternative: Ouvre la carte à partir d'un fichier FXML
     * Utile si vous préférez définir l'interface avec un fichier FXML
     */
    public void openMapFromFXML() {
        try {
            // Charger le fichier FXML de la carte
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Dashboard.fxml"));
            Parent root = loader.load();

            // Obtenir le contrôleur de la carte (si nécessaire)
            // MapViewController mapController = loader.getController();

            // Créer une nouvelle fenêtre pour la carte
            Stage mapStage = new Stage();
            mapStage.setTitle("Carte des Clients");
            mapStage.setScene(new Scene(root, 800, 600));
            mapStage.show();

        } catch (IOException e) {
            System.err.println("Erreur lors de l'ouverture du fichier FXML de la carte: " + e.getMessage());
            e.printStackTrace();
        }
    }
}