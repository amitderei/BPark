package controllers;

import client.ClientController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import ui.UiUtils;

/**
 * Controller for the subscriber's main screen in the BPARK system.
 * Provides navigation to all subscriber-level actions.
 */
public class SubscriberMainController implements ClientAware {

    /* -------------------------------------------------------------
     *  FXML-injected controls
     * ------------------------------------------------------------- */
    @FXML private Label  welcomeLabel;

    @FXML private Button btnHome;
    @FXML private Button btnLogout;

    @FXML private Button btnViewPersonalInfo;
    @FXML private Button btnViewParkingHistory;
    @FXML private Button btnViewActiveParkingInfo;
    @FXML private Button btnExtendParkingTime;
    @FXML private Button btnSubmitVehicle;
    @FXML private Button btnRetrieveVehicle;
    @FXML private Button btnParkingReservation;
    @FXML private Button btnParkingCodeConfirmation;

    /* -------------------------------------------------------------
     *  Runtime fields
     * ------------------------------------------------------------- */
    private ClientController client;

    /* -------------------------------------------------------------
     *  ClientAware implementation
     * ------------------------------------------------------------- */

    /** Injects the active client once the screen is loaded. */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
    }

    /* -------------------------------------------------------------
     *  Public API
     * ------------------------------------------------------------- */

    /**
     * Displays a personalized welcome message.
     *
     * @param name subscriber's first name
     */
    public void setSubscriberName(String name) {
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + name + "!");
        }
    }

    /* -------------------------------------------------------------
     *  Top-toolbar handlers
     * ------------------------------------------------------------- */

    /** “Home” button – already on home, so nothing to do. */
    @FXML private void handleHomeClick() {
        System.out.println("Home button clicked (already on home screen).");
    }

    /** Logs out and returns to the entry screen. */
    @FXML private void handleLogoutClick() {
        UiUtils.loadScreen(btnLogout,
                           "/client/MainScreen.fxml",
                           "BPARK – Welcome",
                           client);
    }

    /* -------------------------------------------------------------
     *  Navigation button handlers (placeholders for now)
     * ------------------------------------------------------------- */

    @FXML private void handleViewPersonalInfo()        { System.out.println("Viewing personal info…"); }
    @FXML private void handleViewParkingHistory()      { System.out.println("Viewing parking history…"); }
    @FXML private void handleViewActiveParkingInfo()   { System.out.println("Viewing active parking info…"); }
    @FXML private void handleExtendParkingTime()       { System.out.println("Extending parking time…"); }
    @FXML private void handleSubmitVehicle()           { System.out.println("Submitting vehicle…"); }

    /**
     * Opens the vehicle-pickup screen when the user clicks “Retrieve Vehicle”.
     */
    @FXML
    private void handleRetrieveVehicle() {
        UiUtils.loadScreen(btnRetrieveVehicle,
                           "/client/VehiclePickupScreen.fxml",
                           "BPARK – Vehicle Pickup",
                           client);
    }

    @FXML private void handleParkingReservation()      { System.out.println("Reserving parking…"); }
    @FXML private void handleParkingCodeConfirmation() { System.out.println("Confirming parking code…"); }
}

