package db;

import common.User;
import java.sql.Connection;

import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import common.*;

/**
 * DBController handles all DB operations related to the BPARK database.
 * This class implements the Singleton pattern to ensure a single shared
 * connection to the database throughout the runtime of the application.
 */
public class DBController {

    /** Singleton instance shared across all classes */
    private static DBController instance = null;

    /** JDBC connection object used for all queries */
    private static Connection conn;

    /**
     * Returns the single instance of the DBController.
     * If it doesn't exist, creates a new one.
     *
     * @return the singleton instance
     */
	public static DBController getInstance() {
		if (instance == null) {
			instance = new DBController(); // initialization
		}
		return instance;
	}

    /**
     * Establishes connection to the MySQL database.
     * Loads the JDBC driver and connects to the local DB.
     */
	public static void connectToDB() {
		try {
			// Loads and initializes the MySQL JDBC driver class
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
			System.out.println("Driver definition succeed");
		} catch (Exception ex) {
			System.out.println("Driver definition failed"); // error in driver loading
		}

		try {
			// Connects to the local MySQL server (replace credentials as needed)
			conn = DriverManager.getConnection("jdbc:mysql://localhost/bpark?serverTimezone=Asia/Jerusalem", "root",
					"Aa123456");
			System.out.println("SQL connection succeed");
		} catch (SQLException ex) {
			// Prints SQL error information if connection fails
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
	}

	/**
	 * Closes the connection to the database.
	 */
	public void disconnectFromDB() {
		try {
			conn.close(); // close JDBC connection
		} catch (Exception e) {
			System.out.println("Error! " + e.getMessage()); // log if failed
		}
	}

	/**
	 * Updates the parking space assigned to a specific order in the database.
	 *
	 * @param newParkingSpace The new parking space number to assign.
	 * @param orderNumber     The unique identifier of the order to update.
	 * @return true if the order was found and updated, false otherwise.
	 */
	public boolean updateParkingSpace(int newParkingSpace, int orderNumber) {
		String query = "UPDATE `order` SET parking_space=? WHERE order_number=?";

		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setInt(1, newParkingSpace);
			stmt.setInt(2, orderNumber);

			// Execute the update and check if exactly one row was affected
			int updatedRows = stmt.executeUpdate();
			return updatedRows == 1;

		} catch (SQLException e) {
			System.err.println("Error updating parking space for order " + orderNumber + ": " + e.getMessage());
			return false;
		}
	}

	/**
	 * Authenticates a user using their username and password. If the credentials
	 * match a record in the 'user' table, returns a User object with their assigned
	 * role. The password is not returned in the User object for security reasons.
	 *
	 * @param username the username provided by the client
	 * @param password the password provided by the client
	 * @return a User object (username + role) if authentication succeeds, or null
	 *         if it fails
	 */
	public User authenticateUser(String username, String password) {
		String query = "SELECT role FROM bpark.user WHERE username = ? AND password = ?";

		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setString(1, username);
			stmt.setString(2, password);

			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					String roleStr = rs.getString("role");

					try {
						UserRole role = UserRole.valueOf(roleStr); // Convert role string to enum
						return new User(username, role); // Password not returned
					} catch (IllegalArgumentException e) {
						System.err.println("[ERROR] Unknown role in database: " + roleStr);
					}
				}
			}

		} catch (SQLException e) {
			System.err.println("[ERROR] Authentication query failed: " + e.getMessage());
		}

		return null;
	}

	/**
	 * Checks if a subscriber with the given subscriberCode exists in the database.
	 *
	 * @param subscriberCode the code to check
	 * @return true if exists, false otherwise
	 */
	public boolean subscriberExists(int subscriberCode) {
		String query = "SELECT 1 FROM subscriber WHERE subscriberCode = ? LIMIT 1";

		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setInt(1, subscriberCode);
			try (ResultSet rs = stmt.executeQuery()) {
				return rs.next(); // If we get a result, subscriber exists
			}
		} catch (SQLException e) {
			System.err.println("Error checking subscriber existence: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Retrieves the subscriber code associated with a given tag ID. This version
	 * performs a case-sensitive match using the BINARY keyword.
	 *
	 * @param tagId the tag identifier (e.g. "TAG_001")
	 * @return the subscriberCode if found, or -1 if not found or error
	 */
	public int getSubscriberCodeByTag(String tagId) {
		String query = "SELECT subscriberCode FROM subscriber WHERE BINARY tagId = ?";

		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setString(1, tagId);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				return rs.getInt("subscriberCode");
			}
		} catch (SQLException e) {
			System.err.println("Error retrieving subscriber by tag: " + e.getMessage());
		}

		return -1; // Not found or error
	}

	/**
	 * Retrieves the latest open parking event for the specified subscriber and
	 * parking code. An event is considered open if its exitDate is null (i.e., the
	 * vehicle has not exited yet).
	 *
	 * @param subscriberCode the subscriber's unique identifier
	 * @param parkingCode    the numeric code assigned when the vehicle entered
	 * @return a ParkingEvent object if a match is found; null otherwise
	 * @throws SQLException if a database access error occurs
	 */
	public ParkingEvent getOpenParkingEvent(int subscriberCode, int parkingCode) throws SQLException {
		String query = "SELECT * FROM parkingEvent "
				+ "WHERE subscriberCode = ? AND parkingCode = ? AND exitDate IS NULL "
				+ "ORDER BY eventId DESC LIMIT 1";

		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setInt(1, subscriberCode);
			stmt.setInt(2, parkingCode);

			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					LocalDate exitDate = rs.getDate("exitDate") != null ? rs.getDate("exitDate").toLocalDate() : null;
					LocalTime exitHour = rs.getTime("exitHour") != null ? rs.getTime("exitHour").toLocalTime() : null;

					ParkingEvent event = new ParkingEvent(rs.getInt("subscriberCode"), rs.getInt("parking_space"),
							rs.getDate("entryDate").toLocalDate(), rs.getTime("entryHour").toLocalTime(), exitDate,
							exitHour, rs.getBoolean("wasExtended"), rs.getString("vehicleId"),
							rs.getString("NameParkingLot"), rs.getString("parkingCode"));

					event.setEventId(rs.getInt("eventId")); // Set ID after object construction
					return event;
				}
			}
		}

		return null;
	}

	/**
	 * Handles the vehicle pickup process for a subscriber.
	 *
	 * First, retrieves the active parking event by subscriber and parking code.
	 * Then finalizes the parking and calculates total hours parked.
	 * 
	 * Allowed duration is up to 4 hours normally, or 8 hours if extended.
	 * 
	 * If the parking duration exceeds the allowed time, a delay is recorded. A
	 * notification is printed to the console (email/SMS sent by the server).
	 *
	 * @param subscriberCode the subscriber's code
	 * @param parkingCode    the parking code provided by the subscriber
	 * @return ServerResponse with success status and message to display
	 */
	public ServerResponse handleVehiclePickup(int subscriberCode, int parkingCode) {
		ParkingEvent event;

		try {
			event = getOpenParkingEvent(subscriberCode, parkingCode);
		} catch (SQLException e) {
			System.err.println("Error retrieving parking event: " + e.getMessage());
			return new ServerResponse(false, null, "An error occurred while retrieving your parking session.");
		}

		if (event == null) {
			return new ServerResponse(false, null, "No active parking session found for the provided information.");
		}

		try {
			finalizeParkingEvent(event.getEventId());

			// Calculate time difference in hours
			LocalDateTime entryTime = LocalDateTime.of(event.getEntryDate(), event.getEntryHour());
			long hours = (System.currentTimeMillis()
					- entryTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()) / (1000 * 60 * 60);

			int allowedHours = event.isWasExtended() ? 8 : 4;

			if (hours > allowedHours) {
				System.out.println(
						"[NOTIFY] Subscriber " + subscriberCode + " had a delayed pickup (" + hours + " hours)");
				return new ServerResponse(true, null, "Pickup successful with delay. A notification was sent.");
			}

			return new ServerResponse(true, null, "Vehicle pickup successful (" + hours + " hours).");

		} catch (SQLException e) {
			System.err.println("Failed to finalize parking event: " + e.getMessage());
			return new ServerResponse(false, null, "An error occurred while completing the pickup process.");
		}
	}

	/**
	 * Finalizes a parking event by: - Setting exitDate and exitHour. - Marking the
	 * parking space as unoccupied. - Decreasing occupiedSpots in the corresponding
	 * parking lot.
	 *
	 * @param eventId the ID of the event to finalize
	 * @throws SQLException if any update fails
	 */
	private void finalizeParkingEvent(int eventId) throws SQLException {
		// Step 1: Fetch the parking_space and parking lot name for this event
		String fetchQuery = "SELECT parking_space, NameParkingLot FROM parkingEvent WHERE eventId = ?";
		int parkingSpace = -1;
		String lotName = null;

		try (PreparedStatement fetchStmt = conn.prepareStatement(fetchQuery)) {
			fetchStmt.setInt(1, eventId);
			ResultSet rs = fetchStmt.executeQuery();
			if (rs.next()) {
				parkingSpace = rs.getInt("parking_space");
				lotName = rs.getString("NameParkingLot");
			} else {
				throw new SQLException("No event found with eventId = " + eventId);
			}
		}

		// Step 2: Finalize the event (set exitDate and exitHour)
		String finalizeQuery = "UPDATE parkingEvent SET exitDate = CURDATE(), exitHour = CURTIME() WHERE eventId = ?";
		try (PreparedStatement finalizeStmt = conn.prepareStatement(finalizeQuery)) {
			finalizeStmt.setInt(1, eventId);
			finalizeStmt.executeUpdate();
		}

		// Step 3: Mark parking space as available
		String updateSpace = "UPDATE parkingSpaces SET is_occupied = FALSE WHERE parking_space = ?";
		try (PreparedStatement spaceStmt = conn.prepareStatement(updateSpace)) {
			spaceStmt.setInt(1, parkingSpace);
			spaceStmt.executeUpdate();
		}

		// Step 4: Decrease occupiedSpots in the parking lot
		String updateLot = "UPDATE parkingLot SET occupiedSpots = occupiedSpots - 1 WHERE NameParkingLot = ?";
		try (PreparedStatement lotStmt = conn.prepareStatement(updateLot)) {
			lotStmt.setString(1, lotName);
			lotStmt.executeUpdate();
		}
	}

	/**
	 * Updates the latest parking event for the subscriber to indicate it was
	 * extended.
	 *
	 * @param subscriberCode the subscriber's code
	 * @return true if the update succeeded, false otherwise
	 */
	public boolean updateWasExtended(int subscriberCode) {
		String updateQuery = "UPDATE parkingEvent " + "SET wasExtended = TRUE " + "WHERE subscriberCode = ? "
				+ "ORDER BY eventId DESC LIMIT 1";

		try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
			stmt.setInt(1, subscriberCode);
			int rowsAffected = stmt.executeUpdate();
			return rowsAffected == 1;
		} catch (SQLException e) {
			System.err.println("Error updating wasExtended: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Retrieves the names of all parking lots from the database.
	 *
	 * @return list of parking lot names
	 */
	public ArrayList<String> parkingLotList() {
		String query = "SELECT * FROM bpark.parkingLot";
		ArrayList<String> parkingLotArrayList = new ArrayList<>();

		try (PreparedStatement stmt = conn.prepareStatement(query); ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {
				parkingLotArrayList.add(rs.getString("NameParkingLot"));
			}

		} catch (SQLException e) {
			System.err.println("Error retrieving parking lot names: " + e.getMessage());
		}

		return parkingLotArrayList;
	}

	/**
	 * 
	 * give parking space to new order
	 * 
	 * @param time
	 * @param date,
	 * @return parking space id (int)
	 */
	private int getParkingSpace(Time time, Date date) {
		String query = "SELECT PS.parking_space FROM bpark.parkingspaces PS WHERE ps.parking_space NOT IN (SELECT O.parking_space FROM bpark.order O WHERE order_date=? AND arrival_time<DATE_ADD(TIMESTAMP(?, ?), INTERVAL 4 HOUR) AND"
				+ "                DATE_ADD(TIMESTAMP(O.order_date,O.arrival_time), INTERVAL 4 HOUR)>TIMESTAMP(?, ?)) LIMIT 1;";
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setDate(1, date);
			stmt.setDate(2, date);
			stmt.setTime(3, time);
			stmt.setDate(4, date);
			stmt.setTime(5, time);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1);
				}
			}
		} catch (SQLException e) {
			System.out.println("Error! " + e.getMessage());
		}
		return -1;
	}

	/**
	 * 
	 * check using query if there is enough parking space to make a reservation (at
	 * least 40%)
	 * 
	 * @param date
	 * @param time,
	 * @return true\false
	 */
	public boolean parkingSpaceCheckingForNewOrder(Date date, Time time) {
		String query = "SELECT (100-COUNT(DISTINCT parking_space))>=40 AS canOrder FROM( SELECT parking_space, TIMESTAMP (order_date, arrival_time) AS startTime, DATE_ADD(TIMESTAMP(order_date, arrival_time), INTERVAL 4 HOUR) AS endTime FROM bpark.order) AS orders WHERE startTime<? AND endTime>?";
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			Timestamp requestToStart = Timestamp.valueOf(date.toString() + " " + time.toString());
			Timestamp requestToEnd = new Timestamp(requestToStart.getTime() + 46060 * 1000);
			stmt.setTimestamp(1, requestToEnd);
			stmt.setTimestamp(2, requestToStart);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return rs.getBoolean("canOrder");
				}
			}
		} catch (SQLException e) {
			System.out.println("Error! " + e.getMessage());
		}
		return false;
	}

	/**
	 * Updates the given Order object with its order number based on matching fields
	 * in the database. The match is done using order date, arrival time, and
	 * parking space.
	 *
	 * @param newOrder the Order object to update with its order number
	 */
	public void setOrderId(Order newOrder) {
		String newQuery = "SELECT order_number FROM `order` WHERE order_date=? AND arrival_time=? AND parking_space=?";
		try (PreparedStatement stmt = conn.prepareStatement(newQuery)) {
			stmt.setDate(1, newOrder.getOrderDate());
			stmt.setTime(2, newOrder.getArrivalTime());
			stmt.setInt(3, newOrder.getParkingSpace());
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					newOrder.setOrderNumber(rs.getInt(1));
					System.out.println(((Integer) newOrder.getOrderNumber()).toString());
				}
			}
		} catch (Exception e) {
			System.out.println("Error! " + e.getMessage());
		}
	}

	/**
	 * 
	 * insert new order to orders table
	 * 
	 * @param newOrder
	 */
	public boolean placingAnNewOrder(Order newOrder) {
		String query = "INSERT INTO `order` (parking_space, order_date, arrival_time ,confirmation_code, subscriberCode, date_of_placing_an_order) VALUES (?, ?, ?, ?, ?, ?)";
		int parking_space_id = getParkingSpace(newOrder.getArrivalTime(), newOrder.getOrderDate());
		newOrder.setParkingSpace(parking_space_id);
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setInt(1, newOrder.getParkingSpace());
			stmt.setDate(2, newOrder.getOrderDate());
			stmt.setTime(3, newOrder.getArrivalTime());
			stmt.setString(4, newOrder.getConfirmationCode());
			stmt.setInt(5, newOrder.getSubscriberId());
			stmt.setDate(6, newOrder.getDateOfPlacingAnOrder());

			int succeed = stmt.executeUpdate();
			if (succeed > 0) {
				setOrderId(newOrder);
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			// Error during update execution
			System.out.println("Error! " + e.getMessage());
			return false;
		}
	}

	/**
	 * get details after subscriber log in
	 * 
	 * @param user
	 * @return
	 */
	public Subscriber getDetailsOfSubscriber(User user) {
		String query = "SELECT * FROM subscriber WHERE username=?";
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setString(1, user.getUsername());
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					Subscriber subscriber = new Subscriber(user.getUsername());
					subscriber.setSubscriberCode(rs.getInt(1));
					subscriber.setUserId(rs.getString(2));
					subscriber.setFirstName(rs.getString(3));
					subscriber.setLastName(rs.getString(4));
					subscriber.setPhoneNum(rs.getString(5));
					subscriber.setEmail(rs.getString(6));
					subscriber.setTagId(rs.getString(8));
					return subscriber;
				}
			}
		} catch (Exception e) {
			System.out.println("Error! " + e.getMessage());
		}
		return null;
	}

	/**
	 * Checks whether the given subscriber has a valid reservation at the current
	 * date and time. A reservation is considered valid only if: - The reservation
	 * date is today. - The current time is within 15 minutes after the scheduled
	 * arrival time.
	 *
	 * For example: if a reservation is for 08:00, it is only valid between 08:00
	 * and 08:15. After 08:15, entry is no longer allowed.
	 *
	 * @param subscriberCode The unique identifier of the subscriber to check.
	 * @return true if a valid reservation exists now, false otherwise.
	 */
	public boolean checkSubscriberHasReservationNow(int subscriberCode) {

		String query = "SELECT order_date, arrival_time FROM bpark.order WHERE subscriberCode = ?";

		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setInt(1, subscriberCode);
			ResultSet rs = stmt.executeQuery();

			LocalDate today = LocalDate.now(); // Current date
			LocalTime now = LocalTime.now(); // Current time

			// Going over all of the subscriber's reservation to check if there's a
			// reservation in the range
			while (rs.next()) {
				LocalDate orderDate = rs.getDate("order_date").toLocalDate();
				LocalTime arrivalTime = rs.getTime("arrival_time").toLocalTime();

				// Check if the order is for today
				if (orderDate.equals(today)) {
					// Reservation is valid only for 15 minutes from arrivalTime
					LocalTime latestAllowedEntry = arrivalTime.plusMinutes(15);

					// If current time is between arrivalTime and arrivalTime + 15 minutes
					if (!now.isBefore(arrivalTime) && now.isBefore(latestAllowedEntry)) {
						return true; // Valid reservation window
					}
				}
			}

		} catch (SQLException e) {
			System.err.println("SQL error in checkSubscriberHasReservationNow: " + e.getMessage());
		}

		return false; // No active reservation found for current time
	}

	/**
	 * Checks if a subscriber has a valid reservation with a specific confirmation
	 * code and that the current time is within 15 minutes from the arrival time.
	 *
	 * @param subscriberCode   The subscriber code to check
	 * @param confirmationCode The confirmation code to verify
	 * @return true if a valid reservation with the given confirmation code exists
	 *         and the current time is within the allowed window, false otherwise
	 */
	public boolean checkConfirmationCode(int subscriberCode, int confirmationCode) {
		String query = "SELECT order_date, arrival_time FROM bpark.order WHERE subscriberCode = ? AND confirmation_code = ?";

		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setInt(1, subscriberCode);
			stmt.setInt(2, confirmationCode);

			// Execute the query
			ResultSet rs = stmt.executeQuery();

			LocalDate today = LocalDate.now(); // Current date
			LocalTime now = LocalTime.now(); // Current time

			// Going over all of the subscriber's reservation to check if there's a
			// reservation in the range
			while (rs.next()) {
				LocalDate orderDate = rs.getDate("order_date").toLocalDate();
				LocalTime arrivalTime = rs.getTime("arrival_time").toLocalTime();

				// Check if the order is for today
				if (orderDate.equals(today)) {
					// Reservation is valid only for 15 minutes from arrivalTime
					LocalTime latestAllowedEntry = arrivalTime.plusMinutes(15);

					// If current time is between arrivalTime and arrivalTime + 15 minutes
					if (!now.isBefore(arrivalTime) && now.isBefore(latestAllowedEntry)) {
						return true; // Valid reservation window
					}
				}
			}

		} catch (SQLException e) {
			System.err.println("Error while checking confirmation code: " + e.getMessage());
		}

		return false; // No confirmation code matching has found
	}

	/**
	 * Checks if there are available parking spots in the specified parking lot.
	 *
	 * @param parkingLotName The name of the parking lot to check.
	 * @return true if there are available spots (occupied < total), false
	 *         otherwise.
	 */
	public synchronized int hasAvailableSpots(String parkingLotName) {
		String query = "SELECT totalSpots, occupiedSpots FROM bpark.parkinglot WHERE NameParkingLot = ?";

		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setString(1, parkingLotName);

			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				int totalSpots = rs.getInt("totalSpots");
				int occupiedSpots = rs.getInt("occupiedSpots");

				if(occupiedSpots < totalSpots) {
					// Updating the amount of occupied parking space by +1
					addOccupiedParkingSpace();

					// Searching for a free parking space
					int parkingSpace = findParkingSpace();

					// Updating the parking space itself to be on a occupied status
					updateParkingSpaceOccupied(parkingSpace);

					// Returning the parking space that has been found
					return parkingSpace;
				}

				// return true if there are more parking spots that occupied spots, false
				// otherwise false
				return -1;
			}

		} catch (SQLException e) {
			System.err.println("Error checking available spots: " + e.getMessage());
		}

		System.out.println("error");
		return -1; // Return false if parking lot not found or error occurred
	}
	/**
	 * Finds the first available parking space that is not currently occupied.
	 *
	 * Queries the 'parkingspaces' table for a parking space where 'is_occupied' is
	 * false. Returns the 'parking_space' value of the first free spot found.
	 *
	 * @return the parking space number if available, if not found then -1.
	 */
	private int findParkingSpace() {
		String query = "SELECT parking_space FROM parkingspaces WHERE is_occupied = 0 LIMIT 1";

		try (PreparedStatement stmt = conn.prepareStatement(query); ResultSet rs = stmt.executeQuery()) {
			if (rs.next()) {
				return rs.getInt("parking_space");
			}
		} catch (SQLException e) {
			System.err.println("Error retrieving parking space: " + e.getMessage());
		}

		return -1; // No free parking space found
	}

	/**
	 * Inserts a new parking event into the 'parkingevent' table in the database.
	 *
	 * @param parkingEvent The ParkingEvent object containing all the event data to
	 *                     be stored.
	 */
	public void addParkingEvent(ParkingEvent parkingEvent) {
		String query = "INSERT INTO bpark.parkingevent (subscriberCode, parking_space, entryDate, entryHour, exitDate, exitHour, wasExtended, vehicleId, NameParkingLot, parkingCode) VALUES "
				+ "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setInt(1, parkingEvent.getSubscriberCode());
			stmt.setInt(2, parkingEvent.getParkingSpace());
			stmt.setDate(3, Date.valueOf(parkingEvent.getEntryDate()));
			stmt.setTime(4, Time.valueOf(parkingEvent.getEntryHour()));
			stmt.setDate(5, parkingEvent.getExitDate() != null ? Date.valueOf(parkingEvent.getExitDate()) : null);
			stmt.setTime(6, parkingEvent.getExitHour() != null ? Time.valueOf(parkingEvent.getExitHour()) : null);
			stmt.setBoolean(7, parkingEvent.isWasExtended());
			stmt.setString(8, parkingEvent.getVehicleId());
			stmt.setString(9, parkingEvent.getLot());
			stmt.setString(10, parkingEvent.getParkingCode());

			stmt.executeUpdate();

			System.out.println(">> INSERTING PARKING EVENT FOR: " + parkingEvent.getSubscriberCode());
		} catch (SQLException e) {
			System.err.println("Error inserting parking event: " + e.getMessage());
		}
	}

	/**
	 * Increments the number of occupied parking spots in the 'parkinglot' table by
	 * 1.
	 */
	private void addOccupiedParkingSpace() {
		String query = "UPDATE bpark.parkinglot SET occupiedSpots = occupiedSpots + 1 WHERE NameParkingLot = 'Braude'";

		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.println("Error updating occupied parking spots: " + e.getMessage());
		}
	}

	/**
	 * Updates the specified parking space to be marked as occupied in the
	 * 'parkingspaces' table.
	 *
	 * @param parkingSpace The parking space number to mark as occupied (as a
	 *                     String).
	 */
	private void updateParkingSpaceOccupied(int parkingSpace) {
		String query = "UPDATE bpark.parkingspaces SET is_occupied = 1 WHERE parking_space = ?";

		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setInt(1, parkingSpace); // Parse String to int
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.println("Error updating parking space occupancy: " + e.getMessage());
		} catch (NumberFormatException e) {
			System.err.println("Invalid parking space format: " + parkingSpace);
		}
	}

	/**
	 * Finds the vehicle ID associated with the given subscriber code.
	 * 
	 * Executes a query on the 'vehicle' table to retrieve the vehicleId for the
	 * specified subscriberCode.
	 *
	 * @param subscriberCode the unique code of the subscriber.
	 * @return the vehicle ID if found, otherwise -1.
	 */
	public String findVehicleID(int subscriberCode) {
		String query = "SELECT vehicleId FROM vehicle WHERE subscriberCode = ?";

		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setInt(1, subscriberCode);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				return rs.getString("vehicleId");
			}
		} catch (SQLException e) {
			System.err.println("Error finding vehicle ID: " + e.getMessage());
		}

		return null; // Vehicle ID matching to this subscriber not found
	}

	/**
	 * the function get all the reservations of subscriber and return them.
	 * 
	 * @param subscriber
	 * @return arrayList of subscriber's reservations
	 */
	public ArrayList<Order> returnReservationOfSubscriber(Subscriber subscriber) {
		String query = "SELECT * FROM `order` WHERE subscriberCode=? AND order_date>CURDATE()+INTERVAL 1 DAY";
		ArrayList<Order> orders = new ArrayList<>();
		int subsCode = subscriber.getSubscriberCode();
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setInt(1, subsCode);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					Order newOrder = new Order(rs.getInt("order_number"), rs.getInt("parking_space"),
							rs.getDate("order_date"), rs.getTime("arrival_time"), rs.getString("confirmation_code"),
							rs.getInt("subscriberCode"), rs.getDate("date_of_placing_an_order"));
					orders.add(newOrder);
				}
				return orders;
			}
		} catch (SQLException e) {
			System.err.println("Error finding reservations: " + e.getMessage());
		}
		return null;
	}

	/**
	 * delete the order from SQL
	 * 
	 * @param orderNumber
	 * @return true if succeed, else- false
	 */
	public boolean deleteOrder(int orderNumber) {
		String query = "DELETE FROM `order` WHERE order_number=?";
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setInt(1, orderNumber);
			int ifDelete = stmt.executeUpdate();
			return (ifDelete > 0);
		} catch (SQLException e) {
			System.err.println("Error delete order: " + e.getMessage());
			return false;
		}
	}

	/**
	 * update the password of user
	 * 
	 * @param user - for updating details
	 * @return if the action succeed
	 */
	public boolean changeDetailsOfUser(User user) {
		System.out.println("user");
		String query = "UPDATE user SET password=? WHERE username=?";
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setString(1, user.getPassword());
			stmt.setString(2, user.getUsername());
			int rowsUpdated = stmt.executeUpdate();
			return rowsUpdated > 0;
		} catch (SQLException e) {
			System.err.println("Error update user: " + e.getMessage());
			return false;
		}
	}

	/**
	 * update the details of subscriber
	 * 
	 * @param subscriber - for updating details
	 * @return if the action succeed
	 */
	public boolean changeDetailsOfSubscriber(Subscriber subscriber) {
		String query = "UPDATE subscriber SET firstName=?, lastName=?, phoneNumber=?,  email=? WHERE subscriberCode=?";
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setString(1, subscriber.getFirstName());
			stmt.setString(2, subscriber.getLastName());
			stmt.setString(3, subscriber.getPhoneNum());
			stmt.setString(4, subscriber.getEmail());
			stmt.setInt(5, subscriber.getSubscriberCode());
			int rowsUpdated = stmt.executeUpdate();
			return rowsUpdated > 0;
		} catch (SQLException e) {
			System.err.println("Error update subscriber: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Retrieves the full parking history of a given subscriber.
	 *
	 * @param subscriber the subscriber whose parking events are requested
	 * @return a list of ParkingEvent objects representing the subscriber's parking
	 *         history, or null if an error occurs during retrieval
	 */
	public ArrayList<ParkingEvent> parkingHistoryOfSubscriber(Subscriber subscriber) {
		String query = "SELECT * FROM parkingEvent WHERE subscriberCode=?";
		ArrayList<ParkingEvent> historyOfParking = new ArrayList<>();
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setInt(1, subscriber.getSubscriberCode());
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					ParkingEvent newParkingEvent = new ParkingEvent();
					newParkingEvent.setEventId(rs.getInt("eventId"));
					newParkingEvent.setSubscriberCode(rs.getInt("subscriberCode"));
					newParkingEvent.setParkingSpace(rs.getInt("parking_space"));
					newParkingEvent.setEntryDate((rs.getDate("entryDate")).toLocalDate());
					newParkingEvent.setEntryTime((rs.getTime("entryHour")).toLocalTime());
					Date exitDate = rs.getDate("exitDate");
					Time exitTime = rs.getTime("exitHour");
					if (exitDate != null && exitTime != null) {
						newParkingEvent.setExitDate(rs.getDate("exitDate").toLocalDate());
						newParkingEvent.setExitTime(rs.getTime("exitHour").toLocalTime());
					}
					newParkingEvent.setWasExtended(rs.getBoolean("wasExtended"));
					newParkingEvent.setVehicleID(rs.getString("vehicleId"));
					newParkingEvent.setLot(rs.getString("NameParkingLot"));
					newParkingEvent.setParkingCode(rs.getString("parkingCode"));
					historyOfParking.add(newParkingEvent);
				}
				return historyOfParking;
			}

		} catch (Exception e) {
			System.err.println("Error in parking history: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Method that checks whether the tag exists in the DB or no If yes then return
	 * true, false otherwise
	 */
	public boolean tagExists(String tag) {

		boolean exists = false;

		try {
			// Checking whether the tag exists or not
			String query = "SELECT 1 FROM bpark.subscriber WHERE tagId = ? LIMIT 1";

			PreparedStatement ps = conn.prepareStatement(query);
			ps.setString(1, tag); // Inserting the tag

			ResultSet rs = ps.executeQuery();
			exists = rs.next(); // If the tag has been found it means that he will exists

			rs.close();
			ps.close();
		} catch (SQLException e) {
			System.err.println("Error finding Tag: " + e.getMessage());
		}

		return exists;
	}

	/**
	 * Method that returns the subscriber through a matched tag-Id with the DB
	 * Returns the subscriberCode, in case of an error returns -1
	 */
	public int seekForTheSubscriberWithTag(String tag) {
		String query = "SELECT s.subscriberCode FROM bpark.subscriber s WHERE s.tagId = ?";

		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setString(1, tag);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				return rs.getInt("subscriberCode");
			}
		} catch (SQLException e) {
			System.err.println("Error finding subscriber code by tag: " + e.getMessage());
		}
		return -1; // Subscriber code matching to the tag not found
	}

	/**
	 * Checks whether the subscriber already has entered his vehicle into the
	 * parking lot If he entered already return true, otherwise return false
	 */
	public boolean checkSubscriberEntered(int codeInt) {
		String query = "SELECT * FROM bpark.parkingevent WHERE subscriberCode = ? AND exitHour IS NULL";

		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setInt(1, codeInt);
			ResultSet rs = stmt.executeQuery();
			return rs.next(); // if any row is returned, subscriber is inside
		} catch (SQLException e) {
			// If there is no subscriber code in the table it means that he didn't entered
			// yet and the method will return false
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Checks whether the vehicle that is matched to the tag-Id is already inside
	 * the parking lot If he entered already return true, otherwise return false
	 */
	public boolean checkTagIDEntered(String tag) {
		String query = "SELECT pe.* FROM bpark.parkingevent pe "
				+ "JOIN bpark.subscriber s ON pe.subscriberCode = s.subscriberCode "
				+ "WHERE s.tagId = ? AND pe.exitHour IS NULL";

		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setString(1, tag);
			ResultSet rs = stmt.executeQuery();
			return rs.next(); // if any row is returned, tag is inside
		} catch (SQLException e) {
			// If there is no matched subscriberCode to the tagId in the table it means that
			// he didn't entered yet and the method will return false
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Retrieves the email and phone number of a subscriber by their subscriber
	 * code.
	 *
	 * @param subscriberCode the unique code of the subscriber
	 * @return a String array where index 0 is email and index 1 is phone number, or
	 *         null if the subscriber was not found or an error occurred
	 */
	public String[] getEmailAndPhoneNumber(int subscriberCode) {
		String query = "SELECT phoneNumber, email FROM subscriber WHERE subscriberCode=?";
		String[] arrayForPhoneAndEmail = new String[2];
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setInt(1, subscriberCode);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				arrayForPhoneAndEmail[0] = rs.getString("email");
				arrayForPhoneAndEmail[1] = rs.getString("phoneNumber");
				return arrayForPhoneAndEmail;
			}
		} catch (SQLException e) {
			System.out.println("Error finding email and phone number: " + e.getMessage());
		}
		return null;
	}

	/**
	 * Returns every subscriber plus a computed "late" count.
	 *
	 * Late = (duration > 8 h) OR (duration > 4 h AND wasExtended = FALSE)
	 *
	 * @return List<Object[]> where index 0 -> Subscriber, index 1 -> Integer
	 *         lateCount
	 */
	public List<Object[]> getAllSubscribersWithLateCount() {
		final String sql = """
				SELECT s.subscriberCode,
				       s.userId,
				       s.firstName,
				       s.lastName,
				       s.phoneNumber,
				       s.email,
				       s.username,
				       s.tagId,
				       COALESCE(l.late_cnt, 0) AS late_count
				FROM   bpark.subscriber AS s
				LEFT JOIN (
				    SELECT subscriberCode,
				           COUNT(*) AS late_cnt
				    FROM   bpark.parkingEvent
				    WHERE  exitDate IS NOT NULL
				      AND (
				           TIMESTAMPDIFF(
				             HOUR,
				             TIMESTAMP(entryDate, entryHour),
				             TIMESTAMP(exitDate, exitHour)
				           ) > 8
				         OR (
				             wasExtended = FALSE
				             AND TIMESTAMPDIFF(
				                   HOUR,
				                   TIMESTAMP(entryDate, entryHour),
				                   TIMESTAMP(exitDate, exitHour)
				                ) > 4
				         )
				      )
				    GROUP BY subscriberCode
				) AS l USING (subscriberCode)
				ORDER BY s.subscriberCode;
				""";

		List<Object[]> result = new ArrayList<>();

		try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {
				Subscriber sub = new Subscriber(rs.getInt("subscriberCode"), rs.getString("userId"),
						rs.getString("firstName"), rs.getString("lastName"), rs.getString("phoneNumber"),
						rs.getString("email"), rs.getString("username"), rs.getString("tagId"));

				int lateCount = rs.getInt("late_count");
				result.add(new Object[] { sub, lateCount });
			}
		} catch (SQLException e) {
			e.printStackTrace(); // replace with proper logging if required
		}
		return result;
	}

	/**
	 * Retrieves all active (open) parking events from the database. Active events
	 * are those that have no recorded exit date/time.
	 *
	 * @return a list of ParkingEvent objects
	 */
	public List<ParkingEvent> getActiveParkingEvents() {
		List<ParkingEvent> list = new ArrayList<>();

		String sql = "SELECT * FROM parkingEvent WHERE exitDate IS NULL AND exitHour IS NULL";

		try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {
				ParkingEvent event = new ParkingEvent();

				event.setEventId(rs.getInt("eventId"));
				event.setSubscriberCode(rs.getInt("subscriberCode"));
				event.setParkingSpace(rs.getInt("parking_space"));
				event.setEntryDate(rs.getDate("entryDate").toLocalDate());
				event.setEntryTime(rs.getTime("entryHour").toLocalTime());
				event.setWasExtended(rs.getBoolean("wasExtended"));
				event.setLot(rs.getString("nameParkingLot"));
				event.setVehicleID(rs.getString("vehicleId"));
				event.setParkingCode(rs.getString("parkingCode"));

				list.add(event);
			}

		} catch (SQLException e) {
			System.err.println("Error retrieving active parking events: " + e.getMessage());
		}

		return list;
	}

	/**
	 * Retrieves the active parking event for a given subscriber. A parking event is
	 * considered active if it has no recorded exit time.
	 *
	 * @param subscriber the subscriber whose active parking event is requested
	 * @return the active ParkingEvent if found, or null if not found or error
	 *         occurs
	 */
	public ParkingEvent getActiveParkingEvent(Subscriber subscriber) {
		String query = "SELECT * FROM parkingEvent WHERE subscriberCode=? AND exitHour IS NULL";
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setInt(1, subscriber.getSubscriberCode());
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				ParkingEvent newParkingEvent = new ParkingEvent();
				newParkingEvent.setEventId(rs.getInt("eventId"));
				newParkingEvent.setSubscriberCode(rs.getInt("subscriberCode"));
				newParkingEvent.setParkingSpace(rs.getInt("parking_space"));
				newParkingEvent.setEntryDate((rs.getDate("entryDate")).toLocalDate());
				newParkingEvent.setEntryTime((rs.getTime("entryHour")).toLocalTime());
				newParkingEvent.setWasExtended(rs.getBoolean("wasExtended"));
				newParkingEvent.setVehicleID(rs.getString("vehicleId"));
				newParkingEvent.setLot(rs.getString("NameParkingLot"));
				newParkingEvent.setParkingCode(rs.getString("parkingCode"));
				return newParkingEvent;
			}
		} catch (SQLException e) {
			System.out.println("Error finding active parking info: " + e.getMessage());
		}
		return null;
	}

	/**
	 * Attempts to extend an active parking session for a given parking code and subscriber.
	 * The extension is only allowed if:
	 * - The parking session has not yet ended (exitDate and exitHour are NULL)
	 * - The session has not been extended before (wasExtended = FALSE)
	 * - The session belongs to the provided subscriber code
	 *
	 * @param parkingCode    the code of the parking session to extend
	 * @param subscriberCode the code identifying the subscriber attempting the extension
	 * @return a {@link ServerResponse} indicating whether the extension was successful,
	 *         including a message explaining the result
	 */
	public ServerResponse extendParkingSession(int parkingCode, String subscriberCode) {
		final String sql =
				"UPDATE bpark.parkingEvent " +
						"SET wasExtended = TRUE " +
						"WHERE parkingCode = ? " +
						"AND subscriberCode = ? " +
						"AND exitDate IS NULL AND exitHour IS NULL " +
						"AND wasExtended = FALSE";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, parkingCode);
			stmt.setString(2, subscriberCode);
			int rowsAffected = stmt.executeUpdate();

			if (rowsAffected > 0) {
				return new ServerResponse(true, null, "Parking session extended successfully.");
			} else {
				return new ServerResponse(false, null, "Invalid code.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return new ServerResponse(false, null, "Database error: " + e.getMessage());
		}
	}

	/**
	 * Returns the next available subscriber code (MAX + 1). Starts from 1011 if
	 * table is empty.
	 */
	public int getNextSubscriberCode() {
		String query = "SELECT MAX(subscriberCode) FROM bpark.subscriber";
		try (PreparedStatement stmt = conn.prepareStatement(query); ResultSet rs = stmt.executeQuery()) {
			if (rs.next()) {
				int currentMax = rs.getInt(1);
				return (currentMax == 0 ? 1011 : currentMax + 1);
			}
		} catch (SQLException e) {
			System.err.println("Error generating subscriber code: " + e.getMessage());
		}
		return 1011; // Fallback starting code
	}

	/**
	 * Generates the next unique tag ID in the format TAG_XXX.
	 */
	public String generateNextTagId() {
		String query = "SELECT COUNT(*) FROM bpark.subscriber";
		try (PreparedStatement stmt = conn.prepareStatement(query); ResultSet rs = stmt.executeQuery()) {
			if (rs.next()) {
				int count = rs.getInt(1) + 1;
				return "TAG_" + String.format("%03d", count);
			}
		} catch (SQLException e) {
			System.err.println("Error generating tag ID: " + e.getMessage());
		}
		return "TAG_999";
	}

	/**
	 * Inserts a new subscriber into the database.
	 *
	 * @param s the subscriber object to insert
	 * @return true if insertion succeeded
	 */
	public boolean insertSubscriber(Subscriber s) {
		String sql = "INSERT INTO bpark.subscriber "
				+ "(subscriberCode, userId, firstName, lastName, phoneNumber, email, username, tagId) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, s.getSubscriberCode());
			stmt.setString(2, s.getUserId());
			stmt.setString(3, s.getFirstName());
			stmt.setString(4, s.getLastName());
			stmt.setString(5, s.getPhoneNum());
			stmt.setString(6, s.getEmail());
			stmt.setString(7, s.getUsername());
			stmt.setString(8, s.getTagId());
			return stmt.executeUpdate() == 1;
		} catch (SQLException e) {
			System.err.println("Error inserting subscriber: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Inserts a new user into the database.
	 *
	 * @param user the user object containing username, password, and role
	 * @return true if the user was successfully inserted, false otherwise
	 */
	public boolean insertUser(User user) {
		String sql = "INSERT INTO bpark.user (username, password, role) VALUES (?, ?, ?)";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, user.getUsername());
			stmt.setString(2, user.getPassword());
			stmt.setString(3, user.getRole().toString());
			return stmt.executeUpdate() == 1;
		} catch (SQLException e) {
			return false;
		}
	}

	/**
	 * Checks whether the provided email is already associated with an existing
	 * subscriber.
	 *
	 * @param email the email to check
	 * @return true if the email exists, false otherwise
	 */
	public boolean emailExists(String email) {
		String query = "SELECT 1 FROM bpark.subscriber WHERE email = ? LIMIT 1";
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setString(1, email);
			ResultSet rs = stmt.executeQuery();
			return rs.next(); // true if exists
		} catch (SQLException e) {
			System.err.println("Error checking email existence: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Checks whether the provided username is already taken.
	 *
	 * @param username the username to check
	 * @return true if the username exists, false otherwise
	 */
	public boolean usernameExists(String username) {
		String query = "SELECT 1 FROM bpark.user WHERE username = ? LIMIT 1";
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setString(1, username);
			ResultSet rs = stmt.executeQuery();
			return rs.next(); // true if exists
		} catch (SQLException e) {
			System.err.println("Error checking username existence: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Checks if a phone number already exists in the subscriber table.
	 *
	 * @param phone the phone number to check
	 * @return true if the phone exists, false otherwise
	 */
	public boolean phoneExists(String phone) {
		String query = "SELECT 1 FROM bpark.subscriber WHERE phoneNumber = ? LIMIT 1";
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setString(1, phone);
			try (ResultSet rs = stmt.executeQuery()) {
				return rs.next(); // returns true if a row was found
			}
		} catch (SQLException e) {
			System.err.println("Error checking if phone exists: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Checks if a user ID (Teudat Zehut) already exists in the subscriber table.
	 *
	 * @param userId the ID to check
	 * @return true if the ID exists, false otherwise
	 */
	public boolean idExists(String userId) {
		String query = "SELECT 1 FROM bpark.subscriber WHERE userId = ? LIMIT 1";
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setString(1, userId);
			try (ResultSet rs = stmt.executeQuery()) {
				return rs.next(); // true if a row exists
			}
		} catch (SQLException e) {
			System.err.println("Error checking if ID exists: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Retrieves all active (open) parking events that late for retrieve car and
	 * doesn't receive mail from the database. Active events are those that have no
	 * recorded exit date.
	 *
	 * @return a list of ParkingEvent objects
	 */
	public ArrayList<ParkingEvent> getActiveParkingEventsThatLateAndDoesntReceiveMail() {

		ArrayList<ParkingEvent> list = new ArrayList<>();
		String query = "SELECT * FROM parkingEvent WHERE exitDate IS NULL AND sendMsgForLating=FALSE";

		try (PreparedStatement ps = conn.prepareStatement(query); ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				ParkingEvent event = new ParkingEvent();

				event.setEventId(rs.getInt("eventId"));
				event.setSubscriberCode(rs.getInt("subscriberCode"));
				event.setParkingSpace(rs.getInt("parking_space"));
				event.setEntryDate(rs.getDate("entryDate").toLocalDate());
				event.setEntryTime(rs.getTime("entryHour").toLocalTime());
				event.setWasExtended(rs.getBoolean("wasExtended"));
				LocalDateTime dateTimeEntry = event.getEntryDate().atTime(event.getEntryHour());
				if (event.isWasExtended()) {

					dateTimeEntry = dateTimeEntry.plusHours(8);

				} else {
					dateTimeEntry = dateTimeEntry.plusHours(4);
				}
				event.setExitDate(dateTimeEntry.toLocalDate()); // expected date
				event.setExitTime(dateTimeEntry.toLocalTime()); // expected time
				event.setLot(rs.getString("nameParkingLot"));
				event.setVehicleID(rs.getString("vehicleId"));
				event.setParkingCode(rs.getString("parkingCode"));
				list.add(event);

			}
			return list;
		} catch (SQLException e) {
			System.err.println("Error retrieving active parking events: " + e.getMessage());
			e.getStackTrace();
		}

		return list;
	}
	

	/**
	 * Marks a subscriber as having been notified for being late. This sets the
	 * 'sendMsgForLating' flag to true for their active parking event.
	 *
	 * @param subscriberCode the code identifying the subscriber
	 */
	public void markSendMail(int subscriberCode) {
		String query = "UPDATE parkingEvent SET sendMsgForLating=TRUE WHERE subscriberCode=?";
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setInt(1, subscriberCode);
			int rowsUpdated = stmt.executeUpdate();
		} catch (SQLException e) {
			System.out.print(e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Returns the total number of parking spots in the lot.
	 *
	 * @return total number of parkingSpaces
	 */
	public int getTotalSpots() {
		final String sql = "SELECT COUNT(*) FROM bpark.parkingSpaces";

		try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

			if (rs.next()) {
				return rs.getInt(1);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return 0;
	}

	/**
	 * Returns the number of currently occupied parking spots.
	 *
	 * @return count of spots where is_occupied = TRUE
	 */
	public int getOccupiedSpots() {
		final String sql = "SELECT COUNT(*) FROM bpark.parkingSpaces WHERE is_occupied = TRUE";

		try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

			if (rs.next()) {
				return rs.getInt(1);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return 0;
	}

	/**
	 * get all the data that we need to save on parking report.
	 * @param date
	 * @return parking report
	 */
	private ParkingReport getDataForParkingReport(Date date) {
		String query = "SELECT * FROM parkingEvent WHERE YEAR(entryDate)=? AND MONTH(entryDate)=?";
		LocalDate local = date.toLocalDate();
		int year = local.getYear();
		int month = local.getMonthValue();
		int totalExtends = 0;
		int totalEntries = 0;
		int totalLates=0;
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setInt(1, year);
			stmt.setInt(2, month);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				totalEntries++;
				if (rs.getBoolean("wasExtended")) {
					totalExtends++;
				}
				if(rs.getBoolean("sendMsgForLating")) {
					totalLates++;
				}
			}
			return new ParkingReport(totalEntries, totalExtends, totalLates);
		} catch (SQLException e) {
			System.out.println("Error get data for parking report: " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * create new parking report for the last month.
	 * @param date
	 */
	public void createParkingReport(Date date) {
		ParkingReport parkingReport=getDataForParkingReport(date);
		String query="INSERT INTO parkingReport(dateOfParkingReport, totalEntries, totalExtends, totalLates) VALUES (?, ?, ?, ?);";
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setDate(1, date);
			stmt.setInt(2, parkingReport.getTotalEntries());
			stmt.setInt(3, parkingReport.getTotalExtends());
			stmt.setInt(4, parkingReport.getTotalLates());
			stmt.executeUpdate();
			System.out.println("Parking reoprt created!");
		} catch (SQLException e) {
			System.out.println("Error creating parking report: "+ e.getMessage());
			e.printStackTrace();
		}	
	}
	
	/**
	 * check existence of parking report on database for creating only the missing reports.
	 * @param date (of month that we want to check)
	 * @return true if exists. false if doesn't.
	 */
	public boolean parkingReportExists(Date date) {
		String query="SELECT * FROM parkingReport WHERE dateOfParkingReport=?";
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setDate(1, date);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				return true;
			}
		} catch (SQLException e) {
			System.out.println("Error check existence of parking report: "+ e.getMessage());
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * get the data from parking report schema of date that manager asked for.
	 * @param date
	 * @return parking report 
	 */
	public ParkingReport getParkingReport(Date date) {
		String query="SELECT * FROM bpark.parkingReport WHERE dateOfParkingReport=?";
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setDate(1, date);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				return new ParkingReport(rs.getInt("totalEntries"), rs.getInt("totalExtends"), rs.getInt("totalLates"));
			}
		} catch (SQLException e) {
			System.out.println("Error get parking report: "+ e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
	
	/* ==============================================================
	 *  Live aggregation: build the subscriber-status list on-the-fly
	 *  (Used when the report for a month was not stored yet.)
	 * ============================================================== */

	/**
	 * Returns, in memory, the subscriber-status aggregation
	 * for the given month and year. No insert – read-only.
	 *
	 * @param month calendar month (1-12)
	 * @param year  4-digit calendar year
	 * @return list of {@link common.SubscriberStatusRow}
	 * @throws SQLException if the query blows up
	 */
	public List<SubscriberStatusRow> getSubscriberStatusLive(int month, int year)
	        throws SQLException {

	    final String sql =
	        """
	        SELECT
	            s.subscriberCode                                    AS code,
	            CONCAT(s.firstName, ' ', s.lastName)               AS fullName,
	            COUNT(pe.eventId)                                  AS totalEntries,
	            COALESCE(SUM(pe.wasExtended), 0)                   AS totalExtends,
	            COALESCE(SUM(pe.sendMsgForLating), 0)              AS totalLates,
	            COALESCE(SUM(
	                TIMESTAMPDIFF(MINUTE,
	                              TIMESTAMP(pe.entryDate, pe.entryHour),
	                              IFNULL(TIMESTAMP(pe.exitDate, pe.exitHour), NOW()))
	            ), 0) / 60.0                                       AS totalHours
	        FROM subscriber s
	        LEFT JOIN parkingEvent pe
	               ON s.subscriberCode = pe.subscriberCode
	              AND YEAR(pe.entryDate)  = ?
	              AND MONTH(pe.entryDate) = ?
	        GROUP BY s.subscriberCode, fullName
	        ORDER BY totalHours DESC;
	        """;

	    List<SubscriberStatusRow> rows = new ArrayList<>();

	    try (PreparedStatement ps = conn.prepareStatement(sql)) {
	        ps.setInt(1, year);
	        ps.setInt(2, month);
	        try (ResultSet rs = ps.executeQuery()) {
	            while (rs.next()) {
	                rows.add(new SubscriberStatusRow(
	                        rs.getInt("code"),
	                        rs.getString("fullName"),
	                        rs.getInt("totalEntries"),
	                        rs.getInt("totalExtends"),
	                        rs.getInt("totalLates"),
	                        rs.getDouble("totalHours")));
	            }
	        }
	    }
	    return rows;
	}

	/* ==============================================================
	 *  Persisted report: write one row per subscriber into the table
	 *  Used by the monthly scheduler thread.
	 * ============================================================== */

	/**
	 * Generates and stores the subscriber-status report for the
	 * specified month. Existing rows for that month are deleted first
	 * (safer re-run).
	 *
	 * @param month month to snapshot (1-12)
	 * @param year  target year
	 * @return number of rows inserted
	 * @throws SQLException if anything fails
	 */
	public int storeSubscriberStatusReport(int month, int year) throws SQLException {

	    // 1. Build the list in memory
	    List<SubscriberStatusRow> rows = getSubscriberStatusLive(month, year);

	    // 2. Clear existing snapshot (if the thread ran twice)
	    String deleteSQL =
	        "DELETE FROM subscriberStatusReport " +
	        "WHERE reportMonth = ?";
	    try (PreparedStatement del = conn.prepareStatement(deleteSQL)) {
	        del.setDate(1, java.sql.Date.valueOf(String.format("%04d-%02d-01", year, month)));
	        del.executeUpdate();
	    }

	    // 3. Insert fresh rows
	    String insertSQL =
	        "INSERT INTO subscriberStatusReport " +
	        "(reportMonth, subscriberCode, totalEntries, totalExtends, totalLates, totalHours) " +
	        "VALUES (?, ?, ?, ?, ?, ?)";

	    int inserted = 0;
	    try (PreparedStatement ins = conn.prepareStatement(insertSQL)) {
	        java.sql.Date monthKey =
	            java.sql.Date.valueOf(String.format("%04d-%02d-01", year, month));

	        for (SubscriberStatusRow r : rows) {
	            ins.setDate   (1, monthKey);
	            ins.setInt    (2, r.getCode());
	            ins.setInt    (3, r.getTotalEntries());
	            ins.setInt    (4, r.getTotalExtends());
	            ins.setInt    (5, r.getTotalLates());
	            ins.setDouble (6, r.getTotalHours());
	            ins.addBatch();
	            inserted++;
	        }
	        ins.executeBatch();
	    }
	    return inserted;
	}
	
	/**
	 * Reads the stored subscriber-status snapshot from the
	 * subscriberStatusReport table. Returns an empty list if
	 * no rows exist for the requested month.
	 *
	 * @param month calendar month (1-12)
	 * @param year  four-digit year
	 * @return list of SubscriberStatusRow (possibly empty)
	 */
	public List<SubscriberStatusRow> getSubscriberStatusFromTable(int month, int year)
	        throws SQLException {

	    String sql =
	        "SELECT subscriberCode, totalEntries, totalExtends, totalLates, totalHours, " +
	        "       CONCAT(s.firstName,' ',s.lastName) AS fullName " +
	        "FROM subscriberStatusReport r " +
	        "JOIN subscriber s USING (subscriberCode) " +
	        "WHERE reportMonth = ?";

	    List<SubscriberStatusRow> rows = new ArrayList<>();

	    try (PreparedStatement ps = conn.prepareStatement(sql)) {
	        ps.setDate(1, java.sql.Date.valueOf(String.format("%04d-%02d-01", year, month)));
	        try (ResultSet rs = ps.executeQuery()) {
	            while (rs.next()) {
	                rows.add(new SubscriberStatusRow(
	                        rs.getInt("subscriberCode"),
	                        rs.getString("fullName"),
	                        rs.getInt("totalEntries"),
	                        rs.getInt("totalExtends"),
	                        rs.getInt("totalLates"),
	                        rs.getDouble("totalHours")));
	            }
	        }
	    }
	    return rows;
	}


}
