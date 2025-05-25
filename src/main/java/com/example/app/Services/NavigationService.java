/*package com.example.app.Services;

import com.example.app.Models.UserRole;
import com.example.app.Models.UserSession;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service responsable de la navigation entre les différentes vues de l'application
 */
/*
public class NavigationService {

    private static final Logger LOGGER = Logger.getLogger(NavigationService.class.getName());

    // Mapping des rôles vers les chemins FXML
    private final Map<UserRole, String> roleToFxmlMap;

    /**
     * Constructeur qui initialise le mapping des rôles vers les fichiers FXML
     */
/*
    public NavigationService() {
        roleToFxmlMap = new HashMap<>();
        // Initialiser le mapping des rôles vers les chemins FXML
        roleToFxmlMap.put(UserRole.ADMIN, "/Fxml/Admin/AdminDashboard.fxml");
        roleToFxmlMap.put(UserRole.BABYSITTER, "/Fxml/Babysitters/babysitters.fxml");
        roleToFxmlMap.put(UserRole.PARENT, "/Fxml/Babysitters/ClientSide.fxml");
        roleToFxmlMap.put(UserRole.CLIENT, "/Fxml/Babysitters/ClientSide.fxml");
        // Vue par défaut
        roleToFxmlMap.put(UserRole.UNKNOWN, "/Fxml/Babysitters/ClientSide.fxml");
    }

    /**
     * Redirige l'utilisateur vers l'interface correspondant à son rôle
     *
     * @param sourceNode Un nœud de la scène actuelle pour accéder à la fenêtre
     */
/*
    public void redirectBasedOnUserRole(Node sourceNode) {
        try {
            UserRole role = UserRole.fromString(UserSession.getInstance().getUserRole());
            String fxmlPath = getFxmlPathForRole(role);

            LOGGER.info("Redirection vers: " + fxmlPath + " pour le rôle: " + role);
            loadFXML(fxmlPath, sourceNode);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la redirection", e);
            throw new RuntimeException("Erreur lors de la redirection: " + e.getMessage(), e);
        }
    }

    /**
     * Obtient le chemin FXML correspondant au rôle de l'utilisateur
     *
     * @param role Le rôle de l'utilisateur
     * @return Le chemin du fichier FXML associé
     */
/*
    private String getFxmlPathForRole(UserRole role) {
        return roleToFxmlMap.getOrDefault(role, roleToFxmlMap.get(UserRole.UNKNOWN));
    }

    /**
     * Charge un fichier FXML et l'affiche dans la fenêtre actuelle
     *
     * @param fxmlPath Chemin vers le fichier FXML
     * @param sourceNode Un nœud de la scène actuelle pour accéder à la fenêtre
     * @throws IOException En cas d'erreur lors du chargement du fichier FXML
     */
/*
    private void loadFXML(String fxmlPath, Node sourceNode) throws IOException {
        LOGGER.info("Chargement de l'interface: " + fxmlPath);

        // Utilisation du chargement avec classe de contrôleur pour une meilleure séparation
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        // Configuration de la scène
        Stage stage = (Stage) sourceNode.getScene().getWindow();
        Scene scene = new Scene(root);

        // Configurer le titre avec les informations utilisateur
        String username = UserSession.getInstance().getUsername();
        String role = UserSession.getInstance().getUserRole();
        stage.setTitle("Application - " + username + " (" + role + ")");

        // Appliquer la nouvelle scène
        stage.setScene(scene);
        stage.centerOnScreen();

        LOGGER.info("Interface chargée avec succès pour: " + username);
    }

    /**
     * Navigation vers une nouvelle vue
     *
     * @param fxmlPath Chemin vers le fichier FXML
     * @param title Titre de la nouvelle fenêtre
     * @param sourcePane Un panneau de la scène actuelle pour accéder à la fenêtre
     * @throws IOException En cas d'erreur lors du chargement du fichier FXML
     */
/*
    public void navigateToView(String fxmlPath, String title, Pane sourcePane) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        Stage newStage = new Stage();
        newStage.setScene(new Scene(root));
        newStage.setTitle(title);
        newStage.show();

        // Fermer la fenêtre actuelle
        Stage currentStage = (Stage) sourcePane.getScene().getWindow();
        currentStage.close();
    }
}*/