package controllers;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Random;

import client.ClientController;
import common.Order;
import common.Subscriber;
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

public class CreateNewOrderViewController implements ClientAware{

	@FXML
	private Label headlineParkingReservation; // headline

	@FXML
	private Label chooseDateAndTime; // display near date picker

	@FXML
	private Label betweenHourAndMinute; // display near hour and minute combobox

	@FXML
	private Label subscriberCode; // temporary

	@FXML
	private TextField insertSubscriberCode; // temporary

	@FXML
	private DatePicker chooseDate; // DatePicker for selecting a new date

	@FXML
	private ComboBox<String> hourCombo; // ComboBox for selecting hour

	@FXML
	private ComboBox<String> minuteCombo; // ComboBox for selecting minutes

	@FXML
	private CheckBox checkBox;
	
	@FXML
	private Hyperlink termsOfUseHyper;

	@FXML
	private Button reserveNowButton; // update button
	
	private MainLayoutController mainLayoutController; 

	private ClientController client;
	
	public Order newOrder;

	/**
	 * start the initialize of the comboboxes and date pickers
	 */
	public void initializeCombo() {
		insertSubscriberCode.setText(((Integer)(client.getSubscriber()).getSubscriberCode()).toString());
		
		hourCombo.getItems().clear();
		minuteCombo.getItems().clear();
		

		// get the day of today
		LocalDate today = LocalDate.now();
		// get the limits of days
		LocalDate tommorow = today.plusDays(1);
		LocalDate nextWeek = today.plusDays(8);
		// get other days disable
		chooseDate.setDayCellFactory((Callback<DatePicker, DateCell>) picker -> new DateCell() {
			@Override
			public void updateItem(LocalDate date, boolean empty) {
				super.updateItem(date, empty);
				if (empty || date.isBefore(tommorow) || date.isAfter(nextWeek)) { // each day that isn't between 1 to 8
																					// days turn to disable
					setDisable(true);
				}
			}
		});

	}

	/**
	 * set client
	 * 
	 * @param client
	 */
	public void setClient(ClientController client) {
		this.client = client;
	}

	/**
	 * add the hours that client choose according to the date he choose
	 */
	public void dateChosen() {
		LocalDate date = chooseDate.getValue();

		LocalDate today = LocalDate.now();
		LocalDate tommorow = today.plusDays(1);
		LocalDate nextWeek = today.plusDays(8);

		LocalTime now = LocalTime.now();

		hourCombo.getItems().clear();
		minuteCombo.getItems().clear();

		if (date.equals(tommorow)) {
			for (int i = now.getHour(); i < 24; i++) { // add the hours of day
				String hour = String.format("%02d", i);
				hourCombo.getItems().add(hour);
			}

		} else if (date.equals(nextWeek)) {
			for (int i = 0; i <= now.getHour(); i++) { // add the hours of day
				String hour = String.format("%02d", i);
				hourCombo.getItems().add(hour);
			}

		}

		else {
			for (int i = 0; i < 24; i++) { // add the hours of day
				String hour = String.format("%02d", i);
				hourCombo.getItems().add(hour);
			}
		}
	}

	/**
	 * add the minutes that client can choose according to the hour he choose
	 */
	public void hourChoosen() {
		LocalDate date = chooseDate.getValue();

		LocalDate today = LocalDate.now();
		LocalDate tommorow = today.plusDays(1);
		LocalDate nextWeek = today.plusDays(8);

		LocalTime now = LocalTime.now();
		int hourNow = now.getHour();
		String hourNowStr = String.format("%02d", hourNow);
		String hour = hourCombo.getValue();

		if (date.equals(tommorow) && hourNowStr.equals(hour)) {

			// add every 15 minutes
			minuteCombo.getItems().clear();
			for (int i = 0; i < 4; i++) {
				if (15 * i >= now.getMinute())
					minuteCombo.getItems().add(String.format("%02d", i * 15));
			}
		} else if (date.equals(nextWeek) && hourNowStr.equals(hour)) {
			// add every 15 minutes
			minuteCombo.getItems().clear();
			for (int i = 0; i < 4; i++) {
				if (15 * i <= now.getMinute())
					minuteCombo.getItems().add(String.format("%02d", i * 15));
			}
		} else {
			// add every 15 minutes
			minuteCombo.getItems().clear();
			for (int i = 0; i < 4; i++) {
				minuteCombo.getItems().add(String.format("%02d", i * 15));

			}
		}
	}

	
	/**
	 * get the details of reservation and try to order it
	 */
	public void addNewOrder() {
		if (!checkBox.isSelected()) {
			showAlert("Please agree to the terms of use", Alert.AlertType.WARNING);
			return;
		}
		// Parse subscriber code from the text field (must be a number)
		int subscriberNum = Integer.parseInt(insertSubscriberCode.getText().trim());

		if (chooseDate.getValue() == null) {
			showAlert("Please select a date and hour.", Alert.AlertType.WARNING);
			return;
		}

		LocalDate date = chooseDate.getValue();
		Date selectedDate = Date.valueOf(date);

		if (hourCombo.getValue() == null || minuteCombo.getValue() == null) {
			showAlert("Please select a hour.", Alert.AlertType.WARNING);
			return;
		}

		String selectHourByUser = hourCombo.getValue();
		String selectMinuByUser = minuteCombo.getValue();
		LocalTime time = LocalTime.of(Integer.parseInt(selectHourByUser), Integer.parseInt(selectMinuByUser));
		Time timeOfArrival = Time.valueOf(time);
		LocalDate today = LocalDate.now();
		Date now = Date.valueOf(today);
		
		client.checkAvailability(selectedDate, timeOfArrival);

		Random rand = new Random();
		int code = rand.nextInt(1000000);
		String strCode = String.format("%06d", code);
		newOrder = new Order(1, 55, selectedDate, timeOfArrival, strCode, subscriberNum, now);

		client.addNewOrder(newOrder);
	}
	
	/**
	 * create pop up dialog that show the Terms of use
	 */
	public void showTermsOfUse() {
		Dialog<Void> dialog= new Dialog<>();
		dialog.setTitle("Terms Of Use");
		dialog.setHeaderText("Please read the Terms of Use.");
		TextArea textArea=new TextArea(returnTermsOfUse());
		textArea.setWrapText(true);
		textArea.setEditable(false);
	    textArea.setPrefWidth(600);
	    textArea.setPrefHeight(400);
	    
	    dialog.getDialogPane().setContent(textArea);
	    dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
	    dialog.showAndWait();
	}
	
	/*
	 * return string of terms of use
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
	
	private void handleGoToOrderSummarry(Order order) {
	    try {
	    	mainLayoutController=client.getMainLayoutController();
	    	mainLayoutController.loadScreen("/client/ReservationSummary.fxml", order);
	    	
	    } catch (Exception e) {
	        System.out.println("Error:"+ e.getMessage());
	    }
	}
	
	
	public void setOrderAndGoToNextPage(Order order) {
		this.newOrder=order;
		handleGoToOrderSummarry(newOrder);
	}

	/**
	 * !!!add fathers of controllers!) Utility method for displaying pop-up alerts
	 * (message boxes) in the GUI.
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
