package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Landing screen shown on the physical kiosk / terminal.
 * No user-specific data is needed; a fixed greeting is enough.
 */
public class TerminalMainController {

    /** Label that shows the greeting text on the terminal welcome screen */
    @FXML
    private Label welcomeLabel;

    /**
     * Called automatically by JavaFX after the FXML is loaded.
     * Sets a static greeting message for the terminal operator.
     */
    @FXML
    public void initialize() {
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome to the Terminal!");
        }
    }
}
