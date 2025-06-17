package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Landing screen shown on the physical kiosk / terminal.
 * No user-specific data is needed; a fixed greeting is enough.
 */
public class TerminalMainController {

    /** Label that shows the greeting text */
    @FXML
    private Label welcomeLabel;

    /**
     * Runs automatically after the FXML is loaded.
     * Sets a static welcome line for attendant use.
     */
    @FXML
    public void initialize() {
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, Terminal Operator!");
        }
    }
}
