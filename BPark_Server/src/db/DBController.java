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
import java.time.Duration;
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
			System.out.println("Error to disconnect from DB " + e.getMessage()); // log if failed
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
			return new ServerResponse(false, null, null, "An error occurred while retrieving your parking session.");
		}

		if (event == null) {
			return new ServerResponse(false, null, ResponseType.PARKING_SESSION_EXTENDED, "No active parking session found for the provided information.");
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
				return new ServerResponse(true, null, ResponseType.PICKUP_VEHICLE, "Pickup successful with delay. A notification was sent.");
			}

			return new ServerResponse(true, null, ResponseType.PICKUP_VEHICLE, "Vehicle pickup successful (" + hours + " hours).");

		} catch (SQLException e) {
			System.err.println("Failed to finalize parking event: " + e.getMessage());
			return new ServerResponse(false, null, null, "An error occurred while completing the pickup process.");
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
			System.out.println("Error get a parking space" + e.getMessage());
		}
		return -1;
	}

	/**
	 * Checks if at least 40% of the parking spaces are available for a new order
	 * at the given date and time. This ensures availability before confirming a reservation.
	 *
	 * @param date date of the desired reservation
	 * @param time time of the desired reservation
	 * @return true if reservation is allowed, false otherwise
	 */
	public synchronized boolean parkingSpaceCheckingForNewOrder(Date date, Time time) {
		String query = "SELECT (100-COUNT(DISTINCT parking_space))>=40 AS canOrder FROM( SELECT parking_space, TIMESTAMP (order_date, arrival_time) AS startTime, DATE_ADD(TIMESTAMP(order_date, arrival_time), INTERVAL 4 HOUR) AS endTime, status FROM bpark.order) AS orders WHERE startTime<? AND endTime>? AND `status`='ACTIVE'";
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
			System.out.println("Error parking space checking " + e.getMessage());
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
			System.out.println("Error set order id " + e.getMessage());
		}
	}

	/**
	 * 
	 * insert new order to orders table
	 * 
	 * @param newOrder
	 */
	public boolean placingAnNewOrder(Order newOrder) {
		String query = "INSERT INTO `order` (parking_space, order_date, arrival_time ,confirmation_code, subscriberCode, date_of_placing_an_order, `status`) VALUES (?, ?, ?, ?, ?, ?, 'ACTIVE')";
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
			System.out.println("Error placing new order " + e.getMessage());
			return false;
		}
	}

	/**
	 * Retrieves the subscriber's full details after successful user login.
	 * Looks up the subscriber in the database using the provided username and
	 * returns a populated Subscriber object (excluding password).
	 *
	 * @param user the User object containing the username to search by
	 * @return a Subscriber object with full profile information if found;
	 *         null if no match was found or an error occurred
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
			System.out.println("Error get details of subscriber " + e.getMessage());
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

		String query = "SELECT order_date, arrival_time FROM bpark.order WHERE subscriberCode = ? AND `status`='ACTIVE'";

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
		String query = "SELECT order_date, arrival_time FROM bpark.order WHERE subscriberCode = ? AND confirmation_code = ? AND `status`='ACTIVE'";

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
						String newQuery="UPDATE `order` SET `status`='FULFILLED' WHERE subscriberCode = ? AND confirmation_code = ? AND `status`='ACTIVE'";
						try (PreparedStatement updateStmt = conn.prepareStatement(newQuery)) {
							updateStmt.setInt(1, subscriberCode);
							updateStmt.setInt(2, confirmationCode);
							updateStmt.executeUpdate();
						}
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
	 * Checks whether there is a free parking space in the given parking lot,
	 * considering both currently occupied spots and future reservations.
	 * 
	 * If space is available:
	 * - A spot is selected that is NOT currently occupied and NOT reserved for a reservation starting now.
	 * - The spot is marked as occupied both in the `parkingSpaces` table and in the `parkingLot` counter.
	 * 
	 * @param parkingLotName the name of the parking lot (e.g., "Braude")
	 * @return the ID of the available parking space if one exists, or -1 if the lot is full
	 */
	public synchronized int hasAvailableSpots(String parkingLotName) {
		try {
			int totalSpots = getTotalSpots();
			int activeParkings = getActiveParkingsCount();
			int reservedNow = getActiveReservationsNowCount();

			int used = activeParkings + reservedNow;

			if (used >= totalSpots) {
				return -1; // No space available
			}

			int freeSpot = findUnreservedFreeParkingSpace();
			if (freeSpot == -1) {
				return -1; // No safe space found
			}

			addOccupiedParkingSpace();               // Increment counter
			updateParkingSpaceOccupied(freeSpot);    // Mark space as occupied

			return freeSpot;

		} catch (Exception e) {
			System.err.println("Error checking available spots: " + e.getMessage());
			return -1;
		}
	}

	/**
	 * Counts how many parking events are currently active (vehicles that are still inside).
	 *
	 * An event is considered active if it has no exit time yet.
	 *
	 * @return number of active parking events, or -1 if an error occurs
	 */
	private int getActiveParkingsCount() {
		String sql = "SELECT COUNT(*) FROM parkingEvent WHERE exitDate IS NULL";

		try (PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {
			return rs.next() ? rs.getInt(1) : 0;
		} catch (SQLException e) {
			System.err.println("Error counting active parking events: " + e.getMessage());
			return -1;
		}
	}


	/**
	 * Counts how many ACTIVE reservations exist for right now.
	 * A reservation is "active" if current time is within the 4-hour protected window.
	 *
	 * @return number of active reservations holding spots right now
	 */
	private int getActiveReservationsNowCount() {
		String sql = """
				    SELECT COUNT(*)
				    FROM `order`
				    WHERE `status` = 'ACTIVE'
				      AND order_date = CURDATE()
				      AND NOW() BETWEEN
				          TIMESTAMP(order_date, SUBTIME(arrival_time, '04:00:00')) AND
				          TIMESTAMP(order_date, ADDTIME(arrival_time, '00:15:00'))
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {
			return rs.next() ? rs.getInt(1) : 0;
		} catch (SQLException e) {
			System.err.println("Error counting active reservations: " + e.getMessage());
			return -1;
		}
	}


	/**
	 * Finds a free parking space that is not currently occupied and
	 * not blocked by a reservation window of [arrival - 4h, arrival + 15min].
	 *
	 * @return a free and unreserved parking space, or -1 if none found
	 */
	private int findUnreservedFreeParkingSpace() {
		String sql = """
				    SELECT ps.parking_space
				    FROM parkingSpaces ps
				    WHERE ps.is_occupied = FALSE
				      AND ps.parking_space NOT IN (
				          SELECT parking_space
				          FROM `order`
				          WHERE `status` = 'ACTIVE'
				            AND order_date = CURDATE()
				            AND NOW() BETWEEN
				                TIMESTAMP(order_date, SUBTIME(arrival_time, '04:00:00')) AND
				                TIMESTAMP(order_date, ADDTIME(arrival_time, '00:15:00'))
				      )
				    LIMIT 1
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {
			return rs.next() ? rs.getInt(1) : -1;
		} catch (SQLException e) {
			System.err.println("Error finding free parking space: " + e.getMessage());
			return -1;
		}
	}

	/**
	 * Inserts a new parking event into the 'parkingevent' table in the database.
	 *
	 * @param parkingEvent The ParkingEvent object containing all the event data to
	 *                     be stored.
	 */
	public void addParkingEvent(ParkingEvent parkingEvent) {
		String query = "INSERT INTO bpark.parkingEvent (subscriberCode, parking_space, entryDate, entryHour, exitDate, exitHour, wasExtended, vehicleId, NameParkingLot, parkingCode) VALUES "
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
		String query = "SELECT * FROM `order` WHERE subscriberCode=? AND TIMESTAMP(order_date, arrival_time) > NOW() + INTERVAL 15 MINUTE AND `status`='ACTIVE'";
		ArrayList<Order> orders = new ArrayList<>();
		int subsCode = subscriber.getSubscriberCode();
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setInt(1, subsCode);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					Order newOrder = new Order(rs.getInt("order_number"), rs.getInt("parking_space"),
							rs.getDate("order_date"), rs.getTime("arrival_time"), rs.getString("confirmation_code"),
							rs.getInt("subscriberCode"), rs.getDate("date_of_placing_an_order"), StatusOfOrder.ACTIVE);
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
		String query = "UPDATE `order` SET `status`='CANCELLED' WHERE order_number=?";
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
	 * Checks whether a subscriber with the given tag ID exists in the database.
	 *
	 * @param tag the tag ID to check (e.g., "TAG_001")
	 * @return true if the tag exists in the database; false otherwise
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
	 * Retrieves the subscriber code associated with the given tag ID.
	 *
	 * @param tag the tag ID to look up (e.g., "TAG_001")
	 * @return the subscriber code if found; -1 if not found or an error occurs
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
	 * Checks whether the subscriber currently has an active parking event,
	 * meaning they have entered the parking lot and not yet exited.
	 *
	 * @param codeInt the subscriber's unique code
	 * @return true if the subscriber has an open parking event (i.e., vehicle is inside); false otherwise
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
	 * Checks whether the vehicle associated with the given tag ID is currently
	 * inside the parking lot (i.e., has an active parking event with no recorded exit time).
	 *
	 * @param tag the tag ID associated with the vehicle (e.g., "TAG_001")
	 * @return true if the vehicle is currently inside the parking lot; false otherwise
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
	 * Returns all subscribers along with how many times each one was late.
	 *
	 * A late is counted if:
	 * - The parking duration was over 8 hours.
	 * - Or it was over 4 hours and was not extended.
	 *
	 * Ongoing and completed events are both included.
	 *
	 * @return List of Object[] where:
	 *         [0] = Subscriber
	 *         [1] = Number of late parkings (Integer)
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
	        FROM bpark.subscriber AS s
	        LEFT JOIN (
	            SELECT subscriberCode,
	                   COUNT(*) AS late_cnt
	            FROM bpark.parkingEvent
	            WHERE (
	                TIMESTAMPDIFF(MINUTE, TIMESTAMP(entryDate, entryHour), 
	                                   COALESCE(TIMESTAMP(exitDate, exitHour), NOW())) > 480
	                OR (
	                    wasExtended = FALSE AND
	                    TIMESTAMPDIFF(MINUTE, TIMESTAMP(entryDate, entryHour), 
	                                       COALESCE(TIMESTAMP(exitDate, exitHour), NOW())) > 240
	                )
	            )
	            GROUP BY subscriberCode
	        ) AS l USING (subscriberCode)
	        ORDER BY s.subscriberCode;
	        """;

	    List<Object[]> result = new ArrayList<>();

	    try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
	        while (rs.next()) {
	            Subscriber sub = new Subscriber(
	                rs.getInt("subscriberCode"),
	                rs.getString("userId"),
	                rs.getString("firstName"),
	                rs.getString("lastName"),
	                rs.getString("phoneNumber"),
	                rs.getString("email"),
	                rs.getString("username"),
	                rs.getString("tagId")
	            );

	            int lateCount = rs.getInt("late_count");
	            result.add(new Object[]{ sub, lateCount });
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
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
	 * Attempts to extend an active parking session based on the given parking code.
	 *
	 * The extension will succeed only if all of the following conditions are met:
	 * - The parking session is still active (exitDate and exitHour are NULL)
	 * - The session has not been extended before (wasExtended = FALSE)
	 * - If a subscriberCode is provided (not null or blank), the session must belong to that subscriber
	 * - There is no upcoming reservation in the next 4 hours if the parking lot is full
	 *
	 * If subscriberCode is null or blank (used from terminal), the extension will be attempted
	 * using only the parking code without checking subscriber ownership.
	 *
	 * @param parkingCode the code identifying the parking session
	 * @param subscriberCode the subscriber's code (may be null or blank if called from terminal)
	 * @return a ServerResponse indicating whether the extension was successful, with a message
	 */
	public ServerResponse extendParkingSession(int parkingCode, String subscriberCode) {
		String sql;
		boolean useSubscriberCode = (subscriberCode != null && !subscriberCode.isBlank());

		// Prevent extension if the parking lot is full and there is a reservation soon
		int totalSpots = getTotalSpots();
		int occupied = getOccupiedSpots();
		boolean upcomingReservation = hasReservationInNext4Hours();

		if (occupied >= totalSpots && upcomingReservation) {
			return new ServerResponse(false, null, ResponseType.PARKING_SESSION_EXTENDED,
					"Extension not allowed- spot is reserved.");
		}

		// Prepare SQL for extension
		if (!useSubscriberCode) {
			sql = "UPDATE bpark.parkingEvent " +
					"SET wasExtended = TRUE " +
					"WHERE parkingCode = ? " +
					"AND exitDate IS NULL AND exitHour IS NULL " +
					"AND wasExtended = FALSE";
		} else {
			sql = "UPDATE bpark.parkingEvent " +
					"SET wasExtended = TRUE " +
					"WHERE parkingCode = ? " +
					"AND subscriberCode = ? " +
					"AND exitDate IS NULL AND exitHour IS NULL " +
					"AND wasExtended = FALSE";
		}

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, parkingCode);
			if (useSubscriberCode) {
				stmt.setString(2, subscriberCode);
			}

			int rowsAffected = stmt.executeUpdate();

			if (rowsAffected > 0) {
				return new ServerResponse(true, null, ResponseType.PARKING_SESSION_EXTENDED, "Parking session extended successfully.");
			} 
			else {
				// if the number of rows is equal to 0, it means that there is some problem in the inputed code

				int subscriberCodeInt;

				if(useSubscriberCode) {
					// case 1: tell the subscriber that his vehicle isn't inside
					try {
						subscriberCodeInt = Integer.parseInt(subscriberCode);
						// If there was no exception thrown it means that the string contains only digits

					} catch (NumberFormatException e) {
						return new ServerResponse(false, null, ResponseType.PARKING_SESSION_EXTENDED, "Subscriber code has failed.");
					}

					if(!checkSubscriberEntered(subscriberCodeInt)) {
						return new ServerResponse(false, null, ResponseType.PARKING_SESSION_EXTENDED, "Your vehicle isn't inside.");
					}

					// case 2 : tell the subscriber that if his vehicle is inside but the parking code isn't correct
					// If the method returns null it means that the vehicle is inside but the parking code doesn't match
					if(getOpenParkingEvent(subscriberCodeInt, parkingCode) == null) {
						return new ServerResponse(false, null, ResponseType.PARKING_SESSION_EXTENDED, "The parking code doesn't match.");
					}

					// case 3 : tell the subscriber that if his vehicle is inside but he already made an extend then he can't make an extend again
					ParkingEvent event = getOpenParkingEvent(subscriberCodeInt, parkingCode);
					if (event.isWasExtended()) {
						return new ServerResponse(false, null, ResponseType.PARKING_SESSION_EXTENDED, "Your parking session was already extended.");
					}
				}
				else {
					System.out.println("HEREWEGO");
					// case 4 : if the subscriber has entered a parking code threw the terminal, but there is no matching parking code
					if(!openParkingCodeExists(parkingCode)) {
						return new ServerResponse(false, null, ResponseType.PARKING_SESSION_EXTENDED, "There is no active parking that matches this parking code.");
					}

					// case 5 : if there is a matched parking code but an extend parking already happened
					if(parkingCodeWasExtended(parkingCode)) {
						return new ServerResponse(false, null, ResponseType.PARKING_SESSION_EXTENDED, "Your parking session was already extended.");
					}
				}

			}
		}
		catch (SQLException e) {
			e.printStackTrace();
			return new ServerResponse(false, null, null, "Database error: " + e.getMessage());
		}

		// Default : return invalid code
		return new ServerResponse(false, null, ResponseType.PARKING_SESSION_EXTENDED, "Invalid parking code.");
	}

	/**
	 * Checks if there is an open parking event with the given parking code
	 * An open parking event means that exitDate and exitHour are NULL
	 *
	 * @param parkingCode the parking code to check
	 * @return true if an open parking event exists for the code, false otherwise
	 */
	private boolean openParkingCodeExists(int parkingCode) {
		String query = "SELECT 1 FROM parkingEvent WHERE parkingCode = ? AND exitDate IS NULL AND exitHour IS NULL LIMIT 1";
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setInt(1, parkingCode);
			ResultSet rs = stmt.executeQuery();
			return rs.next();
		} catch (SQLException e) {
			System.err.println("Error checking open parking code: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Checks if the parking event with the given parking code was already extended
	 * Only events that are still open (no exitDate/exitHour) are checked
	 *
	 * @param parkingCode the parking code to check
	 * @return true if the event was already extended, false otherwise
	 */
	private boolean parkingCodeWasExtended(int parkingCode) {
		String query = "SELECT wasExtended FROM parkingEvent WHERE parkingCode = ? AND exitDate IS NULL AND exitHour IS NULL LIMIT 1";
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setInt(1, parkingCode);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				return rs.getBoolean("wasExtended");
			}
		} catch (SQLException e) {
			System.err.println("Error checking if parking code was extended: " + e.getMessage());
		}
		return false;
	}


	/**
	 * Checks whether there's an upcoming reservation in the next 4 hours.
	 * This helps prevent extending if it might block someone else.
	 *
	 * @return true if there is a reservation coming soon
	 */
	public boolean hasReservationInNext4Hours() {
		String sql = """
				    SELECT 1
				    FROM `order`
				    WHERE `status` = 'ACTIVE'
				      AND order_date = CURDATE()
				      AND TIMESTAMP(order_date, arrival_time) BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL 4 HOUR)
				    LIMIT 1
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {
			return rs.next();
		} catch (SQLException e) {
			System.err.println("Error checking future reservations: " + e.getMessage());
			return false;
		}
	}



	/**
	 * Generates the next available subscriber code by retrieving the current maximum
	 * value from the database and adding 1. 
	 * If the table is empty (i.e., no subscribers exist), returns the default starting value 1011.
	 *
	 * @return the next available subscriber code
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
	 * Generates the next unique tag ID for a new subscriber in the format "TAG_XXX",
	 * where XXX is a zero-padded sequence number based on the current number of subscribers.
	 * For example: if there are 15 subscribers, returns "TAG_016".
	 *
	 * @return the newly generated tag ID; returns "TAG_999" if an error occurs
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
	 * Inserts a new vehicle and assigns it to a subscriber.
	 *
	 * @param vehicleId the vehicle ID (e.g. license plate)
	 * @param subscriberCode the subscriber this vehicle belongs to
	 * @return true if insertion succeeded, false otherwise
	 */
	public boolean insertVehicle(String vehicleId, int subscriberCode) {
		String sql = "INSERT INTO vehicle (vehicleId, subscriberCode) VALUES (?, ?)";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, vehicleId);
			stmt.setInt(2, subscriberCode);
			return stmt.executeUpdate() == 1;
		} catch (SQLException e) {
			System.err.println("Error inserting vehicle: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Checks whether a vehicle with the given ID already exists in the database.
	 *
	 * @param vehicleId the vehicle ID to check
	 * @return true if the vehicle already exists, false otherwise
	 */
	public boolean vehicleExists(String vehicleId) {
		String query = "SELECT 1 FROM vehicle WHERE vehicleId = ? LIMIT 1";
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setString(1, vehicleId);
			ResultSet rs = stmt.executeQuery();
			return rs.next();
		} catch (SQLException e) {
			System.err.println("Error checking vehicle existence: " + e.getMessage());
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
	 * Checks if a user ID (ID) already exists in the subscriber table.
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
				event.setLot(rs.getString("nameParkingLot"));
				event.setVehicleID(rs.getString("vehicleId"));
				event.setParkingCode(rs.getString("parkingCode"));
				LocalDateTime now = LocalDateTime.now();
				LocalDateTime dateTimeEntry = event.getEntryDate().atTime(event.getEntryHour());
				Duration duration = Duration.between(dateTimeEntry, now);
				//check if subscriber is lating according if he extended
				double timeOfParking=duration.toMinutes()/60.0;
				if ((event.isWasExtended() && timeOfParking>8) || ((!event.isWasExtended() )&& timeOfParking>4)) {
					list.add(event);
				} 
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
		System.out.println("year = " + year + ", month = " + month);
		int totalExtends = 0;
		int totalEntries = 0;
		int totalLates=0;
		int lessThanFourHours=0;
		int betweenFourToEight=0;
		int moreThanEight=0;
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setInt(1, year);
			stmt.setInt(2, month);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				totalEntries++;

				if (rs.getDate("exitDate") == null || rs.getTime("exitHour") == null) {
					continue;
				}
				LocalDateTime entryDateTime = LocalDateTime.of(rs.getDate("entryDate").toLocalDate(), rs.getTime("entryHour").toLocalTime());
				LocalDateTime exitDateTime = LocalDateTime.of(rs.getDate("exitDate").toLocalDate(), rs.getTime("exitHour").toLocalTime());

				Duration duration = Duration.between(entryDateTime, exitDateTime);

				long minutes=duration.toMinutes();
				double durationOfParking=minutes/60.0;


				if(durationOfParking <4) {
					lessThanFourHours++;
				}
				else if(durationOfParking>=4 && durationOfParking<8) {
					betweenFourToEight++;
				}
				else {
					moreThanEight++;
				}
				if (rs.getBoolean("wasExtended")) {
					totalExtends++;
					if (durationOfParking>8) {
						totalLates++;
					}
				}
				else {
					if (durationOfParking>4) {
						totalLates++;
					}
				}

			}
			return new ParkingReport(totalEntries, totalExtends, totalLates, lessThanFourHours, betweenFourToEight, moreThanEight);
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
		String query="INSERT INTO parkingReport(dateOfParkingReport, totalEntries, totalExtends, totalLates, lessThanFourHours, betweenFourToEight, moreThanEight) VALUES (?, ?, ?, ?, ?, ?, ?);";
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setDate(1, date);
			stmt.setInt(2, parkingReport.getTotalEntries());
			stmt.setInt(3, parkingReport.getTotalExtends());
			stmt.setInt(4, parkingReport.getTotalLates());
			stmt.setInt(5, parkingReport.getLessThanFour());
			stmt.setInt(6, parkingReport.getBetweenFourToEight());
			stmt.setInt(7, parkingReport.getMoreThanEight());
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
				return new ParkingReport(rs.getInt("totalEntries"), rs.getInt("totalExtends"), rs.getInt("totalLates"), rs.getInt("lessThanFourHours"), rs.getInt("betweenFourToEight"), rs.getInt("moreThanEight"));
			}
		} catch (SQLException e) {
			System.out.println("Error get parking report: "+ e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Returns a list of subscriber statistics (entries, extends, lates, hours)
	 * for the selected month and year – calculated directly from parkingEvent.
	 * This method does NOT store anything in the database, it's used for
	 * real-time calculation only (read-only).
	 *
	 * @param month calendar month (1 = January, ..., 12 = December)
	 * @param year  full year (e.g. 2025)
	 * @return list of subscriber rows with usage data
	 * @throws SQLException if something goes wrong during the query
	 */
	public List<SubscriberStatusReport> getSubscriberStatusLive(int month, int year)
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

		List<SubscriberStatusReport> rows = new ArrayList<>();

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, year);
			ps.setInt(2, month);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					rows.add(new SubscriberStatusReport(
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

	/**
	 * Generates and stores a fixed snapshot of all subscribers’ activity
	 * for a given month+year. This method:
	 * Fetches fresh data using getSubscriberStatusLive()
	 * Deletes any existing snapshot for that month (if already exists)
	 * Inserts a new set of records into subscriberStatusReport
	 * 
	 * Used by the monthly report generator (automatic or manual).
	 *
	 * @param month calendar month to save (1-12)
	 * @param year  year to save (4-digit)
	 * @return number of rows inserted (equals number of subscribers)
	 * @throws SQLException if the DB insert fails
	 */
	public int storeSubscriberStatusReport(int month, int year) throws SQLException {

		// 1. Build the list in memory
		List<SubscriberStatusReport> rows = getSubscriberStatusLive(month, year);

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

			for (SubscriberStatusReport r : rows) {
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
	 * Reads the saved subscriber snapshot from the database for a given month+year.
	 * This is the version used when the report was already generated and stored
	 * (instead of calculating it again).
	 *
	 * @param month month of the report (1-12)
	 * @param year  year of the report (e.g. 2025)
	 * @return list of subscriber statistics for that period (can be empty)
	 * @throws SQLException if database access fails
	 */
	public List<SubscriberStatusReport> getSubscriberStatusFromTable(int month, int year)
			throws SQLException {

		String sql =
				"SELECT subscriberCode, totalEntries, totalExtends, totalLates, totalHours, " +
						"       CONCAT(s.firstName,' ',s.lastName) AS fullName " +
						"FROM subscriberStatusReport r " +
						"JOIN subscriber s USING (subscriberCode) " +
						"WHERE reportMonth = ?";

		List<SubscriberStatusReport> rows = new ArrayList<>();

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setDate(1, java.sql.Date.valueOf(String.format("%04d-%02d-01", year, month)));
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					rows.add(new SubscriberStatusReport(
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

	/**
	 * Checks if there is already at least one row in subscriberStatusReport
	 * for the given month and year.
	 * Used to prevent creating the same report twice.
	 *
	 * @param month month to check (1-12)
	 * @param year  year to check (4-digit)
	 * @return true if snapshot exists, false otherwise
	 * @throws SQLException if DB check fails
	 */
	public boolean subscriberStatusReportExists(int month, int year) throws SQLException {

		String sql =
				"SELECT 1 FROM subscriberStatusReport " +
						"WHERE MONTH(reportMonth)=? AND YEAR(reportMonth)=? " +
						"LIMIT 1";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, month);
			ps.setInt(2, year);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next();     
			}
		}

	}

	/**
	 * Checks whether the given subscriber already has an active order
	 * for the specified date and arrival time.
	 *
	 * @param subscriberCode the subscriber's unique code
	 * @param selectedDate the date of the desired reservation
	 * @param timeOfArrival the time of the desired reservation
	 * @return true if an active order already exists for the same date and time; false otherwise
	 */
	public boolean checkIfOrderAlreadyExists(int subscriberCode, Date selectedDate, Time timeOfArrival) {
		String query = "SELECT * FROM bpark.order WHERE subscriberCode = ? AND order_date = ? AND arrival_time = ? AND `status`='ACTIVE'";

		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setInt(1, subscriberCode);
			stmt.setDate(2, selectedDate);
			stmt.setTime(3, timeOfArrival);

			ResultSet rs = stmt.executeQuery();
			return rs.next(); // if there's at least 1 result of that, it means that there is an order like that that's exists
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return false;
	}


	/**
	 * Retrieves all existing dates for which a parking report has been generated.
	 *
	 * @return a list of dates representing existing parking reports;
	 *         null if an error occurs during retrieval
	 */
	public ArrayList<Date> getAllReportsDates(){
		ArrayList<Date> datesOfReports=new ArrayList<>();
		String query="SELECT dateOfParkingReport FROM parkingReport";
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				datesOfReports.add(rs.getDate("dateOfParkingReport"));
			}
			return datesOfReports;
		} catch (SQLException e) {
			System.out.println("Error getting reports dates: "+e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * set inactive order after 15 minutes from arrival time
	 */
	public void inactiveReservations() {
		String query="UPDATE `order` SET `status`='INACTIVE' WHERE `status`='ACTIVE' AND TIMESTAMP(order_date, arrival_time) <= NOW() - INTERVAL 15 MINUTE;";
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			int rowsUpdated = stmt.executeUpdate();
		} catch (SQLException e) {
			System.out.println("Error inactive reservations: "+e.getMessage());
			e.printStackTrace();
		}
	}
}
