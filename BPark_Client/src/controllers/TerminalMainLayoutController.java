package controllers;

import java.io.IOException;

import client.ClientController;
import common.Order;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import ui.UiUtils;

/**
 * Root layout for the physical kiosk / terminal used by parking-lot staff.  
 * Fixed parts: top bar (Home / Back / Exit) and a small side-menu  
 * listing the two main flows – “Submit Vehicle” (delivery) and
 * “Retrieve Vehicle” (pickup) – plus a quick availability check.
 *
 * Child screens are loaded into the centre pane by loadScreen().
 */
public class TerminalMainLayoutController implements ClientAware {

    /* ---------- top bar ---------- */
    @FXML private Button btnExit;
    @FXML private Button btnBack;
    @FXML private Button btnHome;

    /* ---------- side-menu ---------- */
    @FXML private Button btnSubmitVehicle;
    @FXML private Button btnRetrieveVehicle;
    @FXML private Button btnCheckAvailability;

    /* ---------- placeholder ---------- */
    @FXML private AnchorPane center;

    /* ---------- runtime ---------- */
    private ClientController client;

    /**
     * Saves the shared client so child screens can reach the server.
     *
     * @param client active ClientController
     */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
    }

    /* =====================================================
     *  first load
     * ===================================================== */

    /** Loads the terminal welcome panel right after FXML is ready. */
    @FXML
    public void initialize() {
        handleHomeClick();
    }

    /* =====================================================
     *  top-bar handlers
     * ===================================================== */

    /**
     * Returns the terminal to its default welcome panel.
     * Called when the operator presses the Home button.
     */
    @FXML
    private void handleHomeClick() {
        loadScreen("/client/TerminalMainScreen.fxml");
    }

    /**
     * Goes back to the “Select User Type” screen
     * (the very first screen shown after connection).
     */
    @FXML
    private void handleBackClick() {
        UiUtils.loadScreen(btnBack,
                "/client/SelectionScreen.fxml",
                "BPARK – Welcome",
                client);
    }


    /**
     * Disconnects (if needed) and shuts down the kiosk app.
     */
    @FXML
    private void handleExitClick() {
        try {
            if (client != null && client.isConnected()) {
                client.sendToServer(new Object[] { "disconnect" });
                client.closeConnection();
            }
        } catch (Exception ignored) { }
        Platform.exit();
        System.exit(0);
    }

    /* =====================================================
     *  side-menu handlers
     * ===================================================== */

    /** Opens the vehicle-delivery flow (staff helps driver submit car). */
    @FXML
    public void handleGoToDelivery() {
        loadScreen("/client/VehicleDeliveryScreen.fxml");
    }

    /** Opens the vehicle-pickup screen for retrieving a car. */
    @FXML
    private void handleRetrieveVehicle() {
        loadScreen("/client/VehiclePickupScreen.fxml");
    }

    /** Shows current free-spot count. */
    @FXML
    private void handleCheckAvailability() {
        loadScreen("/client/AvailabilityScreen.fxml");
    }

    /* =====================================================
     *  screen loader
     * ===================================================== */

    /**
     * Swaps the centre pane with the requested FXML and performs
     * minimal wiring so each child controller can talk to the server.
     *
     * @param fxml resource path of the child screen
     */
    public void loadScreen(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent content = loader.load();
            Object ctrl = loader.getController();

            /* ---------- wire client + register for callbacks ---------- */
            if (ctrl instanceof ClientAware aware)
                aware.setClient(client);

            if (ctrl instanceof CreateNewOrderViewController c) {
                client.setNewOrderController(c);
                c.initializeCombo();
            } else if (ctrl instanceof WatchAndCancelOrdersController c) {
                client.setWatchAndCancelOrdersController(c);
                c.defineTable();
            } else if (ctrl instanceof VehiclePickupController c) {
                client.setPickupController(c);
            } else if (ctrl instanceof VehicleDeliveryController c) {
                client.setDeliveryController(c);
            } else if (ctrl instanceof AvailabilityController c) {
                client.setAvailabilityController(c);
                client.requestParkingAvailability();
            }

            center.getChildren().setAll(content);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
