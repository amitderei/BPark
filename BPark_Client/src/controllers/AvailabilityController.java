package controllers;

import client.ClientController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controller for the parking availability screen.
 * 
 * Shows how many spots exist, how many are currently in use,
 * how many are reserved soon, and how many are truly free.
 * 
 * This controller fetches the data from the server automatically when it loads.
 */
public class AvailabilityController implements ClientAware {

    /** Shows total number of spots in the lot */
    @FXML
    private Label lblTotal;

    /** Shows how many spots are currently taken */
    @FXML
    private Label lblOccupied;

    /** Shows how many spots are available right now (not taken and not reserved) */
    @FXML
    private Label lblAvailable;

    /** Shows how many spots are about to be used soon (in next 4 hours) */
    @FXML
    private Label lblUpcoming;;

	/** Reference to the main client used to communicate with the server */
	private ClientController client;

	/**
	 * Called automatically after screen load to inject the shared client instance.
	 * Also registers this controller in the client for callbacks,
	 * and sends the first request for availability data.
	 *
	 * @param client the active ClientController, or null if not connected yet
	 */
	@Override
	public void setClient(ClientController client) {
		this.client = client;

		if (client != null) {
			// Let the client know we're the active AvailabilityController
			client.setAvailabilityController(this);

			// Request availability data from the server
			client.getRequestSender().requestParkingAvailability();
		}
	}

	/**
     * This is called by the client when the server replies with parking stats.
     * 
     * We get 4 numbers:
     * - total spots
     * - how many are occupied
     * - how many are reserved in the next 4 hours
     * - how many are available (the real useful number!)
     *
     * @param stats array that must contain 4 values: [total, occupied, upcoming, available]
     */
    public void updateAvailability(Object[] stats) {
        // Always update UI elements from the JavaFX thread
        Platform.runLater(() -> {
            if (stats == null || stats.length != 4) {
                return; // nothing to update
            }

            // Extract values from the response
            int total     = (int) stats[0];
            int occupied  = (int) stats[1];
            int upcoming  = (int) stats[2];
            int available = (int) stats[3];

            // Show all the numbers on screen
            lblTotal.setText("Total spots: " + total);
            lblOccupied.setText("Occupied: " + occupied);
            lblUpcoming.setText("Upcoming reservations (next 4 hours): " + upcoming);
            lblAvailable.setText("Available: " + available);
        });
	}

}