package controllers;

import client.ClientController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Main screen for guest users – shows greeting and live free-spots counter.
 */
public class GuestMainController implements ClientAware {

    @FXML private Label welcomeLabel;
    @FXML private Label availabilityLabel;

    /** reference to the network client (may be null if not injected). */
    private ClientController client;

    @FXML
    public void initialize() {
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, Guest!");
        }
    }

    /** Injects the client instance (called by layout loader). */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
        // register back so ClientController can push updates easily
        if (client != null) {
            client.setGuestMainController(this);
        }
    }

    /** Updates the “Available spots” label. */
    public void updateAvailableSpots(int freeSpots) {
        Platform.runLater(() -> {
            if (availabilityLabel != null) {
                availabilityLabel.setText("Available spots: " + freeSpots);
            }
        });
    }
}
