package controllers;

import java.io.IOException;

import client.ClientController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import ui.UiUtils;

/**
 * Main layout for a guest session.
 * Top – standard toolbar (Home / Back / Exit).  
 * Left – small side-menu with a single action: spot availability lookup.  
 * Center – switches between guest screens according to user actions.
 */
public class GuestMainLayoutController implements ClientAware {

    /* ---------- FXML toolbar buttons ---------- */
    @FXML private Button btnExit;
    @FXML private Button btnBack;
    @FXML private Button btnHome;

    /* ---------- FXML side-menu button ---------- */
    @FXML private Button btnCheckAvailability;

    /* ---------- Central placeholder pane ---------- */
    @FXML private AnchorPane center;

    /* ---------- Client reference (injected) ---------- */
    private ClientController client;

    /* =====================================================
     *  Framework hooks
     * ===================================================== */

    /**
     * JavaFX initialise hook – runs automatically once the FXML is loaded.
     * Immediately loads the guest home screen into the centre pane.
     */
    @FXML
    public void initialize() {
        handleHomeClick();
    }

    /**
     * Supplies the shared ClientController so this layout
     * can pass it to child screens that implement ClientAware.
     *
     * @param client active client instance, null before connection
     */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
    }

    /* =====================================================
     *  Toolbar actions
     * ===================================================== */

    /** Loads the simple "Welcome, Guest" screen. */
    @FXML
    private void handleHomeClick() {
        loadScreen("/client/GuestMainScreen.fxml");
    }

    /** Returns to the selection screen (login / guest). */
    @FXML
    private void handleBackClick() {
        UiUtils.loadScreen(btnBack,
                           "/client/SelectionScreen.fxml",
                           "BPARK – Welcome",
                           client);
    }

    /**
     * Gracefully disconnects from the server (if connected)
     * and closes the JavaFX application.
     */
    @FXML
    private void handleExitClick() {
        try {
            if (client != null && client.isConnected()) {
                client.sendToServer(new Object[] { "disconnect" });
                client.closeConnection();
            }
        } catch (Exception ignored) {
            // not critical – we are exiting anyway
        }
        Platform.exit();
        System.exit(0);
    }

    /* =====================================================
     *  Side-menu action
     * ===================================================== */

    /** Opens the "Live Availability" screen. */
    @FXML
    private void handleCheckAvailability() {
        loadScreen("/client/AvailabilityScreen.fxml");
    }

    /* =====================================================
     *  Internal helper
     * ===================================================== */

    /**
     * Replaces the content of the centre pane with the given FXML screen.
     * If the loaded controller implements ClientAware it receives the
     * client reference automatically.
     *
     * @param fxml path to the FXML resource inside the classpath
     */
    private void loadScreen(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent content = loader.load();

            // Pass client reference to child controllers that need it
            Object ctrl = loader.getController();
            if (ctrl instanceof ClientAware aware) {
                aware.setClient(client);
            }

            // Special case: Availability screen needs immediate data refresh
            if (ctrl instanceof AvailabilityController ac) {
                ac.setClient(client);
                client.setAvailabilityController(ac);
                client.requestParkingAvailability();
            }

            center.getChildren().setAll(content);

        } catch (IOException e) {
            // In production we would log this; for now print the stacktrace.
            e.printStackTrace();
        }
    }
}

