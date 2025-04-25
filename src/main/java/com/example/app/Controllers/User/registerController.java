package com.example.app.Controllers.User;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.app.Models.Database.insertUser;
import static com.example.app.Models.Database.connect;
import static com.example.app.Models.Database.*;
import static com.example.app.Models.hashPasswd.*;



public class registerController implements Initializable {

    @FXML public Label account_type_label;
    @FXML public Label email_label;
    @FXML public Label phone_label;
    @FXML public TextField phoneInput;
    @FXML public Label adresse_label;
    @FXML public TextField adressInput;
    @FXML public Label password_label;
    @FXML public Label address_label;
    @FXML public TextField address_input;
    @FXML public ChoiceBox<String> accountSelectorChoice;
    @FXML public Label emailLabel;
    @FXML public TextField emailInput;
    @FXML public PasswordField passwordInput;
    @FXML public Label errorLabel;
    @FXML public Button submitBtn;

    public TextField userNameInput;
    public Label userName_label;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        accountSelectorChoice.getItems().addAll("Client", "Baby sitter");
        accountSelectorChoice.setValue("your account type");
    }

    @FXML
    public void submit() {
        String accountType = accountSelectorChoice.getValue();
        String email = emailInput.getText();
        String username = userNameInput.getText();
        String phone = phoneInput.getText();
        String address = adressInput.getText();
        String password = passwordInput.getText();

        if (email.isEmpty() || phone.isEmpty() || address.isEmpty() || password.isEmpty() || accountType == null || username.isEmpty()) {
            errorLabel.setText("Please fill in all fields.");
            return;
        }

        String error = !validateInput(email, EMAIL_REGEX)
                ? "Invalid email format."
                : !validateInput(password, PASSWORD_REGEX)
                ? "Password must have at least 8 characters, 1 number, 1 uppercase, 1 lowercase, 1 special character."
                : null;

        if (error != null) {
            errorLabel.setText(error);
            return;
        }

        insertUser(username, accountType, email, phone, address, password);
        errorLabel.setText("Account created successfully!");
    }


    public void handleButtonClick(javafx.event.ActionEvent actionEvent) {
        Button submitBtn = (Button) actionEvent.getSource();
        submit();
    }

}
