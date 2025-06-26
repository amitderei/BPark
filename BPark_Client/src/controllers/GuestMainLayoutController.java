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
 * Main layout for a guest session.
 * Top – standard toolbar (Home / Back / Exit).<br>
 * Left – side-menu with a single action: spot availability lookup.<br>
 * Center – dynamically switches content based on user actions.
 */
public class GuestMainLayoutController implements ClientAware {

    /** Exit button – disconnects and closes the application */
    @FXML private Button btnExit;

    /** Back button – returns to the selection screen */
    @FXML private Button btnBack;

    /** Home button – loads the guest home screen */
    @FXML private Button btnHome;

    /** Button that loads the live spot availability screen */
    @FXML private Button btnCheckAvailability;

    /** Central placeholder where child screens are loaded */
    @FXML private AnchorPane center;

    /** Reference to the shared client, used to communicate with the server */
    private ClientController client;

    // =====================================================
    // Framework hooks
    // =====================================================

    /**
     * Runs automatically after the FXML file is loaded.
     * Loads the guest home screen into the center pane.
     */
    @FXML
    public void initialize() {
        handleHomeClick();
    }

    /**
     * Receives the shared ClientController so it can be passed to
     * dynamically loaded child screens.
     *
     * @param client active client instance
     */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
    }

    // =====================================================
    // Toolbar actions
    // =====================================================

    /**
     * Loads the static "Welcome, Guest" screen into the center pane.
     */
    @FXML
    private void handleHomeClick() {
        loadScreen("/client/GuestMainScreen.fxml");
    }

    /**
     * Navigates back to the user-type selection screen (guest / login).
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
        try {
            if (client != null && client.isConnected()) {
                client.sendToServer(new Object[]{Operation.DISCONNECT});
                client.closeConnection();
            }
        } catch (Exception ignored) {
            // not critical – we are exiting anyway
        }
        Platform.exit();
        System.exit(0);
    }

    // =====================================================
    // Side-menu actions
    // =====================================================

    /**
     * Loads the live availability screen showing number of free spots.
     */
    @FXML
    private void handleCheckAvailability() {
        loadScreen("/client/AvailabilityScreen.fxml");
    }

    // =====================================================
    // Internal helper
    // =====================================================

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
                client.requestParkingAvailability();
            }

            center.getChildren().setAll(content);

        } catch (IOException e) {
            // For production: replace with logging
            e.printStackTrace();
        }
    }
}
