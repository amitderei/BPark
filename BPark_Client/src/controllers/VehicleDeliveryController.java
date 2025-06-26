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

/**
 * VehicleDeliveryController class responsible for the process of delivering the vehicles in the BPARK system.
 *
 * This class handles two main entry methods for subscribers:
 * - Delivery using a manually entered subscriber code
 * - Delivery using a tag
 *
 * If the delivery is handled by entering the subscriber code it will check whether the subscriber has a reservation or not
 * - If the subscriber has an active reservation he must enter also the parkingCode that is referred to that specific reservation.
 * - If the subscriber doesn't have an active reservation he will enter regularly if there's free parking space or not.
 *
 * The class also handles GUI updates in JavaFX, including status labels, field disabling,
 * The class has visibility toggling, and error messages to guide the user through the delivery process.
 *
 * Main features:
 * - Validates subscriber code and tag input
 * - Checks if a subscriber has a reservation
 * - Retrieves vehicle ID that is matched to the subscriber
 * - Retrieves an available parking space from the server (if there are any free parking spaces at all)
 * - Creates and sends ParkingEvent objects for insertion
 * - Updates the UI on success/failure of delivery
 *
 * This controller is connected to the Vehicle_delivery_screen (FXML).
 * 
 */

public class VehicleDeliveryController implements ClientAware{

	/*SUBSCRIBER CODE CHECKINGS AND VISIBILITY TOGGLINGS*/
	@FXML
	private TextField subscriberCodeField;   // Text field that will be used for seeking the subscriber code

	@FXML
	private Button submitButton;  // Button for submitting the SubscriberCode that the user has entered.

	@FXML
	private Label subscriberCodeLabel;   // Label for displaying status whether the subscriber code has been found or not


	/*DELIVERY AFTER ENTERING SUBSCRIBER CODE, ENTERING CONFIRMATION CODE IF THERE'S RESERVATION */
	@FXML
	private Label ReservationORRegularEnteranceLabel; // Label for entering to the Parking Lot regularly or threw an existing reservation

	@FXML
	private TextField ReservationConfirmationCodeField; // TextField for entering confirmation code for an existing reservation

	@FXML
	private Button ReservationORRegularEnteranceButton; // Button for entering to the Parking Lot regularly or threw an existing reservation

	@FXML
	private Label ReservationConfirmationCodeLabel;  // Label for confirmation code to show case the input whether it's good or not

	/*TAG CHECKINGS AND VISIBILITY TOGGLINGS*/
	@FXML
	private TextField TagCodeField; // TextField for delivering the vehicle with the tag

	@FXML
	private Button submitTagButton; // Button for submitting delivery of a vehicle with the tag

	@FXML
	private Label TagStatusUpdateLabel; // Label for confirming whether the delivery (with the tag scan) happened successfully or not

	/*IF VEHICLE IS ENTERED ALREADY WILL VISIBILITY TOGGLINGS*/
	@FXML
	private Label vehicleHasEnteredLabel; // Label for showing whether the vehicle is already inside or not

	/*AFTER PRESSING DELIVER VIA SUBSCRIBER CODE OR VERIFY TAG AND DELIVER => FOR STATUS*/
	@FXML
	private Label deliverStatusUpdateLabel;  // Label for confirming whether the delivery happened successfully or not

	@FXML
	private Label InsertionUpdateLabel; // Label for confirming that the insertion of parking event has been successful

	@FXML
	private Label parkingCodeLabel; // Label for matching the parkingCode of the subscriber so he will know it for the pickup of the vehicle

	/****************************************/

	private ClientController client;

	// The string will be holding the Subscriber code
	private String code;

	// The integer will be holding the Subscriber code
	private int codeInt; 

	// The boolean variable will be having true if the subscriber has a reservation or false if he doesn't have
	private boolean hasReservation = false;

	// The integer will be holding the Confirmation code
	private String confirmationCode;

	// The String will be holding the Parking code after deliver
	private String parkingCode;

	// The integer will be holding the Confirmation code
	private int confirmationCodeInt;

	// The string will be holding the tag-id
	private String tag;

	// THe integer will be holding the parkingSpace
	private int parkingSpace;

	// The boolean will be holding true if there's a free space or false if no
	public boolean parkingLotStatus = false;

	// This parameter waits until there will be a value received for vehicleID
	public CompletableFuture<String> vehicleIdFuture;

	// This parameter waits until there will be a value received for matching subscriber for the tag
	public CompletableFuture<Integer> subCodeFuture;

	public void setClient(ClientController client) {
		this.client = client;
	}

	/****************************************/

	/**
	 * The method triggered when the Submit button is clicked
	 * Validates the subscriber code, if isn't empty and is a number then sends it to DBController to proceed for the other checking
	 */
	@FXML
	private void handleSubmit() {

		// Checking whether the code input is correct or not, if not then we won't continue.
		if(!checkIfSubscriberCodeIsValid()) {
			return;
		}

		try {
			// If the code isn't type of integer, we will let the user know that the code must be a number
			codeInt = Integer.parseInt(code);

			client.checkSubscriberExists(codeInt);

			// There will be an exception thrown due to a String not containing only digits.
		} catch (NumberFormatException e) {
			showAlert("Subscriber code must be a number.", AlertType.ERROR);
			
			// If a NumberFormatException was thrown it means that the input isn't by numbers
			subscriberCodeLabel.setText("Please enter a valid input [ONLY DIGITS].");
			subscriberCodeLabel.setStyle("-fx-text-fill: red;");
			subscriberCodeLabel.setVisible(true);

			// Hide the label that shows status for the tag delivery
			TagStatusUpdateLabel.setVisible(false);
			return;
		}

	}

	/**
	 * Validates the subscriber code input field.
	 * If invalid, shows a warning and sets the label text in red.
	 *
	 * @return true if the input is valid and not empty, false otherwise.
	 */
	private boolean checkIfSubscriberCodeIsValid() {
		this.code = subscriberCodeField.getText().trim();
		// If the subscriber code field is empty or null, we will let the user know that the input is invalid
		if (code == null || code.isEmpty()) {
			// Show a warning popup if input is missing
			showAlert("Please enter a subscriber code.", AlertType.WARNING);

			// Setting label if the input is null
			subscriberCodeLabel.setText("Please enter a valid input");
			subscriberCodeLabel.setStyle("-fx-text-fill: red;");
			subscriberCodeLabel.setVisible(true);

			// Hide the label that shows status for the tag delivery
			TagStatusUpdateLabel.setVisible(false);
			return false;
		}
		return true;
	}

	/**
	 * The method triggered when the Tag Delivery button is clicked
	 * Validates the tag - id, if isn't empty then sends it to DBController to proceed for the other checking
	 */
	@FXML
	private void handleTagDelivery() {
		// Checking whether the tag input is correct or not, if not then we won't continue.
		if(!checkIfTagIsValid()) {
			return;
		}

		client.validateTag(tag);
	}

	/**
	 * Validates the tag - id input field.
	 * If invalid, shows a warning and sets the label text in red.
	 *
	 * @return true if the input is valid and not empty, false otherwise.
	 */
	private boolean checkIfTagIsValid() {
		this.tag = TagCodeField.getText().trim();
		// If the tag - id field is empty or null, we will let the user know that the input is invalid
		if (tag == null || tag.isEmpty()) {
			// Show a warning popup if input is missing
			showAlert("Please enter the Tag number.", AlertType.WARNING);

			// Setting label if the input is null
			TagStatusUpdateLabel.setText("Please enter a valid input");
			TagStatusUpdateLabel.setStyle("-fx-text-fill: red;");
			TagStatusUpdateLabel.setVisible(true);

			// Hide the label that shows status for the subscriber code delivery
			subscriberCodeLabel.setVisible(false);
			return false;
		}
		return true;
	}

	/**
	 * Method called when the subscriber code is valid and exists in the database.
	 * Updates the label that showcase's the subscriber code status, and proceeds to check if there's an existing reservation.
	 */
	public void subscriberCodeIsValid() {
		// Setting label if the subscriber code is valid
		subscriberCodeLabel.setText("Subscriber code is valid!");
		subscriberCodeLabel.setStyle("-fx-text-fill: green;");
		subscriberCodeLabel.setVisible(true);

		// Hide the label that shows status for the subscriber code delivery
		TagStatusUpdateLabel.setVisible(false);

		// Making the button invisible so it won't be press able anymore
		submitButton.setVisible(false);

		// Making the Text field not write able
		subscriberCodeField.setDisable(true);

		// Making the button and the text field of the tag to be invisible and making the Text field of the tag not write able
		TagStatusUpdateLabel.setVisible(false);
		submitTagButton.setVisible(false);
		TagCodeField.setDisable(true);

		// We will check whether the subscriber has already delivered his vehicle or not
		client.checkIfSubscriberAlreadyEntered(codeInt);
	}

	/**
	 * Called when the subscriber code doesn't exist in the database.
	 * Updates the label to display an error message.
	 */
	public void subscriberCodeDoesntExist() {
		// Setting label if the subscriber code does not exist
		subscriberCodeLabel.setText("Subscriber code does not exist.");
		subscriberCodeLabel.setStyle("-fx-text-fill: red;");
		subscriberCodeLabel.setVisible(true);

		// Hide the label that shows status for the tag delivery
		TagStatusUpdateLabel.setVisible(false);
	}

	/**
	 * Updates the delivery status label in the UI to indicate a failed attempt after noticing that the vehicle is already inside the parking lot
	 * All of the buttons will be invisible, and all of the text fields will be disabled
	 * Label will be updated and will show that the vehicle is already inside
	 */
	public void vehicleIsAlreadyInside() {
		// Every GUI aspect that is a button/field will be invisible/disable
		subscriberCodeField.setDisable(true);
		submitButton.setVisible(false);

		ReservationConfirmationCodeField.setDisable(true);
		ReservationORRegularEnteranceButton.setVisible(false);

		TagCodeField.setDisable(true);
		submitTagButton.setVisible(false);

		// Sets up the label that will tell the user that the vehicle is already inside
		vehicleHasEnteredLabel.setVisible(true);
		vehicleHasEnteredLabel.setStyle("-fx-text-fill: red;");
	}


	/**
	 * Sends a request to the server to check if the subscriber has a valid reservation.
	 * The method is triggered after the server responses that the subscriber didn't enter his vehicle yet.
	 */	
	public void checkIfTheresReservation() {
		client.isThereReservation(codeInt);
	}

	/**
	 * Called when a reservation exists for the current time.
	 * Makes the confirmation code field, label, and button visible to allow order-based delivery.
	 */
	public void hasReservation() {
		this.hasReservation = true;

		// Making every aspect of GUI that connects to the reservation visible
		ReservationORRegularEnteranceLabel.setText("Enter your confirmation code [ORDER]");
		ReservationConfirmationCodeField.setVisible(true);
		ReservationConfirmationCodeLabel.setVisible(true);
		ReservationORRegularEnteranceLabel.setVisible(true);
		ReservationORRegularEnteranceButton.setVisible(true);
	}

	/**
	 * Called when a reservation exists for the current time.
	 * Makes only the label and deliver button visible to allow order-based delivery.
	 */
	public void NoReservation() {
		// Making every aspect of GUI that is relevant to no reservation delivery visible
		ReservationORRegularEnteranceLabel.setVisible(true);
		ReservationORRegularEnteranceButton.setVisible(true);
	}

	/**
	 * Called when a tag input has been found in the data base.
	 * Makes only the label and tag deliver button visible to allow order-based delivery, everything else will be disable/invisible.
	 * After the status changes we will seek for the matching subscriber.
	 */
	public void tagFound() {
		// Setting label if the Tag has been found
		TagStatusUpdateLabel.setText("Tag has found!");
		TagStatusUpdateLabel.setStyle("-fx-text-fill: green;");
		TagStatusUpdateLabel.setVisible(true);

		// Every GUI aspect that is relevant to vehicle delivering via subscriber code will be disabled / invisible
		subscriberCodeField.setDisable(true);
		submitButton.setVisible(false);
		subscriberCodeLabel.setVisible(false);

		// Every GUI aspect that is relevant to the reservation entry / regular vehicle delivery via subscriber code will be invisible
		ReservationConfirmationCodeField.setVisible(false);
		ReservationConfirmationCodeLabel.setVisible(false);
		ReservationORRegularEnteranceLabel.setVisible(false);
		ReservationORRegularEnteranceButton.setVisible(false);

		// We will disable everything else, we don't want to deliver when we already delivered a vehicle successfully
		deliverStatusUpdateLabel.setVisible(false);
		InsertionUpdateLabel.setVisible(false);

		// After all of the updates to the visuals, we will find out if the vehicle that is matched to the tag-Id is already in the parking lot
		client.checkIfTagIDAlreadyInside(tag);
	}

	/**
	 * Called when a tag input hasn't been found in the data base.
	 * Makes the label that represents the status of the delivery via tag-id red and shows an error.
	 */
	public void tagNotFound() {
		// Setting label if the TagID does not exist
		TagStatusUpdateLabel.setText("Tag doesn't exist");
		TagStatusUpdateLabel.setStyle("-fx-text-fill: red;");
		TagStatusUpdateLabel.setVisible(true);

		// Hide the label that shows status for the subscriber code delivery
		subscriberCodeLabel.setVisible(false);

		// We will disable everything else, we don't want to deliver when we already delivered a vehicle successfully
		deliverStatusUpdateLabel.setVisible(false);
		InsertionUpdateLabel.setVisible(false);
	}

	/**
	 * Called after changing the status of the tag - id to be found.
	 * The method is triggered after the server responses that the subscriber didn't enter his vehicle yet
	 * Seeking for the matching subscriber so we will be able to proceed to the vehicle delivery.
	 */
	public void findMatchedSubToTheTag() {
			client.findSubscriberWithTag(tag);

			// Initialize the CompletableFutures
			subCodeFuture = new CompletableFuture<>();

			// When we have the tag, we will seek for the subscriber code
			subCodeFuture.thenAccept(codeInt -> {
				this.codeInt = codeInt;
				// After gathering the subscriber code we will go and check whether there are free space in our parking lot
				client.isThereFreeParkingSpace("braude");
			});
	}

	/**
	 * The method triggered when the Delivery button (via subscriber code) is clicked
	 * If the subscriber has a reservation, it triggers the delivery via reservation.
	 * else, it processes to a regular delivery.
	 */
	@FXML
	public synchronized void handleDelivery() {
		if(hasReservation) {handleDeliveryViaReservation();}

		// If there's no reservation, we will check if we can enter the vehicle on a regular
		else client.isThereFreeParkingSpace("braude");


	}

	/**
	 * Handles the vehicle delivery process for subscribers with an active and relevant reservation.
	 * Validates the confirmation code, changing it to an integer, and sends a delivery request to the server.
	 * Shows an error messages for invalid input or communication failure.
	 */
	private void handleDeliveryViaReservation() {

		// Checking whether the code input is correct or not, if not then we won't continue.
		if(!checkIfConfirmationCodeIsValid()) {
			return;
		}

		// If the code isn't type of integer, we will let the user know that the code must be a number
		try {
			confirmationCodeInt = Integer.parseInt(confirmationCode);

			client.DeliveryViaReservation(codeInt, confirmationCodeInt);

			// There will be an exception thrown due to a String not containing only digits
		} catch (NumberFormatException e) {
			showAlert("Confirmation code must be a number.", AlertType.ERROR);
			ReservationConfirmationCodeLabel.setText("Please enter a valid input [ONLY DIGITS].");
			ReservationConfirmationCodeLabel.setStyle("-fx-text-fill: red;");
			return;
		}

	}

	/**
	 * Validates the confirmation code input from the user.
	 * If the subscriber code field is empty or null, we will let the user know that the input is invalid
	 * If the confirmation code is non-empty and valid return true, false otherwise
	 */
	private boolean checkIfConfirmationCodeIsValid() {
		// Setting our confirmationCode field in this class to the code that is inside the text field without any spaces
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
	 * Called when the Confirmation code does exists in the database.
	 * Updates the label to display that the confirmation code is valid, disables the submit button,
	 * and proceeds to update the delivery inside the database.
	 */
	public void confirmationCodeIsValid() {
		ReservationConfirmationCodeLabel.setText("Confirmation code is valid!");
		ReservationConfirmationCodeLabel.setStyle("-fx-text-fill: green;");

		// Before going to the delivery process we shall check whether is there free space or not
		client.isThereFreeParkingSpace("braude");
	}

	/**
	 * Called when the Confirmation code doesn't exist in the database.
	 * Updates the label to display an error message.
	 */
	public void confirmationCodeNotValid() {
		ReservationConfirmationCodeLabel.setText("Confirmation code not currect.");
		ReservationConfirmationCodeLabel.setStyle("-fx-text-fill: red;");
		deliverStatusUpdateLabel.setVisible(false); 
	}

	/**
	 * Generates a random 6-digit parking code as a zero-padded string.
	 * The code is created using a random integer between 0 and 999999 - (e.g., "004521") and returns it
	 */
	public String createParkingCode() {
		Random rand=new Random();
		int parkingCode=rand.nextInt(1000000);
		return String.format("%06d", parkingCode);
	}

	/**
	 * A setter for the parking space ID
	 */
	public void setParkingSpace(int parkingSpace) {
		this.parkingSpace = parkingSpace;
	}

	/**
	 * Updates the internal parking lot status based on server response, if there is no free space then the labels will show the status.
	 * If there is free space inside the parking lot then the method will continue to the vehicle delivery.
	 */
	public void setParkingLotStatus(boolean parkingLotStatus) {
		this.parkingLotStatus = parkingLotStatus;

		if(!parkingLotStatus) {
			// Setting label if the parking lot is full
			deliverStatusUpdateLabel.setText("The Parking Lot is Full!");
			deliverStatusUpdateLabel.setStyle("-fx-text-fill: red;");
			deliverStatusUpdateLabel.setVisible(true);

			subscriberCodeField.setDisable(true);
			TagCodeField.setDisable(true);

			submitButton.setVisible(false);
			submitTagButton.setVisible(false);


			subscriberCodeLabel.setVisible(false);
			ReservationORRegularEnteranceLabel.setVisible(false);
			ReservationConfirmationCodeField.setVisible(false);
			ReservationORRegularEnteranceButton.setVisible(false);
			ReservationConfirmationCodeLabel.setVisible(false);
			InsertionUpdateLabel.setVisible(false);
			TagStatusUpdateLabel.setVisible(false);
			parkingCodeLabel.setVisible(false);
			vehicleHasEnteredLabel.setVisible(false);


		}
		else {
			// Setting label if there is available parking slot
			deliverStatusUpdateLabel.setText("There is avaliable parking slot!");
			deliverStatusUpdateLabel.setStyle("-fx-text-fill: green;");
			deliverStatusUpdateLabel.setVisible(true);
		}

		// Starting the deliver vehicle process after knowing whether there is free space or no
		deliverVehicle();

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

		// Generating parking code
		parkingCode = createParkingCode();

		// Initialize the CompletableFuture of the vehicleID
		vehicleIdFuture = new CompletableFuture<>();

		// We will seek for the vehicleID before going and making the parking event object
		client.seekVehicleID(codeInt);

		// When vehicle ID is received, create the ParkingEvent and send it
		vehicleIdFuture.thenAcceptAsync(vehicleID -> {
			// Get the current date and time in Israel


			ZonedDateTime nowInIsrael = ZonedDateTime.now(ZoneId.of("Asia/Jerusalem"));
			LocalDate entryDate = nowInIsrael.toLocalDate();
			LocalTime entryTime = nowInIsrael.toLocalTime();

			// Creating a ParkingEvent entity, will send the object to the server
			ParkingEvent parkingEvent = new ParkingEvent(codeInt, parkingSpace, entryDate, entryTime, null, null, false, vehicleID, "Braude", parkingCode);


			client.deliverVehicle(parkingEvent);
			
		});
	}

	/**
	 * Updates the delivery status label in the UI to indicate a successful delivery.
	 * The message is displayed in green to visually confirm completion to the user.
	 */
	public void successfulDelivery() {
		// Setting label if the delivery has completed successfully
		InsertionUpdateLabel.setText("Completed delivery successfully!");
		InsertionUpdateLabel.setStyle("-fx-text-fill: green;");
		InsertionUpdateLabel.setVisible(true);

		// Every GUI aspect that is a button/field will be invisible/disable
		subscriberCodeField.setDisable(true);
		submitButton.setVisible(false);

		ReservationConfirmationCodeField.setDisable(true);
		ReservationORRegularEnteranceButton.setVisible(false);

		TagCodeField.setDisable(true);
		submitTagButton.setVisible(false);

		// Setting the label for showing the parking code to the subscriber
		parkingCodeLabel.setText("Your Parking Code is : " + parkingCode);
		parkingCodeLabel.setStyle("-fx-text-fill: green;");
		parkingCodeLabel.setVisible(true);

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