package controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controller for the guest main screen.
 * Shows a greeting and keeps the live counter of free spots.
 */
public class GuestMainController {

    @FXML private Label welcomeLabel;
    @FXML private Label availabilityLabel;   // ← תוודא שיש label כזה ב-FXML

    /** Initializes the screen with a welcome message. */
    @FXML
    public void initialize() {
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, Guest!");
        }
    }

    /**
     * Updates the “Available spots” label.
     *
     * @param freeSpots current number of vacant parking spaces
     */
    public void updateAvailableSpots(int freeSpots) {
        Platform.runLater(() -> {
            if (availabilityLabel != null) {
                availabilityLabel.setText("Available spots: " + freeSpots);
            }
        });
    }
}
