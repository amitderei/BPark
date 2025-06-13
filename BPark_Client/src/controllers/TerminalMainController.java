package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controller for the terminal's main screen in the BPARK system.
 * Used by parking staff – no login is required.
 */
public class TerminalMainController {

    @FXML
    private Label welcomeLabel;

    /**
     * Initializes the screen and displays a static welcome message.
     */
    @FXML
    public void initialize() {
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, Terminal Operator!");
        }
    }
}
