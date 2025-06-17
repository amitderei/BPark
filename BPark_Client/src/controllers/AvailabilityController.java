package controllers;

import client.ClientController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controller that shows a live snapshot of parking-lot capacity:
 * total spots, how many are occupied, and how many remain free.
 * Connects to the server once at start-up and refreshes the labels
 * when new statistics arrive.
 */
public class AvailabilityController implements ClientAware {

    /* ===== UI labels bound to the FXML layout ===== */
    @FXML private Label lblTotal;
    @FXML private Label lblOccupied;
    @FXML private Label lblAvailable;
    @FXML private Label lblStatus;

    /** Central client object used to send the availability request */
    private ClientController client;

    /**
     * Injects the shared client instance, registers this controller
     * inside the client, and immediately asks the server for stats.
     *
     * @param client active ClientController, or null if not connected yet
     */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
        if (client != null) {
            client.setAvailabilityController(this);   // so the client can call back
            client.requestParkingAvailability();       // initial fetch
        }
    }

    /**
     * Called by ClientController when fresh availability data arrives.
     * Converts the generic Object array into ints and updates the labels.
     * Runs on the JavaFX Application Thread via Platform.runLater.
     *
     * @param stats array containing totalSpots, occupiedSpots, availableSpots
     *              in that exact order; null or wrong length triggers an error
     */
    public void updateAvailability(Object[] stats) {
        Platform.runLater(() -> {               // UI updates must be on FX thread
            if (stats == null || stats.length != 3) {
                lblStatus.setText("Failed to load availability.");
                return;
            }

            int total     = (int) stats[0];
            int occupied  = (int) stats[1];
            int available = (int) stats[2];

            lblTotal.setText("Total spots: " + total);
            lblOccupied.setText("Occupied: " + occupied);
            lblAvailable.setText("Available: " + available);
            lblStatus.setText("Availability data loaded.");
        });
    }
}
