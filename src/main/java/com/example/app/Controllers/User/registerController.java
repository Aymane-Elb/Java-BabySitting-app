package com.example.app.Controllers.User;

import com.example.app.Models.Database;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class registerController implements Initializable {

    // Constants for validation
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    private static final String PASSWORD_REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";

    // FXML components
    @FXML public Label account_type_label;
    @FXML public Label email_label;
    @FXML public Label phone_label;
    @FXML public TextField phoneInput;
    @FXML public Label adresse_label;
    @FXML public TextField adressInput;
    @FXML public Label password_label;
    @FXML public Label address_label;
    @FXML public ChoiceBox<String> accountSelectorChoice;
    @FXML public Label emailLabel;
    @FXML public TextField emailInput;
    @FXML public PasswordField passwordInput;
    @FXML public Label errorLabel;
    @FXML public Button submitBtn;
    @FXML public TextField userNameInput;
    @FXML public Label userName_label;
    @FXML public Button loginButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Récupérer les types d'utilisateurs disponibles depuis la base de données
        try {
            List<String> userTypes = Database.getUserTypes();
            accountSelectorChoice.getItems().addAll(userTypes);

            // Si la liste est vide, ajouter des valeurs par défaut
            if (userTypes.isEmpty()) {
                accountSelectorChoice.getItems().addAll("parent", "babysitter", "admin");
            }
        } catch (Exception e) {
            // En cas d'erreur, utiliser des valeurs par défaut
            accountSelectorChoice.getItems().addAll("parent", "babysitter", "admin");
            System.err.println("Failed to load user types: " + e.getMessage());
        }

        accountSelectorChoice.setValue("Select account type");

        // Set up any tooltips or additional UI hints
        passwordInput.setTooltip(new Tooltip("Password must have at least 8 characters, including 1 number, 1 uppercase, 1 lowercase, and 1 special character"));
        emailInput.setTooltip(new Tooltip("Enter a valid email address"));

        // Clear error label initially
        errorLabel.setText("");
    }

    /**
     * Handle form submission
     */
    @FXML
    public void submit() throws SQLException {
        // Clear previous error message
        errorLabel.setText("");

        // Get form values
        String accountType = accountSelectorChoice.getValue();
        String email = emailInput.getText().trim();
        String username = userNameInput.getText().trim();
        String phone = phoneInput.getText().trim();
        String address = adressInput.getText().trim();
        String password = passwordInput.getText();

        // Basic validation
        if (email.isEmpty() || phone.isEmpty() || address.isEmpty() || password.isEmpty() ||
                "Select account type".equals(accountType) || username.isEmpty()) {
            errorLabel.setText("Please fill in all fields.");
            return;
        }

        // Format validation
        if (!validateInput(email, EMAIL_REGEX)) {
            errorLabel.setText("Invalid email format.");
            return;
        }

        if (!validateInput(password, PASSWORD_REGEX)) {
            errorLabel.setText("Password must have at least 8 characters, 1 number, 1 uppercase, 1 lowercase, and 1 special character.");
            return;
        }

        // Check if email already exists
        if (Database.emailExists(email)) {
            errorLabel.setText("Email already registered. Please login or use a different email.");
            return;
        }

        // Debug message to verify account type value
        System.out.println("Attempting to register with account type: " + accountType);

        try {
            // Insert user into database
            int userId = Database.insertUser(username, accountType, email, phone, address, password);

            if (userId > 0) {
                // Success - show message briefly and redirect to login immediately
                errorLabel.setStyle("-fx-text-fill: green;");
                errorLabel.setText("Account created successfully! Redirecting to login...");

                // Go to login page immediately
                goToLogin();
            } else {
                errorLabel.setText("Registration failed. Please try again.");
            }
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            errorLabel.setText("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Event handler for the submit button
     */
    @FXML
    public void handleButtonClick(ActionEvent actionEvent) {
        try {
            submit();
        } catch (SQLException e) {
            errorLabel.setText("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Navigate to login page
     */
    @FXML
    public void goToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Users/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) submitBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Navigation error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handler for login link button
     */
    @FXML
    public void onLoginClicked(ActionEvent event) {
        goToLogin();
    }

    /**
     * Validate input against regex pattern
     * @param input Text to validate
     * @param regex Regex pattern to match against
     * @return true if input matches the pattern, false otherwise
     */
    private boolean validateInput(String input, String regex) {
        return Pattern.compile(regex).matcher(input).matches();
    }
}