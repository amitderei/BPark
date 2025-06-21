package controllers;

import client.ClientController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import ui.UiUtils;

/**
 * Entry screen of BPARK.
 * 
 * Allows the user to either continue as a guest or log in as a registered user.
 * Also includes navigation controls to go back or exit the application.
 */
public class MainController implements ClientAware {

    // ==============================
    // FXML Buttons
    // ==============================

    /** Button to enter the system as a guest (no login) */
    @FXML private Button guestBtn;

    /** Button to navigate to the login screen for registered users */
    @FXML private Button loginBtn;

    /** Button to exit the application */
    @FXML private Button btnExit;

    /** Button to go back to the previous screen (Mode Selection) */
    @FXML private Button btnBack;

    // ==============================
    // Runtime Fields
    // ==============================

    /** Shared ClientController used to send requests to the server */
    private ClientController client;

    // ==============================
    // Dependency Injection
    // ==============================

    /**
     * Injects the client controller used for server communication and screen transitions.
     *
     * @param client the active client instance
     */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
    }

    // ==============================
    // Button Actions
    // ==============================

    /**
     * Loads the Guest layout and opens the main interface
     * for unregistered users.
     */
    @FXML
    public void handleGuest() {
        UiUtils.loadScreen(guestBtn,
                "/client/GuestMainLayout.fxml",
                "BPARK – Guest",
                client);
    }

    /**
     * Loads the login screen for registered users
     * including Subscribers, Attendants, or Managers.
     */
    @FXML
    public void handleLogin() {
        UiUtils.loadScreen(loginBtn,
                "/client/LoginScreen.fxml",
                "BPARK – Login",
                client);
    }

    /**
     * Disconnects from the server (if connected) and exits the application.
     * Called when the user presses the Exit button.
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

    /**
     * Loads the Mode Selection screen, allowing the user to choose
     * between App mode and Terminal mode.
     */
    @FXML
    private void handleBackClick() {
        UiUtils.loadScreen(btnBack,
                "/client/SelectionScreen.fxml",
                "Select User Type",
                client);
    }
}
