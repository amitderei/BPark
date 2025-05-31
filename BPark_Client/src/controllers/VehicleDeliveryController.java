
package controllers;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import client.ClientController;
import common.ParkingEvent;

import javafx.scene.control.TextField;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class VehicleDeliveryController {

	@FXML
	private TextField subscriberCodeField;   // Text field that will be used for seeking the subscriber code

	@FXML
	private Label subscriberCodeLabel;   // Label for displaying network whether the subscriber code has been found or not

	@FXML
	private Button submitButton;  // Button for submitting the SubscriberCode that the user has entered.

	@FXML
	private TextField ReservationConfirmationCodeField; // TextField for entering confirmation code for an existing reservation

	@FXML
	private Label ReservationORRegularEnteranceLabel; // Label for entering to the Parking Lot regularly or threw an existing reservation

	@FXML
	private Button ReservationORRegularEnteranceButton; // Button for entering to the Parking Lot regularly or threw an existing reservation

	@FXML
	private Label ReservationConfirmationCodeLabel;  // Label for confirmation code to show case the input whether it's good or not

	@FXML
	private Label deliverStatusUpdateLabel;  // Label for confirming whether the delivery happened successfully or not

	@FXML
	private Label InsertionUpdateLabel; // Label for confirming that the insertion of parking event has been successful
	/**
	 * Triggered when the Submit button is clicked.
	 * Validates the subscriber code, and if valid, sends it to DBController for processing.
	 */

	private ClientController client;
	private int codeInt;
	private boolean hasReservation = false;
	private int confirmationCodeInt;

	// The string will be holding the Subscriber code
	private String code;

	// The string will be holding the confirmation code
	private String confirmationCode;

	// The boolean will be holding if there's a free space or no
	public boolean parkingLotStatus = false;

	// The integer will be holding the free parkingSpace that we have found to enter the vehicle into
	public int parkingSpace;

	// The String will be holding the 
	public String vehicleID;

	// This parameter waits until there will be a value received for parkingSpace
	public CompletableFuture<Integer> parkingSpaceFuture;
	
	// This parameter waits until there will be a value received for vehicleID
	public CompletableFuture<String> vehicleIdFuture;
	
	public void setClient(ClientController client) {
		this.client = client;
	}


	/**
	 * Triggered when the user clicks the Submit button.
	 * Validates the subscriber code input. If valid, sends a request to the server
	 * to check whether the subscriber exists in the database.
	 * Handles both input errors (empty or non-numeric) and server request errors.
	 */
	@FXML
	private void handleSubmit() {

		// Checking whether the code input is correct or not, if not then we won't continue.
		if(!checkIfSubscriberCodeIsValid()) {
			return;
		}

		// If the code isn't type of integer, we will let the user know that the code must be a number
		try {
			codeInt = Integer.parseInt(code);

			// Prepare the checking if exists command as an object array and send to server
			try {
				client.sendToServer(new Object[] { "subscriberExists", codeInt });
			} catch (IOException e) {
				// Log the error if the update request fails to send
				System.err.println("Failed to send 'subscriberExists' request to server: " + e.getMessage());
			}

			// There will be an exception thrown due to a String not containing only digits.
		} catch (NumberFormatException e) {
			showAlert("Subscriber code must be a number.", AlertType.ERROR);
			subscriberCodeLabel.setText("Please enter a valid input [ONLY DIGITS].");
			subscriberCodeLabel.setStyle("-fx-text-fill: red;");
			return;
		}

	}

	/**
	 * Validates the subscriber code input field.
	 * Ensures that the field is not empty or null.
	 * If invalid, shows a warning and sets the label text in red.
	 *
	 * @return true if the input is valid and not empty, false otherwise.
	 */
	private boolean checkIfSubscriberCodeIsValid() {
		this.code = subscriberCodeField.getText().trim();
		// If the code wasn't written correctly, we will let the user know that the code is empty / null
		if (code == null || code.isEmpty()) {
			// Show a warning popup if input is missing
			showAlert("Please enter a subscriber code.", AlertType.WARNING);

			// Show a message that the input of the user isn't valid
			subscriberCodeLabel.setText("Please enter a valid input");
			subscriberCodeLabel.setStyle("-fx-text-fill: red;");
			return false;
		}
		return true;
	}

	/**
	 * Called when the subscriber code is valid and exists in the database.
	 * Updates the label accordingly, disables the submit button,
	 * and proceeds to check if there's an existing reservation.
	 */
	public void subscriberCodeIsValid() {
		subscriberCodeLabel.setText("Subscriber code is valid!");
		subscriberCodeLabel.setStyle("-fx-text-fill: green;");

		// Making the button invisible so it won't be press able anymore
		submitButton.setVisible(false);
		
		// Making the Text field not write able
		subscriberCodeField.setDisable(true);

		// Now we will progress the reservation process by checking whether there is a reservation or not.
		checkIfTheresReservation();
	}

	/**
	 * Called when the subscriber code doesn't exist in the database.
	 * Updates the label to display an error message.
	 */
	public void subscriberCodeDoesntExist() {
		subscriberCodeLabel.setText("Subscriber code does not exist.");
		subscriberCodeLabel.setStyle("-fx-text-fill: red;");
	}

	/**
	 * Sends a request to the server to check if the subscriber has a valid reservation.
	 * Triggered right after subscriber code is verified.
	 */	
	@FXML
	private void checkIfTheresReservation() {
		try {
			client.sendToServer(new Object[] {"checkIfTheresReservation", codeInt});

		} catch (IOException e) {
			// Log the error if the update request fails to send
			System.err.println("Failed to send 'checkIfTheresReservation' request to server: " + e.getMessage());
		}
	}

	/**
	 * Called when a reservation exists for the current time.
	 * Makes the confirmation code field, label, and button visible to allow order-based delivery.
	 */
	public void hasReservation() {
		this.hasReservation = true;
		ReservationConfirmationCodeField.setVisible(true);
		ReservationConfirmationCodeLabel.setVisible(true);
		ReservationORRegularEnteranceLabel.setText("Enter your confirmation code [ORDER]");
		ReservationORRegularEnteranceLabel.setVisible(true);
		ReservationORRegularEnteranceButton.setVisible(true);
	}

	/**
	 * Called when no reservation exists for the current time.
	 * Makes the UI controls visible for a regular parking lot entrance (no confirmation code).
	 */
	public void NoReservation() {
		ReservationORRegularEnteranceButton.setVisible(true);
		ReservationORRegularEnteranceLabel.setVisible(true);
	}

	/**
	 * Handles the vehicle delivery process based on reservation status.
	 * If the subscriber has a reservation, it triggers the reservation-based flow.
	 * else, it processes to a regular delivery.
	 */
	@FXML
	public void handleDelivery() {
		if(hasReservation) {handleDeliveryViaReservation();}
		
		// If there's no reservation, we will check if we can enter the vehicle on a regular
		else isThereFreeParkingSpace();

	}

	/**
	 * Validates the confirmation code input from the user.
	 * Checks whether the input field is empty or null, and displays a relevant warning if needed.
	 *
	 * @return true if the confirmation code is non-empty and valid, false otherwise
	 */
	private boolean checkIfConfirmationCodeIsValid() {
		this.confirmationCode = ReservationConfirmationCodeField.getText().trim();
		// If the code wasn't written correctly, we will let the user know that the code is empty / null
		if (confirmationCode == null || confirmationCode.isEmpty()) {
			// Show a warning popup if input is missing
			showAlert("Please enter the confirmation code.", AlertType.WARNING);

			// Show a message that the input of the user isn't valid
			ReservationConfirmationCodeLabel.setText("Please enter a valid input");
			ReservationConfirmationCodeLabel.setStyle("-fx-text-fill: red;");
			return false;
		}
		return true;
	}

	/**
	 * Handles the vehicle delivery process for subscribers with a valid reservation.
	 * Validates the confirmation code, parses it to an integer, and sends a delivery request to the server.
	 * Shows appropriate error messages for invalid input or communication failure.
	 */
	private void handleDeliveryViaReservation() {
		// Checking whether the code input is correct or not, if not then we won't continue.
		if(!checkIfConfirmationCodeIsValid()) {
			return;
		}

		// If the code isn't type of integer, we will let the user know that the code must be a number
		try {
			confirmationCodeInt = Integer.parseInt(confirmationCode);

			// Prepare the checking if exists command as an object array and send to server
			try {
				client.sendToServer(new Object[] {"DeliveryViaReservation", codeInt, confirmationCodeInt});

			} catch (IOException e) {
				// Log the error if the update request fails to send
				System.err.println("Failed to send 'DeliveryViaReservation' request to server: " + e.getMessage());
			}

			// There will be an exception thrown due to a String not containing only digits.
		} catch (NumberFormatException e) {
			showAlert("Confirmation code must be a number.", AlertType.ERROR);
			ReservationConfirmationCodeLabel.setText("Please enter a valid input [ONLY DIGITS].");
			ReservationConfirmationCodeLabel.setStyle("-fx-text-fill: red;");
			return;
		}

	}

	/**
	 * Called when the Confirmation code doesn't exist in the database.
	 * Updates the label to display an error message.
	 */
	public void confirmationCodeNotValid() {
		ReservationConfirmationCodeLabel.setText("Confirmation code not currect.");
		ReservationConfirmationCodeLabel.setStyle("-fx-text-fill: red;");

		//if the delivery status label is visible we will make it not visible
		if(deliverStatusUpdateLabel.isVisible()) deliverStatusUpdateLabel.setVisible(false); 
	}

	/**
	 * Called when the Confirmation code is valid and exists in the database.
	 * Updates the label accordingly, disables the submit button,
	 * and proceeds to update the delivery inside the database.
	 */
	public void confirmationCodeIsValid() {
		ReservationConfirmationCodeLabel.setText("Confirmation code is valid!");
		ReservationConfirmationCodeLabel.setStyle("-fx-text-fill: green;");

		// Before going to the delivery process we shall check whether is there free space or not
		isThereFreeParkingSpace();
	}

	/**
	 * Handles the vehicle delivery process for subscribers without a reservation.
	 * Sends a simple delivery request to the server using the subscriber code.
	 * Displays an error if the request fails to send.
	 *
	private void handleRegularDelivery() {
		
	}
	*/
	 
	/**
	 * Generates a random 6-digit parking code as a zero-padded string.
	 * The code is created using a random integer between 0 and 999999,
	 * and always returned as a 6-character string (e.g., "004521").
	 *
	 * @return A randomly generated parking code as a 6-digit string
	 */
	public String createParkingCode() {
		Random rand=new Random();
		int parkingCode=rand.nextInt(1000000);
		return String.format("%06d", parkingCode);
	}

	/**
	 * Sends a request to the server to check whether there is free parking space in the lot named "Braude".
	 * This will trigger a server-side check on current occupancy.
	 */
	public void isThereFreeParkingSpace() {
		try {
			client.sendToServer(new Object[] {"IsThereFreeParkingSpace", "Braude"});
		} catch (IOException e) {
			// Log the error if the update request fails to send
			System.err.println("Failed to send 'IsThereFreeParkingSpace' request to server: " + e.getMessage());
		}

	}

	/**
	 * Updates the internal parking lot status based on server response.
	 *
	 * @param parkingLotStatus true if there is space available, false otherwise.
	 */
	public void setParkingLotStatus(boolean parkingLotStatus) {
		this.parkingLotStatus = parkingLotStatus;

		if(!parkingLotStatus) {
			deliverStatusUpdateLabel.setText("The Parking Lot is Full!");
			deliverStatusUpdateLabel.setStyle("-fx-text-fill: red;");
			deliverStatusUpdateLabel.setVisible(true);
		}
		else {
			deliverStatusUpdateLabel.setText("There is avaliable parking slot!");
			deliverStatusUpdateLabel.setStyle("-fx-text-fill: green;");
			deliverStatusUpdateLabel.setVisible(true);
		}

		// Starting the deliver vehicle process after knowing whether there is free space or no
		deliverVehicle();

	}

	/**
	 * Sends a request to the server to find an available parking space.
	 * The server will respond with a specific free parking spot if one exists.
	 */
	private void searchingForFreeParkingSpace() {
		try {
			client.sendToServer(new Object[] {"FindFreeParkingSpace"});

		} catch (IOException e) {
			//Log the error if the update request fails to send
			System.err.println("Failed to send 'FindFreeParkingSpace' request to server: " + e.getMessage());
		}
	}

	/**
	 * Sends a request to the server to retrieve the vehicle ID for the current subscriber.
	 */
	public void seekVehicleID() {
		try {
			client.sendToServer(new Object[] {"getVehicleID", codeInt});

		} catch (IOException e) {
			//Log the error if the update request fails to send
			System.err.println("Failed to send 'getVehicleID' request to server: " + e.getMessage());
		}
	}

	/**
	 * Attempts to deliver a vehicle into the parking lot by:
	 * 1. Verifying that there is available space.
	 * 2. Requesting a specific available parking space.
	 * 3. Generating a parking code.
	 * 4. Requesting the vehicle ID.
	 * 5. Creating a ParkingEvent object with all necessary details.
	 * 6. Sending the ParkingEvent to the server for insertion.
	 */
	public void deliverVehicle() {

		// If there are no free space then we won't allow anyone to park in the parking lot
		if(!parkingLotStatus) {return;}

		// Seeking for a specific parking space for the subscriber's vehicle
		searchingForFreeParkingSpace();

		// Generating parking code
		String parkingCode = createParkingCode();

		// Initialize the CompletableFutures
		parkingSpaceFuture = new CompletableFuture<>();
	    vehicleIdFuture = new CompletableFuture<>();

		// When parkingSpace is received, we will seek for the vehicleID
		parkingSpaceFuture.thenAccept(parkingSpace -> {
			seekVehicleID();
			
			// When vehicle ID is received, create the ParkingEvent and send it
			vehicleIdFuture.thenAcceptAsync(vehicleID -> {
				// Get the current date and time in Israel
				
				
				ZonedDateTime nowInIsrael = ZonedDateTime.now(ZoneId.of("Asia/Jerusalem"));
				LocalDate entryDate = nowInIsrael.toLocalDate();
				LocalTime entryTime = nowInIsrael.toLocalTime();


				// Creating a ParkingEvent entity, will send the object to the server
				ParkingEvent parkingEvent = new ParkingEvent(codeInt, parkingSpace, entryDate, entryTime, null, null, false, vehicleID, "Braude", parkingCode);


				// Sending to the Server Parking Event that contains every field except exitDate and exitHour: we can't tell by now which values they'll hold
				// both of these fields will hold null
				try {
					client.sendToServer(new Object[] {"DeliverVehicle", parkingEvent});

				} catch (IOException e) {
					//Log the error if the update request fails to send
					System.err.println("Failed to send 'DeliverVehicle' request to server: " + e.getMessage());
				}
			});
		});
		
	}

	/**
	 * Updates the delivery status label in the UI to indicate a successful delivery.
	 * The message is displayed in green to visually confirm completion to the user.
	 */
	public void successfulDelivery() {
		InsertionUpdateLabel.setText("Completed delivery successfully!");
		InsertionUpdateLabel.setStyle("-fx-text-fill: green;");
		InsertionUpdateLabel.setVisible(true);

		// Making the button invisible so it won't be press able anymore
		ReservationORRegularEnteranceButton.setVisible(false);
		
		// Making the Text field not write able
		ReservationConfirmationCodeField.setDisable(true);
	}

	/**
	 * Utility method for displaying pop-up alerts (message boxes) in the GUI.
	 *
	 * @param message The message to show in the alert dialog.
	 * @param type    The type of alert (INFORMATION, WARNING, or ERROR).
	 */
	private void showAlert(String message, Alert.AlertType type) {
		// Create a new Alert of the specified type (e.g., INFO, WARNING, ERROR)
		Alert alert = new Alert(type);

		// Set a consistent title for all alert popups
		alert.setTitle("BPARK - Message");

		// No header text (cleaner look)
		alert.setHeaderText(null);

		// Set the actual message to be shown in the dialog box
		alert.setContentText(message);

		// Display the alert window and wait until user closes it
		alert.show();
	}


}