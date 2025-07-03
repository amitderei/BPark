package controllers;

import java.io.IOException;

import client.ClientController;
import common.Operation;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import ui.UiUtils;

/**
 * Controller for the main layout shown to guest users.
 * Provides top navigation (Home / Back / Exit), a side menu for availability check,
 * and a central area where different screens are loaded dynamically.
 */
public class GuestMainLayoutController implements ClientAware {

    @FXML 
    private Button btnExit;

    @FXML 
    private Button btnBack;

    @FXML 
    private Button btnHome;

    /** Button that loads the live spot availability screen */
    @FXML 
    private Button btnCheckAvailability;

    /** Central placeholder where child screens are loaded */
    @FXML 
    private AnchorPane center;

    /** Reference to the active client used for server communication */
    private ClientController client;


    /**
     * Runs automatically after the FXML file is loaded.
     * Loads the guest home screen into the center pane.
     */
    @FXML
    public void initialize() {
        handleHomeClick();
    }

    /**
     * Receives the ClientController and registers this controller
     * inside it so the server can push updates later on.
     *
     * @param client active client instance, may be null before connection
     */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
    }

    /**
     * Loads the static screen into the center pane.
     */
    @FXML
    private void handleHomeClick() {
        loadScreen("/client/GuestMainScreen.fxml");
    }

    /**
     * Navigates back to the user-type selection screen
     */
    @FXML
    private void handleBackClick() {
        UiUtils.loadScreen(btnBack,
                "/client/SelectionScreen.fxml",
                "BPARK – Welcome",
                client);
    }

    /**
     * Disconnects from the server if connected, and closes the app.
     * Triggered by the Exit button.
     */
    @FXML
    private void handleExitClick() {
        UiUtils.exitFromSystem();
    }



    /**
     * Loads the live availability screen showing number of free spots.
     */
    @FXML
    private void handleCheckAvailability() {
        loadScreen("/client/AvailabilityScreen.fxml");
    }


    /**
     * Loads the specified FXML screen into the center pane and
     * injects the ClientController into the child controller
     * if it implements ClientAware.
     *
     * @param fxml path to the FXML file within the classpath
     */
    private void loadScreen(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent content = loader.load();

            Object ctrl = loader.getController();

            // Automatically wire client to child controllers
            if (ctrl instanceof ClientAware aware)
                aware.setClient(client);

            // Special handling for availability screen
            if (ctrl instanceof AvailabilityController ac) {
                ac.setClient(client);
                client.setAvailabilityController(ac);
                client.getRequestSender().requestParkingAvailability();
            }

            center.getChildren().setAll(content);

        } catch (IOException e) {
            // For production: replace with logging
            e.printStackTrace();
        }
    }
}
