package controllers;

import client.ClientController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import ui.UiUtils;

/**
 * First screen shown after network connection: lets the user decide
 * whether to run the full application (guest / login flow) or open
 * the simplified terminal interface used by parking-lot attendants.
 */
public class ModeSelectionController implements ClientAware {

    /* ---------- FXML buttons ---------- */
    @FXML private Button btnApp;      // Opens full application mode
    @FXML private Button btnTerminal; // Opens terminal (kiosk) mode
    @FXML private Button btnExit;     // Exits the program

    /* ---------- runtime ---------- */
    private ClientController client;

    /**
     * Receives the shared ClientController so it can be forwarded
     * to whichever mode the user selects.
     *
     * @param client active client instance (may be null before connection)
     */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
    }

    /* =====================================================
     *  Button actions
     * ===================================================== */

    /** Loads the main GUI application (guest / login choice). */
    @FXML
    public void handleApp() {
        UiUtils.loadScreen(btnApp,
                "/client/MainScreen.fxml",
                "BPARK – Welcome",
                client);
    }

    /** Loads the attendant terminal interface. */
    @FXML
    public void handleTerminal() {
        UiUtils.loadScreen(btnTerminal,
                "/client/TerminalMainLayout.fxml",
                "BPARK – Terminal",
                client);
    }

    /** Disconnects from server (if connected) and quits the program. */
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
}
