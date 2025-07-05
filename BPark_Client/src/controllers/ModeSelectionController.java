package controllers;

import client.ClientController;
import common.Operation;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.stage.Stage;
import ui.DragUtil;
import ui.StageAware;
import ui.UiUtils;

/**
 * Controller for the mode selection screen.
 * 
 * This is the first screen shown after a successful connection to the server.
 * It allows the user to select one of the following options:
 * - Full GUI application (guest or subscriber login)
 * - Terminal interface for parking-lot staff
 * - Exit the application
 */
public class ModeSelectionController implements ClientAware, StageAware {
    /** Button to launch the full BPARK application (GUI flow for guests/subscribers) */
    @FXML 
    private Button btnApp;

    /** Button to launch the simplified terminal interface (for parking-lot staff) */
    @FXML 
    private Button btnTerminal;

    @FXML 
    private Button btnExit;

    /** Reference to the shared client instance used to communicate with the server */
    private ClientController client;
    
    /** The toolbar used for dragging the undecorated window */
    @FXML
    private ToolBar dragArea;

    /**
     * Enables drag-to-move behavior using the top toolbar.
     *
     * @param stage the primary application stage
     */
    public void setStage(Stage stage) {
    	DragUtil.enableDrag(dragArea, stage);
    }

	/**
	 * Injects the shared ClientController instance.
	 *
	 * @param client active client controller instance
	 */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
    }

    /**
     * Called when the user selects the full GUI application.
     * Loads the main screen that allows guest access or subscriber login.
     */
    @FXML
    public void handleApp() {
        UiUtils.loadScreen(btnApp,
                "/client/MainScreen.fxml",
                "BPARK – Welcome",
                client);
    }

    /**
     * Called when the user selects terminal mode.
     * Loads the FXML layout for the terminal/kiosk interface.
     */
    @FXML
    public void handleTerminal() {
        UiUtils.loadScreen(btnTerminal,
                "/client/TerminalMainLayout.fxml",
                "BPARK – Terminal",
                client);
    }

    /**
     * Closes the connection to the server (if active) and exits the application.
     * Triggered when the user presses the "Exit" button.
     */
    @FXML
    private void handleExitClick() {
       UiUtils.exitFromSystem();
    }
}
