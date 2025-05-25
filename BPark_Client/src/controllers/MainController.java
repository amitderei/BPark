package controllers;

import client.ClientController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

/**
 * Controller for the main screen of BPARK.
 * Allows users to either login or enter as a guest.
 */
public class MainController implements ClientAware {

    private ClientController client;

    /** Button for guest entry */
    @FXML
    private Button guestBtn;

    /** Button for registered user login */
    @FXML
    private Button loginBtn;

    /**
     * Injects the active ClientController for communication with the server.
     *
     * @param client the active client instance
     */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
    }

    /**
     * Handles guest button click and navigates to the guest interface.
     */
    @FXML
    public void handleGuest() {
        loadScreen("/client/Guest_main.fxml");
    }

    /**
     * Handles login button click and navigates to the login screen.
     */
    @FXML
    public void handleLogin() {
        loadScreen("/client/LoginScreen.fxml");
    }

    /**
     * Loads a new screen and injects the client controller if applicable.
     *
     * @param fxmlPath the relative path to the FXML screen
     */
    private void loadScreen(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object ctrl = loader.getController();
            if (ctrl instanceof ClientAware aware) {
                aware.setClient(client);
            }

            Stage stage = (Stage) guestBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setWidth(800);
            stage.setHeight(500);
            stage.show();

        } catch (Exception e) {
            System.err.println("Failed to load screen: " + fxmlPath);
            e.printStackTrace();
        }
    }
}
