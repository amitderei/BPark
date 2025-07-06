package controllers;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import client.ClientController;
import common.Order;
import common.StatusOfOrder;
import common.Subscriber;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * Handles the "Create New Reservation" workflow for subscribers
 * Collects date, time and subscriber code, checks
 * availability with the server, and finally builds an Order object.
 *
 * Screen flow:
 *  1. User picks a date (1-8 days from now) and hour + quarter hour.
 *  2. Client asks the server if the slot is free.
 *  3. If free, an Order is created and passed to the summary screen.
 */
public class CreateNewOrderViewController implements ClientAware {
	
	@FXML 
	private Label headlineParkingReservation;
	@FXML 
	private Label chooseDateAndTime;
	@FXML 
	private Label betweenHourAndMinute;
	@FXML 
	private Label subscriberCode;
	@FXML 
	private TextField insertSubscriberCode;
	@FXML 
	private Label notPossibleToOrder;
	@FXML 
	private DatePicker chooseDate;
	@FXML 
	private ComboBox<String> hourCombo;
	@FXML 
	private ComboBox<String> minuteCombo;
	@FXML 
	private CheckBox checkBox;
	@FXML 
	private Hyperlink termsOfUseHyper;
	@FXML 
	private Button reserveNowButton;

	/** Reference to the subscriber's main layout controller (for screen switching). */
	private SubscriberMainLayoutController mainLayoutController;

	/** Active client instance used for communication with the server. */
	private ClientController client;

	/** Order instance created once all details are validated */
	public Order newOrder;

	/** Variables to save the information of the order*/
	int subscriberNum;
	Date selectedDate;
	Time timeOfArrival;

	/** This parameter waits until there will be a value received for orderExistFuture */	
	public CompletableFuture<Boolean> orderExistFuture;
	
	/**
	 * Injects the active client controller.
	 *
	 * @param client the current ClientController instance
	 */
	@Override
	public void setClient(ClientController client) {
		this.client = client;
	}

	/**
	 * Called once on screen load.
	 * Initializes the date picker and combo boxes with valid ranges.
	 */
	public void initializeCombo() {

		// Autofill subscriber code for convenience
		insertSubscriberCode.setText(
				String.valueOf(client.getSubscriber().getSubscriberCode()));

		hourCombo.getItems().clear();
		minuteCombo.getItems().clear();

		LocalDate today     = LocalDate.now();
		LocalDate tomorrow  = today.plusDays(1);
		LocalDate nextWeek  = today.plusDays(7);  // inclusive upper bound

		// Disable dates outside [tomorrow..nextWeek]
		chooseDate.setDayCellFactory((Callback<DatePicker, DateCell>) picker -> new DateCell() {
			@Override
			public void updateItem(LocalDate date, boolean empty) {
				super.updateItem(date, empty);
				if (empty || date.isBefore(tomorrow) || date.isAfter(nextWeek)) {
					setDisable(true);
				}
			}
		});
		
		
	    // Prevent manual typing inside the DatePicker field (force user to use the calendar)
	    chooseDate.getEditor().setDisable(true);   // disable text field input
	    chooseDate.getEditor().setOpacity(1);      // keep it visually enabled (not greyed out)

		insertSubscriberCode.setDisable(true);
	}

	/**
	 * Called when the user selects a date.
	 * Fills hour dropdown based on the selected day and current time.
	 */
	public void dateChosen() {

		LocalDate date      = chooseDate.getValue();
		LocalDate tomorrow  = LocalDate.now().plusDays(1);
		LocalDate nextWeek  = LocalDate.now().plusDays(7);
		int currentHour     = LocalTime.now().getHour();

		hourCombo.getItems().clear();
		minuteCombo.getItems().clear();

		if (date.equals(tomorrow)) {
			for (int h = currentHour; h < 24; h++)
				hourCombo.getItems().add(String.format("%02d", h));
		} else if (date.equals(nextWeek)) {
			for (int h = 0; h <= currentHour; h++)
				hourCombo.getItems().add(String.format("%02d", h));
		} else {
			for (int h = 0; h < 24; h++)
				hourCombo.getItems().add(String.format("%02d", h));
		}
	}

	/** Updates the minute selector in 15-minute increments when an hour is chosen. */
	public void hourChoosen() {

		LocalDate date      = chooseDate.getValue();
		LocalDate tomorrow  = LocalDate.now().plusDays(1);
		LocalDate nextWeek  = LocalDate.now().plusDays(7);

		int currentHour     = LocalTime.now().getHour();
		int currentMinute   = LocalTime.now().getMinute();
		String pickedHour   = hourCombo.getValue();

		minuteCombo.getItems().clear();

		// Inner helper λ to decide if a quarter is allowed
		java.util.function.IntConsumer addQuarter = q -> minuteCombo.getItems()
				.add(String.format("%02d", q * 15));

		if ((date.equals(tomorrow) && pickedHour.equals(String.format("%02d", currentHour)))) {
			for (int q = 0; q < 4; q++)
				if (q * 15 >= currentMinute) addQuarter.accept(q);

		} else if ((date.equals(nextWeek) && pickedHour.equals(String.format("%02d", currentHour)))) {
			for (int q = 0; q < 4; q++)
				if (q * 15 <= currentMinute) addQuarter.accept(q);

		} else {
			for (int q = 0; q < 4; q++) addQuarter.accept(q);
		}
	}

	/**
	 * Creates an Order and asks the server to save it.
	 * Shows validation warnings for missing fields or terms box.
	 */
	public void addNewOrder() {

		if (!checkBox.isSelected()) {
			showAlert("Please agree to the terms of use", Alert.AlertType.WARNING);
			return;
		}

		// Read subscriber code
		subscriberNum = Integer.parseInt(insertSubscriberCode.getText().trim());

		// Validate date
		if (chooseDate.getValue() == null) {
			showAlert("Please select a date and hour.", Alert.AlertType.WARNING);
			return;
		}
		selectedDate = Date.valueOf(chooseDate.getValue());

		// Validate time components
		if (hourCombo.getValue() == null || minuteCombo.getValue() == null) {
			showAlert("Please select a hour.", Alert.AlertType.WARNING);
			return;
		}
		LocalTime time     = LocalTime.of(
				Integer.parseInt(hourCombo.getValue()),
				Integer.parseInt(minuteCombo.getValue()));
		timeOfArrival = Time.valueOf(time);

		orderExistFuture = new CompletableFuture<>();

		// Ask server if there is an already existing order for the asked date and time
		client.getRequestSender().checkIfOrderAlreadyExists(client.getSubscriber().getSubscriberCode(), selectedDate, timeOfArrival);

		// When the boolean received, we will check if there is available space in the selected date and time
		orderExistFuture.thenAcceptAsync(orderExists -> {

			if(!orderExists) {
				// Ask server if slot is still free (if the order doesn't exists already)
				client.getRequestSender().checkAvailability(selectedDate, timeOfArrival);
			}
		});
	}

	/**
	 * Called after server confirms availability.
	 * Creates a new Order object and sends it to the server,
	 * or shows an error message if not possible.
	 *
	 * @param canOrder true if the slot is available
	 */
	public void makingReservation(boolean canOrder) {    	

		// If it's possible to make an order we will save the order in our DB 
		if(canOrder) {

			// Build Order and send to server
			int randomCode = new Random().nextInt(1_000_000);
			String formattedCode = String.format("%06d", randomCode);
			newOrder = new Order(
					1,               // dummy order number (server assigns real PK)
					55,              // dummy parking space
					selectedDate,
					timeOfArrival,
					formattedCode,
					subscriberNum,
					Date.valueOf(LocalDate.now()), StatusOfOrder.ACTIVE);

			client.getRequestSender().addNewOrder(newOrder);
		}
		else {
			notPossibleToOrder.setText("Can't make an order on this time");	
			notPossibleToOrder.setVisible(true);
		}
	}

	/**
	 * Shows a modal dialog with the terms of use for the parking lot.
	 * The dialog is read-only and scrollable.
	 */
	public void showTermsOfUse() {
		Dialog<Void> dialog = new Dialog<>();
		dialog.setTitle("Terms Of Use");
		dialog.setHeaderText("Please read the Terms of Use.");

		TextArea textArea = new TextArea(returnTermsOfUse());
		textArea.setWrapText(true);
		textArea.setEditable(false);
		textArea.setPrefWidth(600);
		textArea.setPrefHeight(400);

		dialog.getDialogPane().setContent(textArea);
		dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
		dialog.showAndWait();
	}

	/**
	 * Returns the full text of the terms of use.
	 *
	 * @return string containing formatted terms
	 */
	private String returnTermsOfUse() {
		StringBuilder strBuild=new StringBuilder();
		strBuild.append("Terms of Use – BPARK Automated Parking Lot\r\n\r\n");

		strBuild.append("Welcome to the BPARK automated parking lot (hereinafter: \"the Parking Lot\").\r\n");
		strBuild.append("Use of the parking lot services, including making a reservation and entering the facility,\r\n");
		strBuild.append("is subject to the following terms. By placing a reservation or using the parking lot,\r\n");
		strBuild.append("the user confirms that they have read, understood, and agreed to these terms in full.\r\n\r\n");

		strBuild.append("1. Definitions\r\n");
		strBuild.append("1.1 \"User\" – Any individual who reserves a parking space using the BPARK app.\r\n");
		strBuild.append("1.2 \"Reservation\" – Booking of a parking space for a specific date and time, via the app.\r\n");
		strBuild.append("1.3 \"Parking Lot\" – An automated facility without continuous human operation, where entry, parking, and exit are carried out using technological means.\r\n\r\n");

		strBuild.append("2. Usage and Reservations\r\n");
		strBuild.append("2.1 Use of the parking lot services is subject to a prior reservation via the BPARK app.\r\n");
		strBuild.append("2.2 The reservation is valid only for the selected date and time.\r\n");
		strBuild.append("2.3 A user who fails to arrive within 15 minutes of the scheduled reservation time will have their reservation automatically canceled.\r\n\r\n");

		strBuild.append("3. Liability Disclaimer\r\n");
		strBuild.append("3.1 BPARK is not liable for any damage to the vehicle, its contents, or the user, including theft, malfunction, or natural disaster.\r\n");
		strBuild.append("3.2 The user must ensure their vehicle is compatible with the automated system.\r\n\r\n");

		strBuild.append("4. Safety and Conduct Rules\r\n");
		strBuild.append("4.1 Follow instructions on the screen and in the app.\r\n");
		strBuild.append("4.2 Do not remain in the vehicle during automated parking.\r\n");
		strBuild.append("4.3 Do not bring hazardous or illegal materials into the parking lot.\r\n\r\n");

		strBuild.append("5. Privacy and Data Security\r\n");
		strBuild.append("5.1 Personal data is stored in accordance with BPARK’s privacy policy.\r\n");
		strBuild.append("5.2 BPARK cannot guarantee complete protection from data breaches or system failures.\r\n\r\n");

		strBuild.append("6. Governing Law and Jurisdiction\r\n");
		strBuild.append("6.1 These terms are governed by the laws of the State of Israel.\r\n");
		strBuild.append("6.2 Jurisdiction is granted to the competent court in the parking lot’s district.\r\n\r\n");

		strBuild.append("7. Miscellaneous\r\n");
		strBuild.append("7.1 BPARK may update these terms at any time.\r\n");
		strBuild.append("7.2 These terms are written in masculine form for convenience and apply to all users.\r\n");
		return strBuild.toString();
	}

	/**
	 * Loads the reservation summary screen and passes the created Order.
	 *
	 * @param order the completed reservation
	 */
	private void handleGoToOrderSummarry(Order order) {
		try {
			mainLayoutController=client.getMainLayoutController();
			mainLayoutController.loadScreen("/client/ReservationSummary.fxml", order);

		} catch (Exception e) {
			System.out.println("Error:"+ e.getMessage());
		}
	}

	/**
	 * Called by the ClientController when the order was successfully saved.
	 * Stores the order and navigates to the summary screen.
	 *
	 * @param order the confirmed Order
	 */
	public void setOrderAndGoToNextPage(Order order) {
		this.newOrder=order;
		handleGoToOrderSummarry(newOrder);
	}

	/**
	 * Shows a simple alert popup with the given message and type.
	 *
	 * @param message the text to show
	 * @param type    the type of alert (INFO, WARNING, etc.)
	 */
	private void showAlert(String message, Alert.AlertType type) {
		Alert alert = new Alert(type);
		alert.setTitle("BPARK - Message");
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.show();
	}
}
