package com.example.app.Controllers.User;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import com.example.app.Models.Database;
import com.example.app.Models.hashPasswd;
import com.example.app.Models.Model;
import javafx.stage.Stage;

import java.awt.event.ActionEvent;
import java.util.EventObject;
import java.util.Objects;

import static javafx.application.Application.launch;


public class loginController {
    public Label email_label;
    public TextField emailInput;
    public Label password_label;
    public PasswordField passwdInput;
    public Label errorLabel;
    public Button submitBtn;
    public Label pasDeComptLabel;
    public Button createAcountBtn;

    public void openRegister(javafx.event.ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/Fxml/Users/Register.fxml")));
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
            Stage currentStage = (Stage) ((javafx.scene.Node) actionEvent.getSource()).getScene().getWindow();
            currentStage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
