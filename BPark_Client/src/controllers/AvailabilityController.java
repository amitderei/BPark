package controllers;

import client.ClientController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controller for the availability screen.
 * Displays total, occupied, and available parking spots.
 * Automatically fetches data when the screen is loaded.
 */
public class AvailabilityController implements ClientAware {

	/** Label that shows the total number of parking spots */
	@FXML 
	private Label lblTotal;

	/** Label that shows how many spots are currently occupied */
	@FXML 
	private Label lblOccupied;

	/** Label that shows how many spots are still available */
	@FXML 
	private Label lblAvailable;


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
			client.requestParkingAvailability();
		}
	}

	/**
	 * Called by the ClientController when availability data is received from the server.
	 * Updates the screen with the latest availability data.
	 *
	 * @param stats an array with exactly 3 values: [total, occupied, available]
	 */
	public void updateAvailability(Object[] stats) {
		// All UI updates must be run on the JavaFX thread
		Platform.runLater(() -> {
			if (stats == null || stats.length != 3) {
				return;
			}

			// Extract values from the object array
			int total     = (int) stats[0];
			int occupied  = (int) stats[1];
			int available = (int) stats[2];

			// Update UI labels
			lblTotal.setText("Total spots: " + total);
			lblOccupied.setText("Occupied: " + occupied);
			lblAvailable.setText("Available: " + available);
		});
	}
}