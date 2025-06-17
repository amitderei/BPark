package controllers;

import client.ClientController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controller for the screen that displays real-time parking availability.
 * This version provides a simple summary of total, occupied, and free spots.
 */
public class AvailabilityController implements ClientAware {

    @FXML private Label lblTotal;
    @FXML private Label lblOccupied;
    @FXML private Label lblAvailable;
    @FXML private Label lblStatus;

    private ClientController client;

    @Override
    public void setClient(ClientController client) {
        this.client = client;
        if (client != null) {
            client.setAvailabilityController(this);
            client.requestParkingAvailability(); 
        }
    }

    /**
     * Updates the labels with parking availability stats.
     *
     * @param stats Object array: [totalSpots, occupiedSpots, availableSpots]
     */
    public void updateAvailability(Object[] stats) {
        Platform.runLater(() -> {
            if (stats == null || stats.length != 3) {
                lblStatus.setText("Failed to load availability.");
                return;
            }

            int total = (int) stats[0];
            int occupied = (int) stats[1];
            int available = (int) stats[2];

            lblTotal.setText("Total spots: " + total);
            lblOccupied.setText("Occupied: " + occupied);
            lblAvailable.setText("Available: " + available);
            lblStatus.setText("Availability data loaded.");
        });
    }
}
