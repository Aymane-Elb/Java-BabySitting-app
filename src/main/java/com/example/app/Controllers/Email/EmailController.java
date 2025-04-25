package com.example.app.Controllers.Email;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class EmailController implements Initializable {

    public Button menuBtn;
    public Button writeBtn;
    public Button messageBtn;
    public Button favoriteBtn;
    public Button homeBtn;
    public Button settingBtn;
    public Button searchBtn;
    public FontIcon menuBrtn;
    public Button inboxBtn;
    public TextField searchInput;
    @FXML
    private ListView<inboxEmail> emailListView;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Load dummy data
        emailListView.setItems(FXCollections.observableArrayList(
                new inboxEmail("Oracle University", "Start a new discussion", "Check the latest news on APEX.", "Apr 9"),
                new inboxEmail("Printify", "Here's where you should sell", "Find your niche in a few steps", "Apr 4")
        ));

        // Customize cells
        emailListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(inboxEmail inboxEmail, boolean empty) {
                super.updateItem(inboxEmail, empty);
                if (empty || inboxEmail == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox content = new VBox();
                    content.getChildren().addAll(
                            new Label("📧 " + inboxEmail.getSender() + " - " + inboxEmail.getSubject()),
                            new Label(inboxEmail.getSnippet()),
                            new Label(inboxEmail.getDate())
                    );
                    setGraphic(content);
                }
            }
        });
    }

    public void openCloseMenu(ActionEvent actionEvent) {
    }

    public void openInbox(ActionEvent actionEvent) {
        try
        {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/Fxml/Messages/writeToSomeone.fxml")));
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
            Stage currentStage = (Stage) ((javafx.scene.Node) actionEvent.getSource()).getScene().getWindow();
            currentStage.close();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void openWriteToSomeone(ActionEvent actionEvent) {
        try
        {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/Fxml/Messages/writeToSomeone.fxml")));
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
            Stage currentStage = (Stage) ((javafx.scene.Node) actionEvent.getSource()).getScene().getWindow();
            currentStage.close();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void openClientSide(ActionEvent actionEvent) {
        try
        {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/Fxml/Messages/writeToSomeone.fxml")));
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
            Stage currentStage = (Stage) ((javafx.scene.Node) actionEvent.getSource()).getScene().getWindow();
            currentStage.close();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void openSettings(ActionEvent actionEvent) {
        try
        {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/Fxml/Messages/writeToSomeone.fxml")));
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
            Stage currentStage = (Stage) ((javafx.scene.Node) actionEvent.getSource()).getScene().getWindow();
            currentStage.close();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
