package com.example.app.Controllers.User;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.util.Duration;

import com.example.app.Models.User;
import com.example.app.Services.SessionManager;
import com.example.app.Services.UserService;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

public class loginController implements Initializable {

    public Label pasDeComptLabel;
    @FXML private StackPane rootPane;
    @FXML private TextField emailInput;
    @FXML private PasswordField passwdInput;
    @FXML private Label errorLabel;
    @FXML private Button submitBtn;
    @FXML private Button createAccountBtn;
    @FXML private Hyperlink forgotPasswordLink;

    private UserService userService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userService = new UserService();
        setupEventHandlers();
        errorLabel.setText("");
        emailInput.setOnKeyPressed(this::handleEnterKey);
        passwdInput.setOnKeyPressed(this::handleEnterKey);
    }

    private void setupEventHandlers() {
        submitBtn.setOnAction(event -> authenticateUser());
        createAccountBtn.setOnAction(this::openRegister);
        forgotPasswordLink.setOnAction(this::handleForgotPassword);
    }

    private void handleEnterKey(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            authenticateUser();
        }
    }

    /**
     * Authenticates the user based on provided email and password.
     */
    private void authenticateUser() {
        String email = emailInput.getText().trim();
        String password = passwdInput.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs.");
            return;
        }
        if (!isValidEmail(email)) {
            showError("Format d'email invalide.");
            return;
        }

        // Use UserService for authentication
        Optional<User> authenticatedUser = userService.authenticateUser(email, password);

        if (authenticatedUser.isPresent()) {
            User user = authenticatedUser.get();
            // *** CRITICAL CHANGE: Set the user in SessionManager ***
            SessionManager.setCurrentUser(user);
            System.out.println("DEBUG: User logged in and set in SessionManager: " + user.getName() + " (ID: " + user.getId() + ")");

            clearError();
            redirectBasedOnUserRole(user.getUserType());
        } else {
            showError("Email ou mot de passe incorrect.");
            shakeLoginButton();
        }
    }

    /**
     * Redirige the user based on their role after successful login.
     * @param role The role of the authenticated user.
     */
    private void redirectBasedOnUserRole(String role) {
        try {
            String fxmlPath;

            System.out.println("User role detected: " + role);

            switch (role.toLowerCase()) {
                case "admin":
                    fxmlPath = "/Fxml/Admin/AdminDashboard.fxml";
                    break;
                case "babysitter":
                    fxmlPath = "/Fxml/Babysitters/babysitters.fxml";
                    System.out.println("Redirecting to babysitter interface.");
                    break;
                case "parent":
                case "client":
                    fxmlPath = "/Fxml/Babysitters/ClientSide.fxml";
                    System.out.println("Redirecting to client interface.");
                    break;
                default:
                    fxmlPath = "/Fxml/Client/client.fxml";
                    System.out.println("Unknown role: " + role + ". Using default client interface.");
            }

            loadFXML(fxmlPath);

        } catch (Exception e) {
            showError("Erreur lors de la redirection: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Loads a specific FXML file and sets it as the scene for the current stage.
     * Also updates the stage title with user information from SessionManager.
     * @param fxmlPath The path to the FXML file to load.
     */
    private void loadFXML(String fxmlPath) {
        try {
            System.out.println("Loading FXML: " + fxmlPath);

            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlPath)));
            Stage stage = (Stage) submitBtn.getScene().getWindow();
            Scene scene = new Scene(root);

            stage.setScene(scene);

            User loggedInUser = SessionManager.getCurrentUser();
            String userName = (loggedInUser != null) ? loggedInUser.getName() : "Unknown User";
            String userType = SessionManager.getCurrentUserType();

            stage.setTitle("Application - " + userName + " (" + userType + ")");

            stage.centerOnScreen();

            System.out.println("Interface loaded successfully for user: " + userName);

        } catch (IOException e) {
            showError("Erreur lors du chargement de l'interface: " + e.getMessage());
            System.err.println("Error loading FXML: " + fxmlPath);
            e.printStackTrace();
        } catch (NullPointerException e) {
            showError("Fichier d'interface introuvable: " + fxmlPath);
            System.err.println("FXML file not found: " + fxmlPath);
            e.printStackTrace();
        }
    }

    /**
     * Handles the "Forgot Password" action, opening the password recovery page.
     * @param actionEvent The ActionEvent that triggered this method.
     */
    public void handleForgotPassword(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/Fxml/Users/passwd.fxml")));
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Récupération de mot de passe");
            stage.show();
            Stage currentStage = (Stage) forgotPasswordLink.getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            showError("Erreur lors de l'ouverture de la page de récupération.");
            e.printStackTrace();
        }
    }

    /**
     * Opens the user registration page.
     * @param actionEvent The ActionEvent that triggered this method.
     */
    @FXML
    public void openRegister(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/Fxml/Users/Register.fxml")));
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Créer un compte");
            stage.show();
            Stage currentStage = (Stage) ((javafx.scene.Node) actionEvent.getSource()).getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            showError("Erreur lors de l'ouverture de la page d'inscription.");
            e.printStackTrace();
        }
    }

    /**
     * Displays an error message on the UI.
     * @param message The error message to display.
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    /**
     * Clears any displayed error message.
     */
    private void clearError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
    }

    /**
     * Applies a "shake" animation to the login button, typically used on authentication failure.
     */
    private void shakeLoginButton() {
        javafx.animation.TranslateTransition shake = new javafx.animation.TranslateTransition(Duration.millis(100), submitBtn);
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(4);
        shake.setAutoReverse(true);
        shake.playFromStart();
    }

    /**
     * Performs a basic validation check for email format.
     * @param email The email string to validate.
     * @return true if the email appears valid, false otherwise.
     */
    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".");
    }
}