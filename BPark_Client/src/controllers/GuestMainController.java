package controllers;

import client.ClientController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Main screen for guest users.
 */
public class GuestMainController implements ClientAware {

    @FXML private Label welcomeLabel;
    private ClientController client;

    @FXML
    public void initialize() {
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, Guest!");
        }
    }

    @Override
    public void setClient(ClientController client) {
        this.client = client;
        if (client != null) {
            client.setGuestMainController(this);
        }
    }

    /** No counter shown – method kept for compatibility. */
    public void updateAvailableSpots(int freeSpots) {
        // intentionally left blank
    }
}
