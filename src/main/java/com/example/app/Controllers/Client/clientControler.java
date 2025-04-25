package com.example.app.Controllers.Client;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class clientControler implements Initializable {
    public AnchorPane controlsPane;
    public WebView mapView;
    public TextField searchInput;
    public Button searchBtn0;
    public Button searchBtn1;
    public Button mapBtn;
    public Button messageBtn;
    public Button accountBtn;
    @FXML
    private ListView<Client> babySitersList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Load dummy data
        babySitersList.setItems(FXCollections.observableArrayList(
                new Client("jakie", "New york", 3.5, 10),
                new Client("Printify", "Here's where you should sell", 0,  4)
        ));

        // Customize cells
        babySitersList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Client client, boolean empty) {
                super.updateItem(client, empty);
                if (empty || client == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox content = new VBox();
                    content.getChildren().addAll(
                            new Label( "Name : " +client.getName() ),
                            new Label("Experience : " +client.getExperience()+ "years" ),
                            new Label("Location : " +client.getLocation()),
                            new Label("Price : " +client.getPrice() + "$/Hr")
                    );
                    setGraphic(content);
                }
            }
        });
    }
    private void searchForBabySitters(String query) {
        // Search logic to filter or fetch baby sitters based on query
        // This could filter from the current list of baby sitters based on location, experience, etc.
        // For now, you could just filter by location as an example:

        // Filter the list based on search query
        babySitersList.setItems(FXCollections.observableArrayList(
                new Client("jakie", "New york", 3.5, 10),
                new Client("Printify", "Here's where you should sell", 0,  4)
        ).filtered(client -> client.getLocation().toLowerCase().contains(query.toLowerCase())));
    }


    public void onMessageButtonClick(javafx.event.ActionEvent actionEvent) {
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

    public void openMap(ActionEvent actionEvent) {
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

    public void openAccontWindow(ActionEvent actionEvent) {
        /*try
        {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/Fxml/User/Account.fxml")));
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
            Stage currentStage = (Stage) ((javafx.scene.Node) actionEvent.getSource()).getScene().getWindow();
            currentStage.close();
        } catch (Exception e)
        {
            e.printStackTrace();
        }*/
    }

    public void onSearchBtn0(ActionEvent actionEvent) {
        String query = searchInput.getText();
        if (!query.isEmpty()) {
            searchForBabySitters(query);
        }
    }

    public void onSearchBtn1(ActionEvent actionEvent) {
    }
}
