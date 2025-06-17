package controllers;

import client.ClientController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import ui.UiUtils;

/**
 * Entry screen of BPARK.
 * Presents two choices: continue as guest or log in.
 * Also includes Exit and Back buttons for navigation.
 */
public class MainController implements ClientAware {

    /* ---------- FXML buttons ---------- */
    @FXML private Button guestBtn;   // "Enter as Guest"
    @FXML private Button loginBtn;   // "Login"
    @FXML private Button btnExit;    // "Exit"
    @FXML private Button btnBack;    // "Back to previous"

    /* ---------- runtime ---------- */
    private ClientController client;

    /**
     * Receives the shared ClientController so this screen
     * can pass it along when loading child views.
     *
     * @param client active client instance
     */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
    }

    /* =====================================================
     *  Button actions
     * ===================================================== */

    /** Loads the guest layout and switches the window title. */
    @FXML
    public void handleGuest() {
        UiUtils.loadScreen(guestBtn,
                "/client/GuestMainLayout.fxml",
                "BPARK – Guest",
                client);
    }

    /** Opens the login screen for registered users. */
    @FXML
    public void handleLogin() {
        UiUtils.loadScreen(loginBtn,
                "/client/LoginScreen.fxml",
                "BPARK – Login",
                client);
    }

    /**
     * Disconnects from the server (if needed) and closes the application.
     * Attached to the Exit button.
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

    /** Returns to the previous selection screen. */
    @FXML
    private void handleBackClick() {
        UiUtils.loadScreen(btnBack,
                "/client/SelectionScreen.fxml",
                "Select User Type",
                client);
    }
}

