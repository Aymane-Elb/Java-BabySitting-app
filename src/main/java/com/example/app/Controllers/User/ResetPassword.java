package com.example.app.Controllers.User;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;

import com.example.app.Models.Database;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.ResourceBundle;

public class ResetPassword implements Initializable {

    @FXML
    private TextField codeField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label statusLabel;

    @FXML
    private Button resetButton;

    @FXML
    private Hyperlink backToLoginLink;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Clear status label at startup
        statusLabel.setText("");
    }

    @FXML
    public void handleResetPassword(ActionEvent event) {
        String code = codeField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validate input
        if (code.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showStatus("Veuillez remplir tous les champs", true);
            shakeButton();
            return;
        }

        if (!password.equals(confirmPassword)) {
            showStatus("Les mots de passe ne correspondent pas", true);
            shakeButton();
            return;
        }

        // Use your existing password validation regex
        if (!com.example.app.Models.hashPasswd.validateInput(password, com.example.app.Models.hashPasswd.PASSWORD_REGEX)) {
            showStatus("Le mot de passe doit contenir au moins 8 caractères, une majuscule, une minuscule, un chiffre et un caractère spécial", true);
            shakeButton();
            return;
        }

        // Verify code and reset password
        try {
            if (resetPasswordWithCode(code, password)) {
                showStatus("Mot de passe réinitialisé avec succès", false);

                // Disable reset button
                resetButton.setDisable(true);

                // Redirect to login page after 2 seconds
                javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(Duration.seconds(2));
                pause.setOnFinished(e -> navigateToLogin());
                pause.play();
            } else {
                showStatus("Code invalide ou expiré", true);
                shakeButton();
            }
        } catch (SQLException e) {
            showStatus("Erreur lors de la réinitialisation du mot de passe", true);
            e.printStackTrace();
        }
    }

    @FXML
    public void handleBackToLogin(ActionEvent event) {
        navigateToLogin();
    }

    private boolean resetPasswordWithCode(String code, String newPassword) throws SQLException {
        // Find user with this code (first 8 chars of the token)
        String sql = "SELECT user_id FROM users WHERE reset_token LIKE ? AND reset_token_expires > NOW()";

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, code + "%");  // Add wildcard to match the first part of token

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt("user_id");

                    // Update password and clear token
                    String updateSql = "UPDATE users SET password = ?, reset_token = NULL, reset_token_expires = NULL WHERE user_id = ?";

                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        // Hash the password using your existing hashPasswd class
                        String hashedPassword = com.example.app.Models.hashPasswd.hashPassword(newPassword);
                        updateStmt.setString(1, hashedPassword);
                        updateStmt.setInt(2, userId);

                        int rowsAffected = updateStmt.executeUpdate();
                        return rowsAffected > 0;
                    }
                }
            }
        }

        return false;
    }

    private void navigateToLogin() {
        try {
            // Load the login view
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/Fxml/Users/Login.fxml")));

            // Get the current stage
            Stage stage = (Stage) backToLoginLink.getScene().getWindow();

            // Set the new scene
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Connexion");
            stage.show();
        } catch (IOException e) {
            showStatus("Erreur lors du chargement de la page de connexion", true);
            e.printStackTrace();
        }
    }

    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);

        if (isError) {
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
        } else {
            statusLabel.setStyle("-fx-text-fill: #2ecc71;");
        }
    }

    private void shakeButton() {
        javafx.animation.TranslateTransition shake = new javafx.animation.TranslateTransition(Duration.millis(100), resetButton);
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(4);
        shake.setAutoReverse(true);
        shake.playFromStart();
    }
}