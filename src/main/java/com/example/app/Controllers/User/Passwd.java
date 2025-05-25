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
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.UUID;
import javax.mail.*;
import javax.mail.internet.*;

public class Passwd implements Initializable {

    @FXML
    private TextField emailField;

    @FXML
    private Label statusLabel;

    @FXML
    private Button sendButton;

    @FXML
    private Hyperlink backToLoginLink;

    // Email configuration constants
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String EMAIL_FROM = ""; // Replace with your app's email
    private static final String EMAIL_PASSWORD = ""; // Replace with app password
    private static final String EMAIL_SUBJECT = "";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Clear status label at startup
        statusLabel.setText("");
    }

    @FXML
    public void handleSendRecoveryLink(ActionEvent event) {
        String email = emailField.getText().trim();

        // Basic validation
        if (email.isEmpty()) {
            showStatus("Veuillez entrer votre adresse email", true);
            shakeButton();
            return;
        }

        if (!isValidEmail(email)) {
            showStatus("Format d'email invalide", true);
            shakeButton();
            return;
        }

        // Check if the email exists in database
        try {
            if (emailExists(email)) {
                // Generate and save recovery token
                String token = generateRecoveryToken(email);

                if (token != null) {
                    // Send the recovery email
                    boolean emailSent = sendRecoveryEmail(email, token);

                    if (emailSent) {
                        showStatus("Un code de récupération a été envoyé à " + email, false);

                        // Redirect to the reset password page
                        navigateToResetPassword();

                        // Disable the button to prevent multiple submissions
                        sendButton.setDisable(true);
                    } else {
                        showStatus("Erreur lors de l'envoi de l'email de récupération", true);
                    }
                } else {
                    showStatus("Erreur lors de la création du jeton de récupération", true);
                }
            } else {
                // For security reasons, show the same success message
                // even if the email doesn't exist to prevent email enumeration attacks
                showStatus("Si cet email existe, un code de récupération sera envoyé", false);
            }
        } catch (SQLException e) {
            showStatus("Erreur de connexion à la base de données", true);
            e.printStackTrace();
        }
    }

    @FXML
    public void handleBackToLogin(ActionEvent event) {
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

    private void navigateToResetPassword() {
        try {
            // Load the reset password view
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/Fxml/Users/ResetPassword.fxml")));

            // Get the current stage
            Stage stage = (Stage) sendButton.getScene().getWindow();

            // Set the new scene
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Réinitialisation du mot de passe");
            stage.show();
        } catch (IOException e) {
            showStatus("Erreur lors du chargement de la page de réinitialisation", true);
            e.printStackTrace();
        }
    }

    private boolean emailExists(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }

        return false;
    }

    private String generateRecoveryToken(String email) throws SQLException {
        // Generate a random token
        String token = UUID.randomUUID().toString();

        // Store the token in the database with an expiration time
        String sql = "UPDATE users SET reset_token = ?, reset_token_expires = NOW() + INTERVAL '1 hour' WHERE email = ?";

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, token);
            stmt.setString(2, email);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                return token;
            }
        }

        return null;
    }

    /**
     * Sends a recovery email with the reset token code
     * @param email The recipient's email address
     * @param token The password reset token
     * @return true if email was sent successfully, false otherwise
     */
    private boolean sendRecoveryEmail(String email, String token) {
        // Set up mail server properties
        Properties properties = new Properties();
        properties.put("mail.smtp.host", SMTP_HOST);
        properties.put("mail.smtp.port", SMTP_PORT);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        // Create session with authentication
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_FROM, EMAIL_PASSWORD);
            }
        });

        try {
            // Create message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_FROM));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            message.setSubject(EMAIL_SUBJECT);

            // Extract the first 8 characters of the token to use as a reset code
            String resetCode = token.substring(0, 8);

            // Compose the email content
            String emailContent =
                    "Bonjour,\n\n" +
                            "Vous avez demandé la réinitialisation de votre mot de passe.\n\n" +
                            "Voici votre code de réinitialisation: " + resetCode + "\n\n" +
                            "Utilisez ce code dans l'application pour créer un nouveau mot de passe.\n" +
                            "Ce code expire dans 1 heure.\n\n" +
                            "Si vous n'avez pas demandé de réinitialisation de mot de passe, veuillez ignorer cet email.\n\n" +
                            "Cordialement,\n" +
                            "L'équipe de votre application";

            // Set the email content
            message.setText(emailContent);

            // Send the message
            Transport.send(message);

            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
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
        javafx.animation.TranslateTransition shake = new javafx.animation.TranslateTransition(Duration.millis(100), sendButton);
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(4);
        shake.setAutoReverse(true);
        shake.playFromStart();
    }

    private boolean isValidEmail(String email) {
        // Improved email validation with regex
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }
}