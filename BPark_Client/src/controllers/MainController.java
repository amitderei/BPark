package controllers;

import client.ClientController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import ui.UiUtils;

/**
 * Controller for BPARK's entry screen.
 * Lets the user choose between Guest mode and Login.
 */
public class MainController implements ClientAware {

    /* ------------------------------------------------------------------
     *  FXML-injected controls
     * ------------------------------------------------------------------ */
    @FXML private Button guestBtn;   // “Enter as Guest”
    @FXML private Button loginBtn;   // “Login”

    /* ------------------------------------------------------------------
     *  Runtime
     * ------------------------------------------------------------------ */
    private ClientController client;

    /** Injects the active client. */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
    }

    /* ------------------------------------------------------------------
     *  Button handlers
     * ------------------------------------------------------------------ */

    /** Opens the guest interface. */
    @FXML
    public void handleGuest() {
        UiUtils.loadScreen(guestBtn,
                           "/client/GuestMainScreen.fxml",
                           "BPARK – Guest",
                           client);               // client may be null for guest flow
    }

    /** Opens the login screen. */
    @FXML
    public void handleLogin() {
        UiUtils.loadScreen(loginBtn,
                           "/client/LoginScreen.fxml",
                           "BPARK – Login",
                           client);
    }
}

