package com.example.app.Controllers.Client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.net.URL;

public class map extends Application {

    @Override
    public void start(Stage primaryStage) {
        // 1. Création du WebView et activation de JavaScript
        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();
        webEngine.setJavaScriptEnabled(true);

        // 2. Chargement de map.html depuis src/main/resources/html/map.html
        URL url = getClass().getResource("/html/map.html");
        if (url == null) {
            System.err.println("❌ map.html introuvable dans le classpath !");
            return;
        }
        webEngine.load(url.toExternalForm());

        // 3. Configuration de la scène et affichage
        Scene scene = new Scene(webView, 800, 600);
        primaryStage.setTitle("Test d'affichage de la carte");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        // Lancement de l'application JavaFX
        launch(args);
    }
}
