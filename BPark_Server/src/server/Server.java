package server;

import ocsf.server.*;
import reportService.*;
import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import common.*;
import db.DBController;
import mailService.*;

/**
 * Represents the BPARK server. Handles incoming client connections and
 * dispatches requests using the OCSF framework. Also starts background threads
 * such as email checking and monthly reporting.
 */
public class Server extends AbstractServer {

	/** Singleton DB controller used for all database operations */
	private DBController db;

	/** Mail service used for sending notifications */
	private MailService sendEmail = new MailService();

	/** Thread that monitors overdue parking sessions and sends alerts */
	private ParkingEventChecker parkingEventChecker = new ParkingEventChecker();

	/**
	 * Constructs a new server instance.
	 *
	 * @param port the TCP port the server will listen on
	 */
	public Server(int port) {
		super(port);
		db = DBController.getInstance();
		parkingEventChecker.setDaemon(true);
		parkingEventChecker.start();

	}

	/**
	 * Called automatically when the server starts. Establishes DB connection and
	 * starts the monthly report scheduler.
	 */
	@Override
	protected void serverStarted() {
		DBController.connectToDB();
		db.resetAllLoggedIn();
		MonthlyReportScheduler.start(); // schedule monthly reports
		new MonthlyReportGenerator().generatePastReports();

	}

	/**
	 * Processes incoming messages from connected clients. Each message is expected
	 * to be an Object[] with an action string as the first element. Supported
	 * actions include login, order management, vehicle pickup, and parking
	 * availability.
	 *
	 * @param msg    the message received from the client
	 * @param client the sending client
	 */
	@Override
	protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
		try {
			Object[] data = (Object[]) msg;

			switch ((Operation) data[0]) {

			// When the user clicks "Logout" - just logs them out and keeps the app open.
			// expected format: {LOGOUT}
			case LOGOUT:

				// If we know who the subscriber is, mark them as offline in the DB
				if (client.getInfo("username") instanceof String u1) {
					db.markUserLoggedOut(u1);
					System.out.println("User " + u1 + " logged out.");
				}
				break;

				// When the user clicks "Exit" - logs out and shuts down the whole app expected
				// format: {EXIT}
			case EXIT:
				// mark user as offline if they were logged in
				if (client.getInfo("username") instanceof String u2) {
					db.markUserLoggedOut(u2);
					System.out.println("User " + u2 + " logged out.");
				}
				logClientDisconnect(client); // close client system
				break;

				// get all reservations of specific subscriber. expected format:
				// {ASK_FOR_RESERVATIONS, subscriber}
			case ASK_FOR_RESERVATIONS:
				ArrayList<Order> orders = db.getFutureReservationsForSubscriber((Subscriber) data[1]);
				if (orders.isEmpty()) {
					client.sendToClient(new ServerResponse(true, null, ResponseType.NO_ORDERS, "No orders."));
				} else {
					client.sendToClient(new ServerResponse(true, orders, ResponseType.ORDERS_DISPLAY,
							"Orders of subscriber displayed successfully."));
				}
				break;

				// cancel order of subscriber. expected format: {DELETE_ORDER, orderNumber}
			case DELETE_ORDER:
				int orderNumberToDelete = (int) data[1];
				boolean succeed = db.deleteOrder(orderNumberToDelete);
				if (succeed) {
					System.out.println("Order " + orderNumberToDelete + " cancelled.");
					client.sendToClient(
							new ServerResponse(true, null, ResponseType.ORDER_DELETED, "order deleted successfully."));
				} else {
					client.sendToClient(new ServerResponse(false, null, null, "order didn't delete"));
				}
				break;

				// Check if subscriber has an order within 4 hours of requested time
			case CHECK_RESERVATION_CONFLICT:
				int subCodeToCheck = (int) data[1];
				Date dateToCheck = (Date) data[2];
				Time timeToCheck = (Time) data[3];

				boolean hasConflict = db.hasReservationConflict(subCodeToCheck, dateToCheck, timeToCheck);
				client.sendToClient(new ServerResponse(true, hasConflict, ResponseType.CONFLICT_CHECKED,
						"Conflict check completed."));
				break;

				// get parking history of subscriber. expected format:
				// {UPDATE_PARKING_HISTORY_OF_SUBSCRIBER, subscriber}
			case UPDATE_PARKING_HISTORY_OF_SUBSCRIBER:
				ArrayList<ParkingEvent> history = db.parkingHistoryOfSubscriber((Subscriber) data[1]);
				if (history == null) {
					client.sendToClient(
							new ServerResponse(false, null, null, "There was an error loading parking history."));
				} else if (history.isEmpty()) {
					client.sendToClient(new ServerResponse(false, null, null, "There is no data for this user."));
				} else {
					client.sendToClient(new ServerResponse(true, history, ResponseType.PARKING_HISTORY_LOADED,
							"Parking history data loaded successfully."));
				}
				break;

				// return the parking report. expected format {GET_PARKING_REPORT,
				// parkingReport}
			case GET_PARKING_REPORT:
				ParkingReport parkingReport = db.getParkingReport((Date) data[1]);

				if (parkingReport == null) {
					client.sendToClient(
							new ServerResponse(false, null, null, "There was an error loading parking report."));
				} else {
					client.sendToClient(new ServerResponse(true, parkingReport, ResponseType.PARKING_REPORT_LOADED,
							"Parking report loaded."));
				}
				break;

				// return the dates of the reports. expected format {GET_DATES_OF_REPORTS}
			case GET_DATES_OF_REPORTS:
				ArrayList<Date> dates = db.getAllReportsDates();
				if (dates == null) {
					client.sendToClient(
							new ServerResponse(false, null, null, "There was an error loading parking report dates."));
				} else {
					client.sendToClient(new ServerResponse(true, dates, ResponseType.REPOSTS_DATE_LOADED,
							"Parking report dates loaded."));
				}
				break;

				// return the data of active parking of subscriber. expected format
				// {GET_DETAILS_OF_ACTIVE_INFO, subscriber}
			case GET_DETAILS_OF_ACTIVE_INFO:
				ParkingEvent parkingEvent = db.getActiveParkingEvent((Subscriber) data[1]);
				if (parkingEvent == null) {
					client.sendToClient(new ServerResponse(false, null, ResponseType.PARKING_INFO_LOADED,
							"There is no active parking."));
				} else {
					client.sendToClient(new ServerResponse(true, parkingEvent, ResponseType.PARKING_INFO_LOADED,
							"Active parking info loaded successfully."));
				}
				break;

				// send mail to subscriber that forget the parking code. expected format
				// {FORGEOT_MY_PARKING_CODE, subscriberCode}
			case FORGEOT_MY_PARKING_CODE:
				String[] emailAndPhone = db.getEmailAndPhoneNumber((int) data[1]);
				String email = emailAndPhone[0];
				String phone = emailAndPhone[1];
				try {
					ParkingEvent parkingEventThatFoeget = db.getActiveParkingEvent((new Subscriber((int) data[1])));
					sendEmail.sendEmail(email, parkingEventThatFoeget.getParkingCode(), TypeOfMail.FORGOT_PASSWORD);
					client.sendToClient(new ServerResponse(true, null, null, "The code was sent to your email."));
				} catch (Exception e) {
					e.printStackTrace();
					client.sendToClient(
							new ServerResponse(false, null, null, "Failed to send email. Please try again later."));
					System.err.println("Failed to send email to" + email + ".");
				}
				break;

				// Validate subscriber by tag. expected format {VALIDATE_SUBSCRIBER_BY_TAG,
				// tagId}
			case VALIDATE_SUBSCRIBER_BY_TAG:
				String tagId = (String) data[1];
				int subscriberCode = db.getSubscriberCodeByTag(tagId);

				// Step 1: Check if tag is known
				if (subscriberCode > 0) {
					// Step 2: Ensure that the vehicle associated with this tag is inside the
					// parking lot
					if (!db.checkSubscriberEntered(subscriberCode)) {
						client.sendToClient(
								new ServerResponse(false, null, null, "Your vehicle is not currently parked."));
						return;
					}

					// Step 3: Valid tag and active parking -> send subscriber code for client use
					client.sendToClient(new ServerResponse(true, subscriberCode, ResponseType.SUBSCRIBER_VERIFIED,
							"Subscriber verified successfully by tag."));
				} else {
					client.sendToClient(
							new ServerResponse(false, null, null, "Tag ID not recognized. Please try again."));
				}
				break;

				// Validate subscriber by numeric code. expected format
				// {VALIDATE_SUBSCRIBER_BY_SUBSCRIBER_CODE, subscriberCode}
			case VALIDATE_SUBSCRIBER_BY_SUBSCRIBER_CODE:
				int validateSubscriberCode = (int) data[1];

				// Step 1: Verify that subscriber exists in DB
				if (!db.subscriberExists(validateSubscriberCode)) {
					client.sendToClient(new ServerResponse(false, null, null, "Subscriber code not found."));
				}

				// Step 2: Check that the subscriber's vehicle is currently parked (active
				// session)
				else if (!db.checkSubscriberEntered(validateSubscriberCode)) {
					client.sendToClient(new ServerResponse(false, null, null, "Your vehicle is not currently parked."));
				}

				// Step 3: If both checks pass, approve validation
				else {
					client.sendToClient(
							new ServerResponse(true, null, ResponseType.SUBSCRIBER_VERIFIED, "Subscriber verified"));
				}
				break;

				// Login request. expected format {LOGIN, username. password}
			case LOGIN:
				handleLogin(data, client);
				break;

				// Collect car. expected format: {COLLECT_CAR, subscriberCode, parkingCode}
			case COLLECT_CAR:
				int subCode = (int) data[1];
				int parkCode = (int) data[2];

				try {
					// Call the DB method to handle the vehicle pickup
					// It returns a String that represents the status
					String response = db.handleVehiclePickup(subCode, parkCode);

					// Fail response case
					if (response.equals("An error occurred while retrieving your parking session.")
							|| response.equals("An error occurred while completing the pickup process.")) {
						client.sendToClient(new ServerResponse(false, null, null, response));
						return;
					}

					// Fail - no active parking session
					if (response.equals("Parking code is incorrect.")) {
						client.sendToClient(new ServerResponse(false, null, ResponseType.PICKUP_VEHICLE, response));
						return;
					}

					// Else it would be a success response
					client.sendToClient(new ServerResponse(true, null, ResponseType.PICKUP_VEHICLE, response));
					return;

				} catch (Exception e) {
					// Unexpected error (e.g. DB crash or null pointer)
					client.sendToClient(
							new ServerResponse(false, null, null, "An error occurred while collecting the vehicle."));
					System.err.println("Error: collectCar - " + e.getMessage());
				}
				break;

				// update details of subscriber. expected format={UPDATE_DETAILS_OF_SUBSCRIBER,
				// subscriber\null, user\null}
			case UPDATE_DETAILS_OF_SUBSCRIBER:
				boolean isSucceedSubscriber = true;
				boolean isSucceedUser = true;
				ArrayList<Object> newDetails = new ArrayList<>();
				if (data[1] != null) {
					Subscriber subscriber = (Subscriber) data[1];
					if (db.duplicatePhone(subscriber.getPhoneNum(), subscriber.getSubscriberCode())) {
						client.sendToClient(new ServerResponse(false, null, null,
								"The phone number already exists in the system. try another phone number."));
						return;
					}
					if (db.duplicateEmail(subscriber.getEmail(), subscriber.getSubscriberCode())) {
						client.sendToClient(new ServerResponse(false, null, null,
								"The email already exists in the system. try another email."));
						return;
					}
					isSucceedSubscriber = db.changeDetailsOfSubscriber(subscriber);
					newDetails.add(subscriber);
				}
				if (data[2] != null) {
					User user = (User) data[2];
					isSucceedUser = db.changeDetailsOfUser(user);
					newDetails.add(user);
				}
				if (isSucceedSubscriber && isSucceedUser) {
					client.sendToClient(new ServerResponse(true, newDetails, ResponseType.DETAILS_UPDATED,
							"Details updated successfully."));
				} else {
					client.sendToClient(new ServerResponse(false, null, null,
							"The update did not occur due to a problem. Please try again later."));
				}
				break;

				// get the details of subscriber. expected format={SUBSCRIBER_DETAILS, user}
			case SUBSCRIBER_DETAILS:
				Subscriber subscriber = db.getDetailsOfSubscriber((User) data[1]);
				if (subscriber != null) {
					client.sendToClient(new ServerResponse(true, subscriber, ResponseType.SUBSCRIBER_DETAILS,
							"Subscriber details saved successfully."));
				} else {
					client.sendToClient(new ServerResponse(false, null, null, "Subscriber details not found."));
				}
				break;

				// check availability for order (at least 40%). expected
				// format={CHECK_AVAILABILITY_FOR_ORDER, date, time}
			case CHECK_AVAILABILITY_FOR_ORDER:
				boolean possible = db.parkingSpaceCheckingForNewOrder((Date) data[1], (Time) data[2]);
				if (possible) {
					client.sendToClient(
							new ServerResponse(true, null, ResponseType.RESERVATION_VALID, "Can make resarvation"));
				} else {
					client.sendToClient(new ServerResponse(false, null, ResponseType.RESERVATION_INVALID,
							"Can't make resarvation because no availability"));
				}
				break;

				// add a new reservation. expected format: {ADD_NEW_ORDER, order}
			case ADD_NEW_ORDER:
				Order orderToAdd = (Order) data[1];
				boolean success = db.placingAnNewOrder(orderToAdd);
				if (success) {
					System.out.println("The subscriber " + orderToAdd.getSubscriberId()
					+ " has successfully made a reservation " + orderToAdd.getOrderNumber() + ".");
					client.sendToClient(
							new ServerResponse(true, orderToAdd, ResponseType.ORDER_ADDED, "reservation succeed!"));
				} else {
					System.err.println("Reservation of" + orderToAdd.getSubscriberId() + "failed.");
					client.sendToClient(new ServerResponse(false, null, null, "reservation not succeed!"));
				}
				break;

				// check if there is a subscriber with this subscriber code. expected format:
				// {SUBSCRIBER_EXISTS, subscriberCode}
			case SUBSCRIBER_EXISTS:
				int codeInt = (int) data[1];

				// Checking whether the code exists or not
				if (!db.subscriberExists(codeInt)) {
					// If the code doesn't exist we will let the user to know
					client.sendToClient(new ServerResponse(false, null, ResponseType.SUBSCRIBER_CODE,
							"Subscriber code does not exist."));
				}
				// If the code does exist we will let the user to know and to continue
				else {
					client.sendToClient(
							new ServerResponse(true, null, ResponseType.SUBSCRIBER_CODE, "Subscriber code is valid!"));
				}
				break;

				// check if the subscriber has a reservation right now. expected format:
				// {CHECK_IF_THERE_IS_RERSERVATION, subscriberCode}
			case CHECK_IF_THERE_IS_RERSERVATION:
				int codeIntForCheckingReservation = (int) data[1];

				// Checking whether the subscriber has a reservation, then checks if there is a
				// reservation in time of now
				if (!db.checkSubscriberHasReservationNow(codeIntForCheckingReservation)) {
					// If the subscriber doesn't have a reservation we will let the user to enter
					// only regularly
					client.sendToClient(new ServerResponse(true, null, ResponseType.RESERVATION_NOT_EXISTS,
							"Subscriber doesn't have a reseravtion."));
				}
				// If the subscriber has a reservation we will let the user to enter with the
				// existing reservation
				else {
					client.sendToClient(new ServerResponse(true, null, ResponseType.RESERVATION_EXISTS,
							"Subscriber has a reservation."));
				}
				break;

				// make the delivery via reservation of subscriber. expected format:
				// {DELIVERY_VIA_RESERVATION, subscriberCode, confirmationCode}
			case DELIVERY_VIA_RESERVATION:
				int codeIntForDeliveryViaReservation = (int) data[1];
				int confirmationCodeInt = (int) data[2];

				// Checking whether the subscriber has entered his confirmation code correctly
				if (!db.checkConfirmationCode(codeIntForDeliveryViaReservation, confirmationCodeInt)) {
					// If the subscriber code hasn't entered correctly we will tell the user
					client.sendToClient(new ServerResponse(false, null, ResponseType.CONFIRMATION_CODE_VALIDATION,
							"The confirmation code isn't currect."));
				} else {
					// Letting the user know that he has entered the confirmation code successfully
					client.sendToClient(new ServerResponse(true, null, ResponseType.CONFIRMATION_CODE_VALIDATION,
							"The confirmation code has entered successfully."));
				}
				break;

				// check if there is an empty parking spot in the parking lot. expected format:
				// {IS_THERE_FREE_PARKING_SPACE, lotName, subscriberCode, activeReservation}
			case IS_THERE_FREE_PARKING_SPACE:
				String lotName = (String) data[1];
				subscriberCode = (Integer) data[2];
				boolean activeReservation = (Boolean) data[3];
				int parkingSpaceInt = db.hasAvailableSpots(lotName, subscriberCode, activeReservation);

				// If the method hasAvaliableSpots returns -1 it means that the parking lot is full
				if (parkingSpaceInt == -1) {
					client.sendToClient(new ServerResponse(false, null, ResponseType.PARKING_SPACE_AVAILABILITY,
							"The Parking Lot is Full"));
					
				} else if (parkingSpaceInt == -2) {
					// The server will check whether the subscriber has already entered his vehicle
					// into the parking lot or not
					client.sendToClient(new ServerResponse(false, null, ResponseType.SUBSCRIBER_VEHICLE_ISNT_INSIDE,
							"The vehicle is Already inside the parking lot"));
				} else {
					// Else, we will sent the parking space to the client controller
					client.sendToClient(new ServerResponse(true, parkingSpaceInt,
							ResponseType.PARKING_SPACE_AVAILABILITY, "There is free parking space"));
				}
				break;

				// get vehicle id. expected format: {GET_VEHICLE_ID, subscriberCode}
			case GET_VEHICLE_ID:
				int codeIntForGetVehicleId = (int) data[1];

				// Seeking for a matching vehicle to the asked subscriber
				String vehicleID = db.findVehicleID(codeIntForGetVehicleId);

				// The server sends the matched vehicleID
				client.sendToClient(
						new ServerResponse(true, vehicleID, ResponseType.VEHICLE_ID, "Found matched vehicle"));
				break;

				// get in the new car to the lot. expected format: {DELIVER_VEHICLE,
				// parkingEvent}
			case DELIVER_VEHICLE:
				ParkingEvent newParkingEvent = (ParkingEvent) data[1];

				// change status of inactive orders
				db.inactiveReservations();
				// Inserting the parking event into the DB
				db.addParkingEvent(newParkingEvent);

				System.out.println("Vehicle of " + newParkingEvent.getSubscriberCode() + " entered successfully.");

				// The server sends the successful addition of parking event
				client.sendToClient(new ServerResponse(true, null, ResponseType.DELIVER_VEHICLE,
						"Added parking event successfully"));
				break;

				// check if tag exists. expected format: {TAG_EXISTS, tagId}
			case TAG_EXISTS:
				String tag = (String) data[1];

				// Check whether the tag exists in the DB or not
				if (!db.tagExists(tag)) {
					client.sendToClient(
							new ServerResponse(false, null, ResponseType.TAG_EXISTS, "Tag does not exists"));

				} else {
					// The server sends that the tag has been found
					client.sendToClient(new ServerResponse(true, null, ResponseType.TAG_EXISTS, "Tag exists"));
				}
				break;

				// find a matched subscriber code to the tag ID. expected format:
				// {FIND_MATCHED_SUBSCRIBER_TO_THE_TAG, tagId}
			case FIND_MATCHED_SUBSCRIBER_TO_THE_TAG:
				String tagIdOfSubscriber = (String) data[1];

				// Gathering the subscriber threw the relevant tag of his
				int subsCode = db.seekForTheSubscriberWithTag(tagIdOfSubscriber);

				// The server sends the successful addition of parking event
				client.sendToClient(new ServerResponse(true, subsCode, ResponseType.MATCHED_SUBSCRIBER_TO_TAG,
						"Subscriber with matching tag has been found"));
				break;

				// check if subscriber already entered to the lot. expected format:
				// {SUBSCRIBER_ALREADY_ENTERED, subscriberCode}
			case SUBSCRIBER_ALREADY_ENTERED:
				int codeIntToCheckIfSubscriberEntered = (int) data[1];

				if (!db.checkSubscriberEntered(codeIntToCheckIfSubscriberEntered)) {
					// The server will check whether the subscriber has already entered his vehicle
					// into the parking lot or not
					client.sendToClient(new ServerResponse(true, null, ResponseType.SUBSCRIBER_VEHICLE_ISNT_INSIDE,
							"The subscriber didn't entered his vehicle yet"));
				} else {
					// If this method will return true, it means that he already entered his vehicle
					// into the parking lot
					client.sendToClient(new ServerResponse(false, null, ResponseType.SUBSCRIBER_VEHICLE_ISNT_INSIDE,
							"The vehicle is already inside the parking lot"));
				}
				break;

				// check if tag id already entered to lot. expected format:
				// {TAG_ID_ALREADY_ENTERED, tadId}
			case TAG_ID_ALREADY_ENTERED:
				String tagIdToCheckIfEntered = (String) data[1];

				if (!db.checkTagIDEntered(tagIdToCheckIfEntered)) {
					// The server will check whether the vehicle that is matched with the tag is
					// already inside the parking lot
					client.sendToClient(new ServerResponse(true, null,
							ResponseType.SUBSCRIBER_VEHICLE_ISNT_INSIDE_BY_TAG, "The tag isn't inside"));
				} else {
					// If this method will return true, it means that the vehicle that is matched
					// with the tag is already inside the parking lot
					client.sendToClient(new ServerResponse(false, null, ResponseType.SUBSCRIBER_VEHICLE_ISNT_INSIDE,
							"The vehicle is Already inside the parking lot"));
				}
				break;

				// return all subscribers and their late pickup counts. expected format:
				// {GET_ALL_SUBSCRIBERS}
			case GET_ALL_SUBSCRIBERS:
				ArrayList<Object[]> rows = new ArrayList<>(db.getAllSubscribersWithLateCount());
				client.sendToClient(new ServerResponse(true, rows, ResponseType.LATE_PICKUP_COUNTS, "all_subscribers"));
				break;
				// get all parking event that active right now. expected format:
				// {GET_ACTIVE_PARKINGS}
			case GET_ACTIVE_PARKINGS:
				List<ParkingEvent> events = db.getActiveParkingEvents();
				client.sendToClient(new ServerResponse(true, events, ResponseType.ACTIVE_PARKINGS, "active_parkings"));
				break;

				// get the data of parking availability
			case GET_PARKING_AVAILABILITY:

				try {
					int total = db.getTotalSpots();
					int occupied = db.getOccupiedSpots();
					int upcoming = db.getUpcomingReservations(); 
					int available = Math.max(0, total - occupied - upcoming);

					Object[] stats = new Object[] { total, occupied, upcoming, available };
					System.out.println("Received availability request");
					client.sendToClient(
							new ServerResponse(true, stats, ResponseType.PARKING_AVALIABILITY, "parking_availability"));
				} catch (Exception e) {
					e.printStackTrace();
					client.sendToClient(
							new ServerResponse(false, null, null, "Failed to retrieve parking availability."));
				}
				break;


				// extend the parking event of subscriber. expected format: {"extendParking",
				// parkingCode, subscriberCode(String)}
			case EXTEND_PARKING:
				int parkingCode = (int) data[1];
				String subscriberCodeForExtend = (String) data[2];

				String response = db.extendParkingSession(parkingCode, subscriberCodeForExtend);
				boolean completed = false;

				if (response.equals("Parking session extended successfully.")) {
					completed = true;
					System.out.println("Parking time extension of " + parkingCode + " completed successfully.");
				}

				client.sendToClient(
						new ServerResponse(completed, null, ResponseType.PARKING_SESSION_EXTENDED, response));
				break;

				// check if subscriber has a order in the same date and time. expected format:
				// {IS_THERE_AN_EXISTED_ORDER, subscriberCode, selectedDate, timeOfArrival}
			case IS_THERE_AN_EXISTED_ORDER:
				int subscriberCodeToCheckOrder = (int) data[1];
				Date selectedDate = (Date) data[2];
				Time timeOfArrival = (Time) data[3];

				if (!db.checkIfOrderAlreadyExists(subscriberCodeToCheckOrder, selectedDate, timeOfArrival)) {
					client.sendToClient(
							new ServerResponse(true, null, ResponseType.ORDER_NOT_EXISTS, "Order doesn't exists."));
				} else {
					client.sendToClient(new ServerResponse(false, null, ResponseType.ORDER_ALREADY_EXISTS,
							"This order already exists for this subscriber."));
				}
				break;

				// register a new subscriber to the system. expected format:
				// {REGISTER_SUBSCRIBER, subscriber, vehicleID}
			case REGISTER_SUBSCRIBER:
				Subscriber receivedSub = (Subscriber) data[1];
				String vehicleId = (data.length > 2 && data[2] instanceof String) ? (String) data[2] : null;

				// 1. check duplicates before we touch the DB
				List<String> invalid = new ArrayList<>();
				if (db.usernameExists(receivedSub.getUsername()))
					invalid.add("username");
				if (db.emailExists(receivedSub.getEmail()))
					invalid.add("email");
				if (db.phoneExists(receivedSub.getPhoneNum()))
					invalid.add("phone");
				if (db.idExists(receivedSub.getUserId()))
					invalid.add("id");
				if (vehicleId != null && db.vehicleExists(vehicleId))
					invalid.add("vehicle");

				if (!invalid.isEmpty()) {
					client.sendToClient(
							new ServerResponse(false, invalid, ResponseType.SUBSCRIBER_INSERTED, "Duplicate fields"));
					break; // stop right here
				}

				// 2. generate code / tag / temp-password
				int newCode = db.getNextSubscriberCode();
				String newTag = db.generateNextTagId();
				String tmpPassword = generateRandomPassword();

				receivedSub.setSubscriberCode(newCode);
				receivedSub.setTagId(newTag);

				// 3. user -> DB
				if (!db.insertUser(new User(receivedSub.getUsername(), tmpPassword, "Subscriber"))) {
					client.sendToClient(new ServerResponse(false, null, ResponseType.SUBSCRIBER_INSERTED,
							"Failed to insert user. Try again later."));
					break;
				}

				// 4. subscriber -> DB */
				if (!db.insertSubscriber(receivedSub)) {
					client.sendToClient(new ServerResponse(false, null, ResponseType.SUBSCRIBER_INSERTED,
							"Failed to insert subscriber. Try again."));
					break;
				}

				// 5. vehicle
				if (vehicleId != null && !db.insertVehicle(vehicleId, newCode)) {
					client.sendToClient(new ServerResponse(false, null, ResponseType.SUBSCRIBER_INSERTED,
							"Failed to register vehicle."));
					break;
				}

				// 6. build the welcome mail – now includes Subscriber Code
				String mailBody = String.format("""
						Hello %s,

						Your registration to the BPARK system was successful!

						Subscriber Code: %d

						Login credentials:
						- Username: %s
						- Temporary Password: %s

						%s

						Thank you,
						BPARK Team
						""", receivedSub.getFirstName(), newCode, receivedSub.getUsername(), tmpPassword,
						(vehicleId != null ? "Vehicle ID: " + vehicleId : ""));

				sendEmail.sendEmail(receivedSub.getEmail(), mailBody, TypeOfMail.GENERIC_MESSAGE);

				// 7. done – tell the client
				client.sendToClient(new ServerResponse(true, receivedSub, ResponseType.SUBSCRIBER_INSERTED,
						"Subscriber registered successfully." + "Login details sent via email."));
				break;

				// get subscriber status report of selected date. expected format:
				// {GET_SUBSCRIBER_STATUS_REPORT, month, year}
			case GET_SUBSCRIBER_STATUS_REPORT:
				int month = (int) data[1];
				int year = (int) data[2];

				try {
					List<SubscriberStatusReport> rowsOfSubscriberStatus = db.getSubscriberStatusFromTable(month, year);

					/* current open month? -> build live if snapshot missing */
					LocalDate today = LocalDate.now();
					boolean isCurrent = (year == today.getYear() && month == today.getMonthValue());

					if (rowsOfSubscriberStatus.isEmpty() && isCurrent) {
						rowsOfSubscriberStatus = db.getSubscriberStatusLive(month, year);
					}

					if (rowsOfSubscriberStatus.isEmpty()) { // past month with no snapshot
						client.sendToClient(new ServerResponse(false, null, null,
								"No snapshot available for " + month + "/" + year));
					} else {
						client.sendToClient(new ServerResponse(true, rowsOfSubscriberStatus,
								ResponseType.SUBSCRIBER_STATUS, "subscriber_status"));
					}
				} catch (SQLException ex) {
					ex.printStackTrace();
					client.sendToClient(
							new ServerResponse(false, null, ResponseType.SUBSCRIBER_STATUS, "subscriber_status"));
				}
				break;

			}
		} catch (IOException | SQLException e) {
			System.err.println("Client communication error: " + e.getMessage());
		}

	}

	/**
	 * Handles a login request sent from the client side.
	 * Expects the data array to contain: {LOGIN, username, password}.
	 * If the credentials are valid and the user is not already logged in,
	 * the server responds with a successful login. Otherwise, it sends an error message.
	 *
	 * @param data   the message array sent from the client (operation + login details)
	 * @param client the connection to the client that sent the login request
	 */
	private void handleLogin(Object[] data, ConnectionToClient client) {
		String username = (String) data[1];
		String password = (String) data[2];

		System.out.println("Login attempt for user: " + username);

		User user = db.authenticateUser(username, password); // null => wrong creds OR already online

		try {
			if (user != null) {
				// remember username on this socket – cleared on disconnect
				client.setInfo("username", username);
				System.out.println("User " + username + " successfully logged in");
				client.sendToClient(new ServerResponse(true, user, ResponseType.LOGIN_SUCCESSFULL, "Login successful"));
			} else {
				boolean alreadyOnline = db.usernameExists(username) && db.isUserOnline(username);

				String msg = alreadyOnline ? "This account is already logged in from another device."
						: "Invalid username or password.";

				client.sendToClient(new ServerResponse(false, null, ResponseType.LOGIN_FAILED, msg));
			}
		} catch (IOException e) {
			System.err.println("[SERVER] Failed to send login response: " + e.getMessage());
		}
	}

	/**
	 * Logs new client connections.
	 *
	 * @param client the connecting client
	 */
	@Override
	protected void clientConnected(ConnectionToClient client) {
		try {
			String ip = client.getInetAddress().getHostAddress();
			String host = client.getInetAddress().getHostName();
			System.out.println("Client connected from: " + host + " (" + ip + ")");
		} catch (Exception e) {
			System.out.println("Could not retrieve client info: " + e.getMessage());
		}
	}

	/**
	 * Logs when a client disconnects.
	 *
	 * @param client the disconnecting client
	 */
	@Override
	protected void clientDisconnected(ConnectionToClient client) {
		logClientDisconnect(client);
	}

	/**
	 * Handles cleanup when a client disconnects from the server.
	 * Prints the client's IP and hostname, and if the user was logged in,
	 * it updates the database to mark them as logged out.
	 *
	 * @param client the connection of the client that disconnected
	 */
	private void logClientDisconnect(ConnectionToClient client) {
		try {
			String ip = client.getInetAddress().getHostAddress();
			String host = client.getInetAddress().getHostName();
			System.out.println("Client disconnected from: " + host + " (" + ip + ")");

			/* clear online flag (if this socket belonged to a logged-in user) */
			Object uObj = client.getInfo("username");
			if (uObj instanceof String u) {
				db.markUserLoggedOut(u);
			}
		} catch (Exception e) {
			System.out.println("Could not process disconnect: " + e.getMessage());
		}
	}

	/**
	 * Returns a list of all connected client hostnames and IPs.
	 *
	 * @return list of client connection strings
	 */
	public ArrayList<String> getConnectedClientInfoList() {
		ArrayList<String> list = new ArrayList<>();

		for (Thread t : this.getClientConnections()) {
			if (t instanceof ConnectionToClient client) {
				try {
					String ip = client.getInetAddress().getHostAddress();
					String host = client.getInetAddress().getHostName();
					list.add("Host: " + host + " (" + ip + ")");
				} catch (Exception e) {
					list.add("Unknown client");
				}
			} else {
				list.add("Unknown connection type");
			}
		}

		return list;
	}

	/**
	 * Generates a random 8-character alphanumeric password.
	 *
	 * @return random password
	 */
	private String generateRandomPassword() {
		String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 8; i++) {
			int idx = (int) (Math.random() * chars.length());
			sb.append(chars.charAt(idx));
		}
		return sb.toString();
	}

	/**
	 * Stops the BPARK server in a clean way.
	 *
	 * What happens here: - Send "server_shutdown" to every connected client so they
	 * can quit nicely. - Close the listening socket (this drops all client sockets
	 * too). - Clear is_logged_in for every user in the DB, just to be extra safe.
	 *
	 * Called from the GUI when the user presses Exit.
	 */
	public void shutdownGracefully() {
		// warn all clients
		sendToAllClients("server_shutdown");

		// close the server socket itself
		try {
			close(); // method from AbstractServer
		} catch (IOException ex) {
			System.err.println("Shut down failed: " + ex.getMessage());
		}

		// make sure nobody is still marked online in the DB
		db.resetAllLoggedIn();

		System.out.println("Shutdown complete – all clients logged out.");
	}

}