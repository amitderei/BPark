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
import java.util.Map;
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
		db.connectToDB();
		System.out.println("Server started on port " + getPort());
		MonthlyReportScheduler.start(); // schedule monthly reports
		new MonthlyReportGenarator().generatePastReports();

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
			Object [] data=(Object[]) msg;
			
			switch ((Operation) data[0]) {
				// Disconnect request from client. expected format: {DISCONNECT}
				case DISCONNECT:
					logClientDisconnect(client);
					break;
				// get all reservations of specific subscriber.  expected format: {ASK_FOR_RESERVATIONS, subscriber}
				case ASK_FOR_RESERVATIONS:
					ArrayList<Order> orders = db.returnReservationOfSubscriber((Subscriber) data[1]);
					if (orders.isEmpty()) {
						client.sendToClient(new ServerResponse(true, null, "No orders."));
					} else {
						client.sendToClient(
								new ServerResponse(true, orders, "Orders of subscriber displayed successfully."));
					}
					break;
				//cancel order of subscriber. expected format: {DELETE_ORDER, orderNumber}
				case DELETE_ORDER:
					int orderNumberToDelete = (int) data[1];
					boolean succeed = db.deleteOrder(orderNumberToDelete);
					if (succeed) {
						client.sendToClient(new ServerResponse(true, null, "order deleted successfully."));
					} else {
						client.sendToClient(new ServerResponse(false, null, "order didn't delete"));
					}
					break;
				//get parking history of subscriber. expected format: {UPDATE_PARKING_HISTORY_OF_SUBSCRIBER, subscriber}
				case UPDATE_PARKING_HISTORY_OF_SUBSCRIBER:
					ArrayList<ParkingEvent> history = db.parkingHistoryOfSubscriber((Subscriber) data[1]);
					if (history == null) {
						client.sendToClient(
								new ServerResponse(false, null, "There was an error loading parking history."));
					} else if (history.isEmpty()) {
						client.sendToClient(new ServerResponse(false, null, "There is no data for this user."));
					} else {
						client.sendToClient(
								new ServerResponse(true, history, "Parking history data loaded successfully."));
					}
					break;
				//return the parking report. expected format {GET_PARKING_REPORT, parkingReport}
				case GET_PARKING_REPORT:
					ParkingReport parkingReport = db.getParkingReport((Date) data[1]);

					if (parkingReport == null) {
						client.sendToClient(
								new ServerResponse(false, null, "There was an error loading parking report."));
					} else {
						client.sendToClient(new ServerResponse(true, parkingReport, "Parking report loaded."));
					}
					break;
				//return the dates of the reports. expected format {GET_DATES_OF_REPORTS}
				case GET_DATES_OF_REPORTS:
					ArrayList<Date> dates = db.getAllReportsDates();
					if (dates == null) {
						client.sendToClient(
								new ServerResponse(false, null, "There was an error loading parking report dates."));
					} else {
						client.sendToClient(new ServerResponse(true, dates, "Parking report dates loaded."));
					}
					break;
				//return the data of active parking of subscriber. expected format {GET_DETAILS_OF_ACTIVE_INFO, subscriber}
				case GET_DETAILS_OF_ACTIVE_INFO:
					ParkingEvent parkingEvent = db.getActiveParkingEvent((Subscriber) data[1]);
					if (parkingEvent == null) {
						client.sendToClient(new ServerResponse(false, null, "There is no active parking."));
					} else {
						client.sendToClient(
								new ServerResponse(true, parkingEvent, "Active parking info loaded successfully."));
					}
					break;
				//send mail to subscriber that forget the parking code. expected format {FORGEOT_MY_PARKING_CODE, subscriberCode}
				case FORGEOT_MY_PARKING_CODE:
					try {
						String[] emailAndPhone = db.getEmailAndPhoneNumber((int) data[1]);
						String email = emailAndPhone[0];
						String phone = emailAndPhone[1];
						ParkingEvent parkingEventThatFoeget = db.getActiveParkingEvent((new Subscriber((int) data[1])));
						sendEmail.sendEmail(email, parkingEventThatFoeget.getParkingCode(), TypeOfMail.FORGOT_PASSWORD);
						client.sendToClient(new ServerResponse(true, null, "The code was sent to your email."));
					} catch (Exception e) {
						e.printStackTrace();
						client.sendToClient(
								new ServerResponse(false, null, "Failed to send email. Please try again later."));
					}
			}
		}catch(IOException e){
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
