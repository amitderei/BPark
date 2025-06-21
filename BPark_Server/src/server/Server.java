package server;

import ocsf.server.*;
import reportService.MonthlyReportScheduler;

import java.io.IOException;
import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import common.Order;
import common.ParkingEvent;
import common.ParkingReport;
import common.ServerResponse;
import common.Subscriber;
import common.User;
import db.DBController;
import mailService.MailService;
import mailService.TypeOfMail;

/**
 * Represents the BPARK server. Handles incoming client connections and
 * dispatches requests using the OCSF framework.
 * Also starts background threads such as email checking and monthly reporting.
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
     * Called automatically when the server starts.
     * Establishes DB connection and starts the monthly report scheduler.
     */
    @Override
    protected void serverStarted() {
        db.connectToDB();
        System.out.println("Server started on port " + getPort());
        MonthlyReportScheduler.start(); // schedule monthly reports
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
			if (msg instanceof Object[] data) {

				// Disconnect request
				if (data.length == 1 && "disconnect".equals(data[0])) {
					logClientDisconnect(client);
					return;
				}

				//get all reservations of subscriber
				else if (data.length==2 && "askForReservations".equals(data[0])){
					ArrayList<Order> orders=db.returnReservationOfSubscriber((Subscriber)data[1]);
					if (orders.isEmpty()) {
						client.sendToClient(new ServerResponse(true, null, "No orders."));
					} else {
						client.sendToClient(new ServerResponse(true, orders, "Orders of subscriber displayed successfully."));
					}
				}
				//expected format: {deleteOrder, orderNumber}
				else if (data.length==2 && "deleteOrder".equals(data[0])) {
					int orderNumberToDelete=(int) data[1];
					boolean succeed= db.deleteOrder(orderNumberToDelete);
					if(succeed) {
						client.sendToClient(new ServerResponse(true, null, "order deleted successfully."));
					}
					else {
						client.sendToClient(new ServerResponse(false, null, "order didn't delete"));
					}
				}
				//get parking history of subscriber: expected format {"updateParkingHistoryOfSubscriber", subscriber}
				else if(data.length==2 && "updateParkingHistoryOfSubscriber".equals(data[0])) {
					ArrayList<ParkingEvent> history=db.parkingHistoryOfSubscriber((Subscriber)data[1]);
					if(history==null) {
						client.sendToClient(new ServerResponse(false, null, "There was an error loading parking history."));
					}
					else if(history.isEmpty()) {
						client.sendToClient(new ServerResponse(false, null, "There is no data for this user."));
					}
					else {
						client.sendToClient(new ServerResponse(true, history, "Parking history data loaded successfully."));
					}
				}
				//return the parking report: expected format {"GetParkingReport", parkingReport}
				else if(data.length==2 && "GetParkingReport".equals(data[0])) {
					ParkingReport parkingReport=db.getParkingReport((Date)data[1]);

					if (parkingReport==null) {
						client.sendToClient(new ServerResponse(false, null, "There was an error loading parking report."));
					}
					else {
						System.out.println(Integer.toString(parkingReport.getTotalEntries()));
						client.sendToClient(new ServerResponse(true, parkingReport, "Parking report loaded."));
					}
					return;
				}

				else if (data.length==2 && "getDetailsOfActiveInfo".equals(data[0])) {
					ParkingEvent parkingEvent=db.getActiveParkingEvent((Subscriber)data[1]);
					if (parkingEvent==null) {
						client.sendToClient(new ServerResponse(false, null, "There is no active parking."));
					}
					else {
						client.sendToClient(new ServerResponse(true, parkingEvent, "Active parking info loaded successfully."));
					}
				}

				else if(data.length==2 && "forgotMyParkingCode".equals(data[0])) {
					try {
						String[] emailAndPhone=db.getEmailAndPhoneNumber((int) data[1]);
						String email=emailAndPhone[0];
						String phone=emailAndPhone[1];
						ParkingEvent parkingEvent=db.getActiveParkingEvent((new Subscriber((int)data[1])));
						sendEmail.sendEmail(email, parkingEvent.getParkingCode(), TypeOfMail.FORGOT_PASSWORD);
						client.sendToClient(new ServerResponse(true, null, "The code was sent to your email."));
					} catch(Exception e) {
						e.printStackTrace();
						client.sendToClient(new ServerResponse(false, null, "Failed to send email. Please try again later."));
					}
				}
				// Validate subscriber by tag: ["validateSubscriberByTag", tagId]
				else if (data.length == 2 && "validateSubscriberByTag".equals(data[0])) {
					String tagId = (String) data[1];
					int subscriberCode = db.getSubscriberCodeByTag(tagId);

					// Step 1: Check if tag is known
					if (subscriberCode > 0) {
						// Step 2: Ensure that the vehicle associated with this tag is inside the parking lot
						if (!db.checkSubscriberEntered(subscriberCode)) {
							client.sendToClient(new ServerResponse(false, null, "Your vehicle is not currently parked."));
							return;
						}

						// Step 3: Valid tag and active parking -> send subscriber code for client use
						client.sendToClient(new ServerResponse(true, subscriberCode,
								"Subscriber verified successfully by tag."));
					} else {
						client.sendToClient(new ServerResponse(false, null,
								"Tag ID not recognized. Please try again."));
					}
					return;
				}



				// Validate subscriber by numeric code: ["validateSubscriber", subscriberCode]
				else if (data.length == 2 && "validateSubscriber".equals(data[0])) {
					int subscriberCode = (int) data[1];

					// Step 1: Verify that subscriber exists in DB
					if (!db.subscriberExists(subscriberCode)) {
						client.sendToClient(new ServerResponse(false, null, "Subscriber code not found."));
						return;
					}

					// Step 2: Check that the subscriber's vehicle is currently parked (active session)
					if (!db.checkSubscriberEntered(subscriberCode)) {
						client.sendToClient(new ServerResponse(false, null, "Your vehicle is not currently parked."));
						return;
					}

					// Step 3: If both checks pass, approve validation
					client.sendToClient(new ServerResponse(true, null, "Subscriber verified"));
					return;
				}



				// Login request: ["login", username, password]
				else if (data.length == 3 && "login".equals(data[0])) {
					handleLogin(data, client);
					return;
				}

				// Collect car: ["collectCar", subscriberCode, parkingCode]
				else if (data.length == 3 && "collectCar".equals(data[0])) {
					int subCode = (int) data[1];
					int parkCode = (int) data[2];
					try {
						ServerResponse response = db.handleVehiclePickup(subCode, parkCode);
						client.sendToClient(response);
					} catch (Exception e) {
						client.sendToClient(new ServerResponse(false, null,
								"An error occurred while collecting the vehicle."));
						System.err.println("Error: collectCar - " + e.getMessage());
					}
					return;
				}
				//update details of subscriber. expected format={command, subscriber\null, user\null}
				else if (data.length==3 && "updateDetailsOfSubscriber".equals(data[0])) {
					boolean isSucceedSubscriber=true;
					boolean isSucceedUser=true;
					ArrayList<Object> newDetails= new ArrayList<>();
					if (data[1]!=null) {
						Subscriber subscriber= (Subscriber) data[1];
						isSucceedSubscriber=db.changeDetailsOfSubscriber(subscriber);
						newDetails.add(subscriber);
					}
					if(data[2]!=null) {
						User user=(User) data[2];
						isSucceedUser=db.changeDetailsOfUser(user);
						newDetails.add(user);
					}
					if (isSucceedSubscriber && isSucceedUser) {
						client.sendToClient(new ServerResponse(true, newDetails, "Details updated successfully.")); 
					}
					else {
						client.sendToClient(new ServerResponse(false, null, "The update did not occur due to a problem. Please try again later."));
					}
				}


				else if (data.length == 2 && "subscriberDetails".equals(data[0])) {
					Subscriber subscriber = db.getDetailsOfSubscriber((User) data[1]);
					if (subscriber != null) {
						client.sendToClient(
								new ServerResponse(true, subscriber, "Subscriber details saved successfully."));
					} else {
						client.sendToClient(new ServerResponse(false, null, "Subscriber details not found."));
					}
				}



				else if (data.length == 3 && "checkAvailability".equals(data[0])) {
					System.out.println("checkAvailability-server");
					boolean possible = db.parkingSpaceCheckingForNewOrder((Date) data[1], (Time) data[2]);
					if (possible) {
						client.sendToClient(new ServerResponse(true, null, "Can make resarvation"));
					} else {
						client.sendToClient(new ServerResponse(false, null, "Can't make resarvation"));
					}
				}
				//Expected format: {"addNewOrder", order}
				else if (data.length == 2 && "addNewOrder".equals(data[0])) {
					Order orderToAdd = (Order) data[1];
					boolean success = db.placingAnNewOrder(orderToAdd);

					if (success) {
						client.sendToClient(new ServerResponse(true, orderToAdd, "reservation succeed!"));
					} else {
						client.sendToClient(new ServerResponse(false, null, "reservation not succeed!"));
					}
				}

				// Expected format: {"subscriberExists", codeInt }
				else if (data.length == 2 && "subscriberExists".equals(data[0])) {
					int codeInt = (int) data[1];

					// Checking whether the code exists or not
					if (!db.subscriberExists(codeInt)) {
						// If the code doesn't exist we will let the user to know
						client.sendToClient(new ServerResponse(false, null, "Subscriber code does not exist."));
						return;
					}
					// If the code does exist we will let the user to know and to continue
					client.sendToClient(new ServerResponse(true, null, "Subscriber code is valid!"));

				}
				// Expected format: {"checkIfTheresReservation", codeInt}
				else if (data.length == 2 && "checkIfTheresReservation".equals(data[0])) {
					int codeInt = (int) data[1];

					// Checking whether the subscriber has a reservation, then checks if there is a
					// reservation in time of now
					if (!db.checkSubscriberHasReservationNow(codeInt)) {
						// If the subscriber doesn't have a reservation we will let the user to enter
						// only regularly
						client.sendToClient(new ServerResponse(true, null, "Subscriber doesn't have a reseravtion."));
						return;
					}
					// If the subscriber has a reservation we will let the user to enter with the
					// existing reservation
					client.sendToClient(new ServerResponse(true, null, "Subscriber has a reservation."));

				}

				// Expected format: {"DeliveryViaReservation", codeInt, confirmationCodeInt}
				else if (data.length == 3 && "DeliveryViaReservation".equals(data[0])) {
					int codeInt = (int) data[1];
					int confirmationCodeInt = (int) data[2];

					// Checking whether the subscriber has entered his confirmation code currectly
					if (!db.checkConfirmationCode(codeInt, confirmationCodeInt)) {
						// If the subscriber code hasn't entered currectly we will tell the user
						client.sendToClient(new ServerResponse(false, null, "The confirmation code isn't currect."));
						return;
					}
					// Letting the user know that he has entered the confirmation code successfully
					client.sendToClient(
							new ServerResponse(true, null, "The confirmation code has entered successfully."));
				}

				// Expected format: {"IsThereFreeParkingSpace", lotName}
				else if (data.length == 2 && "IsThereFreeParkingSpace".equals(data[0])) {
					String lotName = (String) data[1];
					int parkingSpaceInt = db.hasAvailableSpots(lotName);

					// If the method hasAvaliableSpots returns -1 it means that the parking lot is full
					if (parkingSpaceInt == -1) {
						client.sendToClient(new ServerResponse(false, null, "The Parking Lot is Full"));
						return;
					}

					// Else, we will sent the parking space to the client controller 
					client.sendToClient(new ServerResponse(true, parkingSpaceInt, "There is free parking space"));
				}

				// Expected format: {"getVehicleID", codeInt}
				else if (data.length == 2 && "getVehicleID".equals(data[0])) {
					int codeInt = (int) data[1];

					// Seeking for a matching vehicle to the asked subscriber
					String vehicleID = db.findVehicleID(codeInt);

					// The server sends the matched vehicleID
					client.sendToClient(new ServerResponse(true, vehicleID, "Found matched vehicle"));
					return;
				}
				// Expected format: {"DeliverVehicle", parkingEvent}
				else if (data.length == 2 && "DeliverVehicle".equals(data[0])) {
					ParkingEvent parkingEvent = (ParkingEvent) data[1];

					// Inserting the parking event into the DB
					db.addParkingEvent(parkingEvent);

					// The server sends the successful addition of parking event
					client.sendToClient(new ServerResponse(true, null, "Added parking event successfully"));
					return;
				}
				// Expected format: {"TagExists", tag}
				else if (data.length == 2 && "TagExists".equals(data[0])) {
					String tag = (String) data[1];

					// Check whether the tag exists in the DB or not
					if (!db.tagExists(tag)) {
						client.sendToClient(new ServerResponse(false, null, "Tag does not exists"));
						return;
					}

					// The server sends that the tag has been found
					client.sendToClient(new ServerResponse(true, null, "Tag exists"));
					return;
				}
				// Expected format: {"FindMatchedSubToTheTag", tag}
				else if (data.length == 2 && "findMatchedSubToTheTag".equals(data[0])) {
					String tag = (String) data[1];

					// Gathering the subscriber threw the relevant tag of his
					int subCode = db.seekForTheSubscriberWithTag(tag);

					// The server sends the successful addition of parking event
					client.sendToClient(new ServerResponse(true, subCode, "Subscriber with matching tag has been found"));
					return;
				}
				// Expected format: {"subscriberAlreadyEntered", codeInt}
				else if (data.length == 2 && "subscriberAlreadyEntered".equals(data[0])) {
					int codeInt = (int) data[1];

					if (!db.checkSubscriberEntered(codeInt)) {
						// The server will check whether the subscriber has already entered his vehicle into the parking lot or not
						client.sendToClient(new ServerResponse(true, null, "The subscriber didn't entered his vehicle yet"));
						return;
					}
					// If this method will return true, it means that he already entered his vehicle into the parking lot
					client.sendToClient(new ServerResponse(false, null, "The vehicle is already inside the parking lot"));
					return;
				}


				// Expected format: {"tagIdAlreadyEntered", tag}
				else if (data.length == 2 && "tagIdAlreadyEntered".equals(data[0])) {
					String tag = (String) data[1];

					if (!db.checkTagIDEntered(tag)) {
						// The server will check whether the vehicle that is matched with the tag is already inside the parking lot
						client.sendToClient(new ServerResponse(true, null, "The tag isn't inside"));
						return;
					}
					// If this method will return true, it means that the vehicle that is matched with the tag is already inside the parking lot
					client.sendToClient(new ServerResponse(false, null, "The vehicle is Already inside the parking lot"));
					return;
				}

				// return all subscribers and their late pickup counts
				else if (data.length == 1 && "get_all_subscribers".equals(data[0])) {
					System.out.println("[SERVER] Received get_all_subscribers request");
					ArrayList<Object[]> rows = new ArrayList<>(db.getAllSubscribersWithLateCount());
					client.sendToClient(new ServerResponse(true, rows, "all_subscribers"));
					return;
				}

				// "get_active_parkings" - returns List<ParkingEvent>
				else if (data.length == 1 && "get_active_parkings".equals(data[0])) {
					List<ParkingEvent> events = db.getActiveParkingEvents();
					client.sendToClient(new ServerResponse(true, events, "active_parkings"));
					return;
				}

				// Expected format: {"get_parking_availability"}
				else if (data.length == 1 && "get_parking_availability".equals(data[0])) {
					System.out.println("[SERVER] Received availability request");

					try {
						int total = db.getTotalSpots();
						int occupied = db.getOccupiedSpots();
						int available = total - occupied;

						Object[] stats = new Object[] { total, occupied, available };
						client.sendToClient(new ServerResponse(true, stats, "parking_availability"));
					} catch (Exception e) {
						e.printStackTrace();
						client.sendToClient(new ServerResponse(false, null, "Failed to retrieve parking availability."));
					}
					return;
				}



				// Expected format: {"extendParking", parkingCode, subscriberCode}
				else if (data.length == 3 && "extendParking".equals(data[0])) {
					int parkingCode = (int) data[1];
					String subscriberCode = (String) data[2];

					ServerResponse response = db.extendParkingSession(parkingCode, subscriberCode);
					client.sendToClient(response);
					return;
				}

				// Register new subscriber: ["registerSubscriber", Subscriber]
				else if (data.length == 2 && "registerSubscriber".equals(data[0])) {
					Subscriber receivedSub = (Subscriber) data[1];

					// Step 0: Check if username, email, phone or ID already exists
					if (db.usernameExists(receivedSub.getUsername())) {
						client.sendToClient(new ServerResponse(false, null, "Username already exists. Please choose another."));
						return;
					}
					if (db.emailExists(receivedSub.getEmail())) {
						client.sendToClient(new ServerResponse(false, null, "Email already registered. Use a different email."));
						return;
					}
					if (db.phoneExists(receivedSub.getPhoneNum())) {
						client.sendToClient(new ServerResponse(false, null, "Phone number already in use."));
						return;
					}
					if (db.idExists(receivedSub.getUserId())) {
						client.sendToClient(new ServerResponse(false, null, "ID already in use. Please verify the subscriber is not already registered."));
						return;
					}

					// Step 1: Generate subscriberCode and tagId
					int newCode = db.getNextSubscriberCode();
					String newTag = db.generateNextTagId();
					receivedSub.setSubscriberCode(newCode);
					receivedSub.setTagId(newTag);

					// Step 2: Generate random password
					String generatedPassword = generateRandomPassword();

					// Step 3: Insert user
					boolean userSuccess = db.insertUser(new User(
							receivedSub.getUsername(),
							generatedPassword,
							"Subscriber"
							));

					if (!userSuccess) {
						client.sendToClient(new ServerResponse(false, null, "Failed to insert user. Try again later."));
						return;
					}

					// Step 4: Insert subscriber
					boolean subSuccess = db.insertSubscriber(receivedSub);

					if (subSuccess) {
						// Step 5: Send password to email
						String content = String.format("""
								Hello %s,

								Your registration to the BPARK system was successful!

								Login credentials:
								- Username: %s
								- Temporary Password: %s

								You can now log in using these credentials.

								Thank you,
								BPARK Team
								""", receivedSub.getFirstName(), receivedSub.getUsername(), generatedPassword);

						sendEmail.sendEmail(receivedSub.getEmail(), content, TypeOfMail.GENERIC_MESSAGE);

						client.sendToClient(new ServerResponse(true, receivedSub, "Subscriber registered successfully. Login details sent via email."));
					} else {
						client.sendToClient(new ServerResponse(false, null, "Failed to insert subscriber. Try again."));
					}
					return;
				}



			}
		} catch (IOException e) {
			System.err.println("Client communication error: " + e.getMessage());
		}

	}

	/**
	 * Handles a login request.
	 *
	 * @param data   the message parts: ["login", username, password]
	 * @param client the requesting client
	 */
	private void handleLogin(Object[] data, ConnectionToClient client) {
		String username = (String) data[1];
		String password = (String) data[2];

		System.out.println("[SERVER] Login attempt from username: " + username);
		User user = db.authenticateUser(username, password);
		System.out.println("[DEBUG] DB returned user: " + user);

		try {
			if (user != null) {
				client.sendToClient(new ServerResponse(true, user, "Login successful"));
			} else {
				client.sendToClient(new ServerResponse(false, null, "Invalid username or password."));
			}
		} catch (IOException e) {
			System.err.println("Error sending login response: " + e.getMessage());
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
	 * Logs the disconnection of a client.
	 *
	 * @param client the client to log
	 */
	private void logClientDisconnect(ConnectionToClient client) {
		try {
			String ip = client.getInetAddress().getHostAddress();
			String host = client.getInetAddress().getHostName();
			System.out.println("Client disconnected from: " + host + " (" + ip + ")");
		} catch (Exception e) {
			System.out.println("Could not retrieve disconnected client info: " + e.getMessage());
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

}
