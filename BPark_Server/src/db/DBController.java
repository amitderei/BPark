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
import java.util.List;

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
			Class.forName("com.mysql.cj.jdbc.Driver");
			System.out.println("Driver definition succeed");
		} catch (Exception ex) {
			System.err.println("Driver definition failed"); // error in driver loading
		}

		try {
			// Connects to the local MySQL server (replace credentials as needed)
			conn = DriverManager.getConnection("jdbc:mysql://localhost/bpark?serverTimezone=Asia/Jerusalem", "root",
					"Aa123456");
			System.out.println("SQL connection succeed");
		} catch (SQLException ex) {
			// Prints SQL error information if connection fails
			System.err.println("SQLException: " + ex.getMessage());
			System.err.println("SQLState: " + ex.getSQLState());
			System.err.println("VendorError: " + ex.getErrorCode());
		}
	}

	/**
	 * Closes the connection to the database.
	 */
	public void disconnectFromDB() {
		try {
			conn.close(); // close JDBC connection
		} catch (Exception e) {
			System.err.println("Error to disconnect from DB " + e.getMessage()); // log if failed
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
		// Update the parking space assigned to a specific order number
		String query = "UPDATE `order` SET parking_space=? WHERE order_number=?";

		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			// set in the query
			stmt.setInt(1, newParkingSpace);
			stmt.setInt(2, orderNumber);

			// Execute the update and check if exactly one row was affected
			int updatedRows = stmt.executeUpdate();
			return updatedRows == 1;

		} catch (SQLException e) {
			// Print error message to the console for debugging
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
	 * @return a User object (username + role) if authentication succeeds, or null if it fails
	 */
	public User authenticateUser(String username, String password) {
		// Authenticate the user by checking exact (case-sensitive) match of username and password,
		// and return the user's role and login status
		String query = "SELECT role, is_logged_in " +
				"FROM   bpark.user " +
				"WHERE  BINARY username = ? AND BINARY password = ?";

		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			// set in the query
			stmt.setString(1, username);
			stmt.setString(2, password);

			try (ResultSet rs = stmt.executeQuery()) { //save the answer from query
				if (rs.next()) {
					boolean alreadyOnline = rs.getBoolean("is_logged_in");
					if (alreadyOnline) {
						// Someone is already logged in with this account
						return null;
					}

					// extract and parse the user's role
					String roleStr = rs.getString("role");
					UserRole role  = UserRole.valueOf(roleStr);

					// try to set the online flag atomically
					if (markUserLoggedIn(username)) {
						return new User(username, role);   // success
					}
				}
			}

		} catch (SQLException e) {
			System.err.println("[ERROR] authenticateUser: " + e.getMessage());
		}

		return null;   // login failed (wrong creds OR already online)
	}


	/**
	 * Checks if a subscriber with the given subscriberCode exists in the database.
	 *
	 * @param subscriberCode the code to check
	 * @return true if exists, false otherwise
	 */
	public boolean subscriberExists(int subscriberCode) {
		// Check if a subscriber with the given subscriber code exists
		String query = "SELECT 1 FROM subscriber WHERE subscriberCode = ? LIMIT 1";

		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setInt(1, subscriberCode);
			try (ResultSet rs = stmt.executeQuery()) {
				return rs.next(); // If we get a result, its mean subscriber exists
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
		// Retrieve the subscriber code for a specific (case-sensitive) tag ID
		String query = "SELECT subscriberCode FROM subscriber WHERE BINARY tagId = ?";

		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			// set in query
			stmt.setString(1, tagId);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) { // if tag found return associated subscriberCode
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
		// Get the most recent active parking event (no exitDate yet) for a given subscriber and parking code
		String query = "SELECT * FROM parkingEvent "
				+ "WHERE subscriberCode = ? AND parkingCode = ? AND exitDate IS NULL "
				+ "ORDER BY eventId DESC LIMIT 1";

		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			// set in query parametrs
			stmt.setInt(1, subscriberCode);
			stmt.setInt(2, parkingCode);

			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					// Extract exit date/time only if they exist
					LocalDate exitDate = rs.getDate("exitDate") != null ? rs.getDate("exitDate").toLocalDate() : null;
					LocalTime exitHour = rs.getTime("exitHour") != null ? rs.getTime("exitHour").toLocalTime() : null;

					// Build the ParkingEvent object from DB fields
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
	 * @return String with a matched message to the case that occurred
	 */
	public String handleVehiclePickup(int subscriberCode, int parkingCode) {
		ParkingEvent event;

		try {
			// Retrieve the active parking event for this subscriber and code
			event = getOpenParkingEvent(subscriberCode, parkingCode);
		} catch (SQLException e) {
			System.err.println("Error retrieving parking event: " + e.getMessage());
			return "An error occurred while retrieving your parking session.";
		}

		if (event == null) { // No active parking session found
			return "Parking code is incorrect.";
		}

		try {
			// Finalize the parking event: set exit time and release spot
			finalizeParkingEvent(event.getEventId());

			// Calculate time difference in hours
			LocalDateTime entryTime = LocalDateTime.of(event.getEntryDate(), event.getEntryHour());
			long hours = (System.currentTimeMillis()
					- entryTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()) / (1000 * 60 * 60);

			int allowedHours = event.isWasExtended() ? 8 : 4;

			if (hours > allowedHours) { //print to the subscriber
				System.out.println(
						"Subscriber " + subscriberCode + " had a delayed pickup (" + hours + " hours)");
				return "Pickup successful with delay. A notification was sent.";
			}
			System.out.println(
					"Subscriber " + subscriberCode + " had successful pickup (" + hours + " hours)");
			// Normal pickup within allowed time
			return "Vehicle pickup successful (" + hours + " hours).";

		} catch (SQLException e) {
			System.err.println("Failed to finalize parking event: " + e.getMessage());
			return "An error occurred while completing the pickup process.";
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
			// Set the subscriberCode parameter in the query
			stmt.setInt(1, subscriberCode);
			// Execute update on the most recent parking event of the subscriber
			int rowsAffected = stmt.executeUpdate();
			// Return true if exactly one row was updated
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

		try (
			// Prepare and execute the query
			PreparedStatement stmt = conn.prepareStatement(query);
			ResultSet rs = stmt.executeQuery()
			) {
			
			// Iterate over results and collect parking lot names
			while (rs.next()) {
				parkingLotArrayList.add(rs.getString("NameParkingLot"));
			}

		} catch (SQLException e) { // if fails
			System.err.println("Error retrieving parking lot names: " + e.getMessage());
		}

		return parkingLotArrayList; // Return the result list
	}

	/**
	 * Finds a free parking space available for the requested date and time,
	 * ensuring no overlap with existing active orders in the 4-hour window.
	 *
	 * @param time requested arrival time
	 * @param date requested reservation date
	 * @return parking space ID if available; -1 if none found
	 */
	private int getParkingSpace(Time time, Date date) {
		String query = "SELECT PS.parking_space FROM bpark.parkingspaces PS WHERE ps.parking_space NOT IN (SELECT O.parking_space FROM bpark.order O WHERE order_date=? AND arrival_time<DATE_ADD(TIMESTAMP(?, ?), INTERVAL 4 HOUR) AND"
				+ "                DATE_ADD(TIMESTAMP(O.order_date,O.arrival_time), INTERVAL 4 HOUR)>TIMESTAMP(?, ?)) LIMIT 1;";
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			// set the paramaters in the query
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
		} catch (SQLException e) { //if fails
			System.err.println("Error get a parking space" + e.getMessage());
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
		String query = "SELECT \r\n"
				+ "  ((SELECT SUM(totalSpots) FROM bpark.parkingLot) - COUNT(DISTINCT parking_space)) >= \r\n"
				+ "  (0.4 * (SELECT SUM(totalSpots) FROM bpark.parkingLot)) AS canOrder\r\n"
				+ "FROM (\r\n"
				+ "  SELECT parking_space,\r\n"
				+ "         TIMESTAMP(order_date, arrival_time) AS startTime,\r\n"
				+ "         DATE_ADD(TIMESTAMP(order_date, arrival_time), INTERVAL 4 HOUR) AS endTime,\r\n"
				+ "         status\r\n"
				+ "  FROM bpark.`order`\r\n"
				+ ") AS orders\r\n"
				+ "WHERE startTime < ? AND endTime > ? AND status = 'ACTIVE';";
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			// Construct the requested start and end timestamps
			Timestamp requestToStart = Timestamp.valueOf(date.toString() + " " + time.toString());
			Timestamp requestToEnd = new Timestamp(requestToStart.getTime() + 46060 * 1000);
			// Set time window for overlap check
			stmt.setTimestamp(1, requestToEnd);
			stmt.setTimestamp(2, requestToStart);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) { //return if 40% is availabe
					return rs.getBoolean("canOrder");
				}
			}
		} catch (SQLException e) {
			System.err.println("Error parking space checking " + e.getMessage());
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
			// set query parameters
			stmt.setDate(1, newOrder.getOrderDate());
			stmt.setTime(2, newOrder.getArrivalTime());
			stmt.setInt(3, newOrder.getParkingSpace());
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) { // set order number
					newOrder.setOrderNumber(rs.getInt(1));
				}
			}
		} catch (Exception e) {
			System.err.println("Error set order id " + e.getMessage());
		}
	}

	/**
	 * Places a new order and inserts it into the orders table in the database.
	 * Also automatically assigns an available parking space and sets the order ID if the insert succeeds.
	 *
	 * @param newOrder the Order object containing all the order details
	 * @return true if the order was successfully saved to the database, false if something failed
	 */
	public boolean placingAnNewOrder(Order newOrder) {
	    String query = "INSERT INTO `order` (parking_space, order_date, arrival_time ,confirmation_code, subscriberCode, date_of_placing_an_order, `status`) VALUES (?, ?, ?, ?, ?, ?, 'ACTIVE')";
	    
	    // Get an available parking space for the given time and date
	    int parking_space_id = getParkingSpace(newOrder.getArrivalTime(), newOrder.getOrderDate());
	    newOrder.setParkingSpace(parking_space_id);
	    
	    try (PreparedStatement stmt = conn.prepareStatement(query)) {
	        // Set parameters in query
	        stmt.setInt(1, newOrder.getParkingSpace());
	        stmt.setDate(2, newOrder.getOrderDate());
	        stmt.setTime(3, newOrder.getArrivalTime());
	        stmt.setString(4, newOrder.getConfirmationCode());
	        stmt.setInt(5, newOrder.getSubscriberId());
	        stmt.setDate(6, newOrder.getDateOfPlacingAnOrder());
	        
	        // Try to insert the order
	        int succeed = stmt.executeUpdate();
	        if (succeed > 0) {
	            // If successful, update the order ID from DB
	            setOrderId(newOrder);
	            return true;
	        } else {
	            return false;
	        }
	    } catch (Exception e) {
	        System.err.println("Error placing new order: " + e.getMessage());
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
			stmt.setString(1, user.getUsername()); //set parameter
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) { //if there is an subscriber - set the details
					Subscriber subscriber = new Subscriber(user.getUsername());
					subscriber.setSubscriberCode(rs.getInt(1));
					subscriber.setUserId(rs.getString(2));
					subscriber.setFirstName(rs.getString(3));
					subscriber.setLastName(rs.getString(4));
					subscriber.setPhoneNum(rs.getString(5));
					subscriber.setEmail(rs.getString(6));
					subscriber.setTagId(rs.getString(8));
					return subscriber; //return the founded subscriber
				}
			}
		} catch (Exception e) {
			System.err.println("Error get details of subscriber " + e.getMessage());
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
			stmt.setInt(1, subscriberCode); //set the parameters in query
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
			// set paramters in query
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
	 * Checks if there’s at least one free parking space in the given parking lot,
	 * taking into account both currently parked vehicles and future reservations.
	 *
	 * Here's how it works:
	 * - If a spot is available, it picks one that’s not currently occupied and not reserved for someone arriving now.
	 * - If the subscriber has a valid reservation starting now, reserved spots are also considered.
	 * - Once a spot is found, it gets marked as occupied both in the database and in the parking lot counters.
	 *
	 * @param parkingLotName the name of the parking lot (e.g., "Braude")
	 * @param subscriberCode the subscriber's code (used to check if they have a reservation)
	 * @param activeReservation the status of a subscriber whether he has an active reservation or not
	 * @return the ID of an available parking spot, or -1 if the lot is full
	 */
	public synchronized int hasAvailableSpots(String parkingLotName, int subscriberCode, boolean activeReservation) {
		try {
			int freeSpot;
			// If subscriber has reservation, find any free spot (even reserved)
			if (activeReservation) {
				freeSpot = findAnyFreeParkingSpace();
				if (freeSpot != -1) {
					addOccupiedParkingSpace();               // Increment occupied counter
					updateParkingSpaceOccupied(freeSpot);    // Mark spot as taken in DB
				}
				return freeSpot;
			}

			// Now we will focus on a subscriber which enters without an active order
			// Get the total number of parking spots across all lots
			int totalSpots = getTotalSpots();

			// Get the number of currently active (ongoing) parkings
			int activeParkings = getActiveParkingsCount();
			
			// Get the number of reservations that start right now
			int closeReservations = getCloseReservationsNowCount();
			System.out.println("amount of closest relevant reservations: " + closeReservations);

			// Calculate total used spots (parked + reserved)
			int used = activeParkings + closeReservations;

			// If all spots are used or exceeded, return -1
			if (used >= totalSpots) {
				System.out.println("1");
				return -1; // Lot is full for people who doesn't have a reservation
			}			
			freeSpot = findAnyFreeParkingSpace();
			
			if(freeSpot == -1) {
				System.out.println("2");
				return -1;
			}
			
			addOccupiedParkingSpace();               // Increment occupied counter
			updateParkingSpaceOccupied(freeSpot);    // Mark spot as taken in DB
			
			// Return selected parking spot ID if there is
			return freeSpot;

		} catch (Exception e) {
			// Log and return failure code
			System.err.println("Error checking available spots: " + e.getMessage());
			return -1;
		}
	}


	/**
	 * Finds any free parking space that is currently not occupied, ignoring reservation windows.
	 *
	 * @return a free parking space ID if found, or -1 if none found
	 */
	private int findAnyFreeParkingSpace() {
		String sql = """
				    SELECT ps.parking_space
				    FROM parkingSpaces ps
				    WHERE ps.is_occupied = FALSE
				    LIMIT 1
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {
			return rs.next() ? rs.getInt(1) : -1; // if there is answer from query, if not return -1
		} catch (SQLException e) {
			System.err.println("Error finding any free parking space: " + e.getMessage());
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
		// Query to count parking events where the vehicle hasn't exited yet
		String sql = "SELECT COUNT(*) FROM parkingEvent WHERE exitDate IS NULL";

		try (PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {
			// If result exists, return the count
			return rs.next() ? rs.getInt(1) : 0;
		} catch (SQLException e) {
			System.err.println("Error counting active parking events: " + e.getMessage());
			return -1;
		}
	}


	/**
	 * Counts how many ACTIVE reservations exist for the next 4 hours.
	 * A reservation is "close" if either starting within next 4h, or started but within +15min grace
	 *
	 * @return number of active reservations holding spots right now
	 */
	private int getCloseReservationsNowCount() {
	    // SQL to count today's ACTIVE reservations for the next 4 hours window
	    String sql = """
	            SELECT COUNT(*)
	            FROM `order`
	            WHERE `status` = 'ACTIVE'
	              AND order_date = CURDATE()
	              AND (
	                    TIMESTAMP(order_date, arrival_time) BETWEEN NOW() AND TIMESTAMPADD(HOUR, 4, NOW())
	                    OR
	                    NOW() BETWEEN TIMESTAMP(order_date, arrival_time)
	                              AND TIMESTAMPADD(MINUTE, 15, TIMESTAMP(order_date, arrival_time))
	                  )
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
	 * Inserts a new parking event into the 'parkingevent' table in the database.
	 *
	 * @param parkingEvent The ParkingEvent object containing all the event data to
	 *                     be stored.
	 */
	public void addParkingEvent(ParkingEvent parkingEvent) {
		// SQL query to insert a new row into the parkingEvent table
		String query = "INSERT INTO bpark.parkingEvent (subscriberCode, parking_space, entryDate, entryHour, exitDate, exitHour, wasExtended, vehicleId, NameParkingLot, parkingCode) VALUES "
				+ "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			// Set each parameter in the prepared statement from the ParkingEvent object
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

			// Execute the insert statement
			stmt.executeUpdate();

		} catch (SQLException e) {
			System.err.println("Error inserting parking event: " + e.getMessage());
		}
	}

	/**
	 * Increments the number of occupied parking spots in the 'parkinglot' table by
	 * 1.
	 */
	private void addOccupiedParkingSpace() {
		// SQL query to increment the occupiedSpots for the Braude lot
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
			stmt.setInt(1, parkingSpace); // Set the parking space ID in the query
			stmt.executeUpdate();// Execute the update
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
		// SQL query to get vehicleId by subscriberCode
		String query = "SELECT vehicleId FROM vehicle WHERE subscriberCode = ?";

		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			// Set the subscriber code in the query
			stmt.setInt(1, subscriberCode);
			// Execute the query
			ResultSet rs = stmt.executeQuery();

			// If result exists, return the vehicle ID
			if (rs.next()) {
				return rs.getString("vehicleId");
			}
		} catch (SQLException e) {
			System.err.println("Error finding vehicle ID: " + e.getMessage());
		}

		return null; // Vehicle ID matching to this subscriber not found
	}

	/**
	 * Gets all active future reservations for a given subscriber.
	 * Only returns orders that are scheduled to start more than 15 minutes from now.
	 *
	 * @param subscriber the subscriber whose reservations we want to check
	 * @return an ArrayList of the subscriber's upcoming reservations, or null if an error occurs
	 */
	public ArrayList<Order> getFutureReservationsForSubscriber(Subscriber subscriber) {
	    // SQL to select all active future reservations of the subscriber
	    String query = "SELECT * FROM `order` WHERE subscriberCode=? AND TIMESTAMP(order_date, arrival_time) > NOW() + INTERVAL 15 MINUTE AND `status`='ACTIVE'";
	    ArrayList<Order> orders = new ArrayList<>();
	    int subsCode = subscriber.getSubscriberCode();
	    try (PreparedStatement stmt = conn.prepareStatement(query)) {
	        // Set subscriber code in the query
	        stmt.setInt(1, subsCode);
	        try (ResultSet rs = stmt.executeQuery()) {
	            while (rs.next()) {
	                // Build an Order object from result set
	                Order newOrder = new Order(rs.getInt("order_number"), rs.getInt("parking_space"),
	                        rs.getDate("order_date"), rs.getTime("arrival_time"), rs.getString("confirmation_code"),
	                        rs.getInt("subscriberCode"), rs.getDate("date_of_placing_an_order"), StatusOfOrder.ACTIVE);
	                orders.add(newOrder); // Add to the result list
	            }
	            return orders; // Return all matched orders
	        }
	    } catch (SQLException e) {
	        System.err.println("Error finding reservations: " + e.getMessage());
	    }
	    return null; // Return null in case of failure
	}


	/**
	 * Cancels an order by updating its status to 'CANCELLED' in the database.
	 *
	 * @param orderNumber the unique ID of the order to cancel
	 * @return true if the update was successful (order was found and cancelled), false otherwise
	 */
	public boolean deleteOrder(int orderNumber) {
	    // SQL query to cancel the order by setting its status
	    String query = "UPDATE `order` SET `status`='CANCELLED' WHERE order_number=?";
	    try (PreparedStatement stmt = conn.prepareStatement(query)) {
	        // Set the order number in the query
	        stmt.setInt(1, orderNumber);
	        // Execute the update and check if any rows were affected
	        int ifDelete = stmt.executeUpdate();
	        return (ifDelete > 0); // return true if successful
	    } catch (SQLException e) {
	        System.err.println("Error deleting order: " + e.getMessage());
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
		// SQL query to update the user's password
		String query = "UPDATE user SET password=? WHERE username=?";
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			// Set the new password and username in the query
			stmt.setString(1, user.getPassword());
			stmt.setString(2, user.getUsername());
			// Execute the update and check if a row was affected
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
		// SQL query to update subscriber's personal information
		String query = "UPDATE subscriber SET firstName=?, lastName=?, phoneNumber=?,  email=? WHERE subscriberCode=?";
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			// Set updated values in the query
			stmt.setString(1, subscriber.getFirstName());
			stmt.setString(2, subscriber.getLastName());
			stmt.setString(3, subscriber.getPhoneNum());
			stmt.setString(4, subscriber.getEmail());
			stmt.setInt(5, subscriber.getSubscriberCode());
			
			// Execute the update and return success status
			int rowsUpdated = stmt.executeUpdate();
			return rowsUpdated > 0;
		} catch (SQLException e) {
			System.err.println("Error update subscriber: " + e.getMessage());
			return false;
		}
	}
	
	/**
	 * Checks if another subscriber is already using this phone number.
	 *
	 * @param phone   the phone number to check
	 * @param subCode the current subscriber's code (to exclude themself)
	 * @return true if another subscriber has this phone number, false otherwise
	 */
	public boolean duplicatePhone(String phone, int subCode) {
	    String query = "SELECT * FROM subscriber WHERE phoneNumber=? AND subscriberCode<>?";

	    try (PreparedStatement stmt = conn.prepareStatement(query)) {
	        stmt.setString(1, phone);
	        stmt.setInt(2, subCode);

	        try (ResultSet rs = stmt.executeQuery()) {
	            return rs.next(); // someone else is already using this number
	        }

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return false;
	}

	
	/**
	 * Checks if another subscriber already uses this email.
	 *
	 * @param email the email to check
	 * @param subCode the subscriber's own code (so we can ignore their own record)
	 * @return true if another subscriber has the same email, false otherwise
	 */
	public boolean duplicateEmail(String email, int subCode) {
	    String query = "SELECT * FROM subscriber WHERE email=? AND subscriberCode<>?";

	    try (PreparedStatement stmt = conn.prepareStatement(query)) {
	        stmt.setString(1, email);
	        stmt.setInt(2, subCode);

	        try (ResultSet rs = stmt.executeQuery()) {
	            return rs.next(); // if we got a result, someone else has that email
	        }

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return false;
	}


	/**
	 * Retrieves the full parking history of a given subscriber.
	 *
	 * @param subscriber the subscriber whose parking events are requested
	 * @return a list of ParkingEvent objects representing the subscriber's parking
	 *         history, or null if an error occurs during retrieval
	 */
	public ArrayList<ParkingEvent> parkingHistoryOfSubscriber(Subscriber subscriber) {
		// SQL query to select all parking events for the subscriber
		String query = "SELECT * FROM parkingEvent WHERE subscriberCode=?";
		ArrayList<ParkingEvent> historyOfParking = new ArrayList<>();
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			// Set subscriber code in the query
			stmt.setInt(1, subscriber.getSubscriberCode());
			// Execute the query
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					// Create a new ParkingEvent from the result
					ParkingEvent newParkingEvent = new ParkingEvent();
					newParkingEvent.setEventId(rs.getInt("eventId"));
					newParkingEvent.setSubscriberCode(rs.getInt("subscriberCode"));
					newParkingEvent.setParkingSpace(rs.getInt("parking_space"));
					newParkingEvent.setEntryDate((rs.getDate("entryDate")).toLocalDate());
					newParkingEvent.setEntryTime((rs.getTime("entryHour")).toLocalTime());
					// Handle nullable exit date and time
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
				return historyOfParking; // Add to the result list
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
			exists = rs.next(); // If the tag has been found (there is row) it means that he will exists
			// Close resources
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
		// query to find subscriberCode by tagId
		String query = "SELECT s.subscriberCode FROM bpark.subscriber s WHERE s.tagId = ?";

		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setString(1, tag); // Set the tag value in the query
			ResultSet rs = stmt.executeQuery();// Execute the query

			if (rs.next()) { 
				return rs.getInt("subscriberCode"); // Return the subscriber code if found
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
		// query to check if there's an active parking event (no exitHour) for the subscriber
		String query = "SELECT * FROM bpark.parkingevent WHERE subscriberCode = ? AND exitHour IS NULL";

		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setInt(1, codeInt); // Set the subscriber code in the query
			ResultSet rs = stmt.executeQuery(); // Execute the query and return true if a row is found
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
		// query to check for active parking event by tag ID
		String query = "SELECT pe.* FROM bpark.parkingevent pe "
				+ "JOIN bpark.subscriber s ON pe.subscriberCode = s.subscriberCode "
				+ "WHERE s.tagId = ? AND pe.exitHour IS NULL";

		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setString(1, tag); // set the ID tag string in the parameter in query
			ResultSet rs = stmt.executeQuery(); //execute and find the row result
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
		// query to get phone and email by subscriber code
		String query = "SELECT phoneNumber, email FROM subscriber WHERE subscriberCode=?";
		String[] arrayForPhoneAndEmail = new String[2];
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			// subscriber code in the query
			stmt.setInt(1, subscriberCode);
			ResultSet rs = stmt.executeQuery(); //exucute
			// If a result is found, extract email and phone number
			if (rs.next()) {
				arrayForPhoneAndEmail[0] = rs.getString("email");         // email in index 0
				arrayForPhoneAndEmail[1] = rs.getString("phoneNumber");   // phone number in index 1
				return arrayForPhoneAndEmail;
			}
		} catch (SQLException e) {
			System.err.println("Error finding email and phone number: " + e.getMessage());
		}
		return null; // Return null if subscriber not found or error occurred
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
		// query with LEFT JOIN to count late parking events per subscriber
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
			while (rs.next()) { // Process each row in the result set
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

				// Get number of late pickups
				int lateCount = rs.getInt("late_count");
				result.add(new Object[]{ sub, lateCount }); // Add both subscriber and count to result list
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result; // Return list of subscriber + late count pairs
	}

	/**
	 * Retrieves all active (open) parking events from the database. Active events
	 * are those that have no recorded exit date/time.
	 *
	 * @return a list of ParkingEvent objects
	 */
	public List<ParkingEvent> getActiveParkingEvents() {
		List<ParkingEvent> list = new ArrayList<>();
		// query to select active parking events (no exit time)
		String sql = "SELECT * FROM parkingEvent WHERE exitDate IS NULL AND exitHour IS NULL";

		try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

			while (rs.next()) { // Process each active parking event from result set
				ParkingEvent event = new ParkingEvent();
				// Populate ParkingEvent fields from result row
				event.setEventId(rs.getInt("eventId"));
				event.setSubscriberCode(rs.getInt("subscriberCode"));
				event.setParkingSpace(rs.getInt("parking_space"));
				event.setEntryDate(rs.getDate("entryDate").toLocalDate());
				event.setEntryTime(rs.getTime("entryHour").toLocalTime());
				event.setWasExtended(rs.getBoolean("wasExtended"));
				event.setLot(rs.getString("nameParkingLot"));
				event.setVehicleID(rs.getString("vehicleId"));
				event.setParkingCode(rs.getString("parkingCode"));

				list.add(event); // Add event to result list
			}

		} catch (SQLException e) {
			System.err.println("Error retrieving active parking events: " + e.getMessage());
		}
		// Return list of active events
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
		// query to get an active parking event by subscriber code
		String query = "SELECT * FROM parkingEvent WHERE subscriberCode=? AND exitHour IS NULL";
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			// Set subscriber code in the query
			stmt.setInt(1, subscriber.getSubscriberCode());
			ResultSet rs = stmt.executeQuery(); //execute
			// If an active parking event is found, create and populate the object
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
			System.err.println("Error finding active parking info: " + e.getMessage());
		}
		return null; // Return null if no active event found or error occurred
	}

	/**
	 * Tries to extend a parking session using a given parking code and (optionally) a subscriber code.
	 *
	 * Extension is only allowed if:
	 * - The session is still active (not exited yet)
	 * - It hasn't been extended already
	 * - If a subscriberCode is given, the session must belong to that subscriber
	 * - There's still extension capacity left in the system (based on future reservations)
	 *
	 * If subscriberCode is missing or blank, we assume the request comes from a terminal
	 * and skip subscriber validation.
	 *
	 * @param parkingCode the unique code for the current parking session
	 * @param subscriberCode optional code that identifies the subscriber (may be null/blank)
	 * @return a string message indicating success or the specific reason why the extension failed
	 * @throws SQLException if there's a problem talking to the database
	 */
	public String extendParkingSession(int parkingCode, String subscriberCode) throws SQLException {
	    String sql;
	    boolean useSubscriberCode = (subscriberCode != null && !subscriberCode.isBlank());

	    // First: Check that this session is actually valid and eligible for extension
	    if (useSubscriberCode) {
	        // Try parsing the subscriber code (should be a number)
	        int subscriberCodeInt;
	        try {
	            subscriberCodeInt = Integer.parseInt(subscriberCode);
	        } catch (NumberFormatException e) {
	            return "Subscriber code has failed.";
	        }

	        // Make sure the subscriber's vehicle is currently parked
	        if (!checkSubscriberEntered(subscriberCodeInt)) {
	            return "Your vehicle isn't inside.";
	        }

	        // Get the parking event and verify ownership and state
	        ParkingEvent event = getOpenParkingEvent(subscriberCodeInt, parkingCode);
	        if (event == null) {
	            return "The parking code doesn't match your active parking session.";
	        }

	        if (event.isWasExtended()) {
	            return "Your parking session was already extended.";
	        }

	        if (subscriberIsLate(subscriberCodeInt)) {
	            return "Your parking session is late.";
	        }

	    } else {
	        // Terminal scenario – no subscriber code, so we only verify the session
	        if (!openParkingCodeExists(parkingCode)) {
	            return "There is no active parking that matches this parking code.";
	        }

	        if (parkingCodeWasExtended(parkingCode)) {
	            return "Your parking session was already extended.";
	        }

	        if (subscriberIsLateByParkingCode(parkingCode)) {
	            return "Your parking session is late.";
	        }
	    }

	    // Second: Check if allowing another extension would interfere with upcoming reservations
	    updateRemainingExtensionCapacity("Braude");
	    if (extensionWouldBlockReservation()) {
	        return "Extension denied – upcoming reservations exceed available capacity.";
	    }

	    // Build the SQL query based on whether subscriber validation is required
	    if (!useSubscriberCode) {
	        sql = "UPDATE bpark.parkingEvent SET wasExtended = TRUE " +
	              "WHERE parkingCode = ? AND exitDate IS NULL AND exitHour IS NULL " +
	              "AND wasExtended = FALSE AND (TIMESTAMPDIFF(MINUTE, TIMESTAMP(entryDate, entryHour), NOW()) <= 240)";
	    } else {
	        sql = "UPDATE bpark.parkingEvent SET wasExtended = TRUE " +
	              "WHERE parkingCode = ? AND subscriberCode = ? AND exitDate IS NULL AND exitHour IS NULL " +
	              "AND wasExtended = FALSE AND (TIMESTAMPDIFF(MINUTE, TIMESTAMP(entryDate, entryHour), NOW()) <= 240)";
	    }

	    // Try updating the session in the database to mark it as extended
	    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
	        stmt.setInt(1, parkingCode);
	        if (useSubscriberCode) {
	            stmt.setString(2, subscriberCode);
	        }

	        int rowsAffected = stmt.executeUpdate();

	        // If the update succeeded, that means the session was extended successfully
	        if (rowsAffected > 0) {
	            decrementRemainingExtensions("Braude"); // update capacity count
	            return "Parking session extended successfully.";
	        }
	    } catch (SQLException e) {
	        // In case something goes wrong while talking to the DB
	        e.printStackTrace();
	        return "Database error: " + e.getMessage();
	    }

	    // Fallback message (shouldn't usually get here)
	    return "Invalid parking code.";
	}



	/**
	 * Decreases the number of remaining extensions for the lot (if more than 0).
	 * @param lotName the name of the parking lot
	 */
	public void decrementRemainingExtensions(String lotName) {
	    String sql = """
	        UPDATE bpark.extensionCapacity
	        SET remainingExtensions = remainingExtensions - 1
	        WHERE lotName = ? AND remainingExtensions > 0
	    """;

	    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
	        stmt.setString(1, lotName);
	        stmt.executeUpdate();
	    } catch (SQLException e) {
	        System.err.println("Failed to decrement remainingExtensions: " + e.getMessage());
	    }
	}

	/**
	 * Checks if no further extensions should be allowed due to upcoming reservations.
	 * This is a conservative check - it blocks if only one extension slot remains,
	 * assuming the current request would consume it and leave no room for reservations.
	 *
	 * @return true if extensions should be blocked, false if there is still capacity
	 */
	public boolean extensionWouldBlockReservation() {
	    String sql = "SELECT remainingExtensions FROM bpark.extensionCapacity WHERE lotName = 'Braude'";

	    try (PreparedStatement stmt = conn.prepareStatement(sql);
	         ResultSet rs = stmt.executeQuery()) {

	        if (rs.next()) {
	            int remaining = rs.getInt("remainingExtensions");
	            return remaining < 1; // block now to preserve space for reservation
	        }
	    } catch (SQLException e) {
	        System.err.println("Error in extensionWouldBlockReservation: " + e.getMessage());
	    }

	    return true; // safer to block in case of DB error
	}


	/**
	 * Updates the extension capacity in the DB based on current lot status.
	 * This method recalculates how many users can still extend their parking.
	 *
	 * @param lotName the name of the parking lot
	 */
	public void updateRemainingExtensionCapacity(String lotName) {
	    try {
	        int totalSpots = getTotalSpots(lotName);
	        int reservations = getUpcomingReservationCount(lotName);
	        int alreadyExtended = getExtendedParkingsCount(lotName);

	        int allowedExtensions = totalSpots - reservations - alreadyExtended - 1;
	        if (allowedExtensions < 0) allowedExtensions = 0;

	        // Make sure the row exists (for first-time insert)
	        PreparedStatement insertStmt = conn.prepareStatement(
	            "INSERT IGNORE INTO bpark.extensionCapacity (lotName, remainingExtensions) VALUES (?, ?)"
	        );
	        insertStmt.setString(1, lotName);
	        insertStmt.setInt(2, allowedExtensions);
	        insertStmt.executeUpdate();

	        // Then update the value to reflect real-time state
	        PreparedStatement updateStmt = conn.prepareStatement(
	            "UPDATE bpark.extensionCapacity SET remainingExtensions = ? WHERE lotName = ?"
	        );
	        updateStmt.setInt(1, allowedExtensions);
	        updateStmt.setString(2, lotName);
	        updateStmt.executeUpdate();

	    } catch (SQLException e) {
	        System.err.println("Failed to update extension capacity: " + e.getMessage());
	    }
	}

	/**
	 * Gets the total number of parking spots in a lot.
	 * @param lotName the lot name
	 * @return number of total spots, or 0 if error
	 */
	public int getTotalSpots(String lotName) {
	    String sql = "SELECT totalSpots FROM bpark.parkingLot WHERE NameParkingLot = ?";
	    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
	        stmt.setString(1, lotName);
	        ResultSet rs = stmt.executeQuery();
	        if (rs.next()) return rs.getInt("totalSpots");
	    } catch (SQLException e) {
	        System.err.println("Error in getTotalSpots: " + e.getMessage());
	    }
	    return 0;
	}

	/**
	 * Counts how many active orders are scheduled to arrive in the next 4 hours.
	 * These affect the limit on who can extend parking.
	 *
	 * @param lotName the lot name
	 * @return number of upcoming reservations
	 */
	public int getUpcomingReservationCount(String lotName) {
	    String sql = """
	        SELECT COUNT(*) AS cnt
	        FROM bpark.`order` o
	        JOIN bpark.parkingSpaces ps ON o.parking_space = ps.parking_space
	        WHERE o.status = 'ACTIVE'
	          AND TIMESTAMP(o.order_date, o.arrival_time) BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL 4 HOUR)
	    """;

	    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
	        ResultSet rs = stmt.executeQuery();
	        if (rs.next()) return rs.getInt("cnt");
	    } catch (SQLException e) {
	        System.err.println("Error in getUpcomingReservationCount: " + e.getMessage());
	    }
	    return 0;
	}

	/**
	 * Returns how many active sessions in the lot have already been extended.
	 * @param lotName the lot name
	 * @return number of extended (and still active) sessions
	 */
	public int getExtendedParkingsCount(String lotName) {
	    String sql = """
	        SELECT COUNT(*) AS cnt
	        FROM bpark.parkingEvent
	        WHERE wasExtended = TRUE
	          AND exitDate IS NULL
	          AND NameParkingLot = ?
	    """;

	    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
	        stmt.setString(1, lotName);
	        ResultSet rs = stmt.executeQuery();
	        if (rs.next()) return rs.getInt("cnt");
	    } catch (SQLException e) {
	        System.err.println("Error in getExtendedParkingsCount: " + e.getMessage());
	    }
	    return 0;
	}

	/**
	 * Checks if the subscriber is late based on parking time and extension status.
	 * The check is accurate to the minute: if more than 240 minutes (4 hours) have passed,
	 * the subscriber is considered late.
	 *
	 * @param subscriberCode the subscriber's code
	 * @return true if the subscriber is late, false otherwise
	 */
	public boolean subscriberIsLate(int subscriberCode) {
		// Create a temporary Subscriber object with the given subscriber code
		Subscriber s = new Subscriber();
		s.setSubscriberCode(subscriberCode);
		
		// Retrieve the currently active parking event for this subscriber
		ParkingEvent event = getActiveParkingEvent(s);
		
		// If no active parking event, subscriber can't be late
		if (event == null) {
			return false; // No active parking, can't be late
		}

		// Calculate how many minutes have passed since the parking started
		LocalDateTime entryTime = LocalDateTime.of(event.getEntryDate(), event.getEntryHour());
		LocalDateTime now = LocalDateTime.now();
		long minutes = Duration.between(entryTime, now).toMinutes();

		// If more than 240 minutes (4 hours) have passed → subscriber is late
		return minutes > 240;
	}
	
	/**
	 * Checks if the parking session associated with the given parking code is late.
	 * The check is accurate to the minute: if more than 240 minutes (4 hours) have passed,
	 * the parking session is considered late.
	 *
	 * @param parkingCode the code identifying the parking session
	 * @return true if the parking session is late, false otherwise
	 */
	private boolean subscriberIsLateByParkingCode(int parkingCode) {
		// query to find an active parking event by parking code (not yet exited)
		String query = "SELECT * FROM parkingEvent WHERE parkingCode = ? AND exitDate IS NULL AND exitHour IS NULL";

		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			// Set the parking code in the query
			stmt.setInt(1, parkingCode);
			ResultSet rs = stmt.executeQuery();
			
			// If a matching active event is found
			if (rs.next()) {
				// Extract entry date and hour
				LocalDate entryDate = rs.getDate("entryDate").toLocalDate();
				LocalTime entryHour = rs.getTime("entryHour").toLocalTime();
				// Calculate time passed since entry
				LocalDateTime entryTime = LocalDateTime.of(entryDate, entryHour);
				LocalDateTime now = LocalDateTime.now();

				long minutes = Duration.between(entryTime, now).toMinutes();
				
				// Return true if parking duration exceeds 240 minutes (4 hours)
				return minutes > 240;
			}
		} catch (SQLException e) {
			System.err.println("Error checking if parking code is late: " + e.getMessage());
		}
		// Return false if no match or error occurred
		return false;
	}
	
	/**
	 * Checks if there is an open parking event with the given parking code
	 * An open parking event means that exitDate and exitHour are NULL
	 *
	 * @param parkingCode the parking code to check
	 * @return true if an open parking event exists for the code, false otherwise
	 */
	private boolean openParkingCodeExists(int parkingCode) {
		// query to check if there is an active parking event with the given parking code
		String query = "SELECT 1 FROM parkingEvent WHERE parkingCode = ? AND exitDate IS NULL AND exitHour IS NULL LIMIT 1";
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setInt(1, parkingCode); 	// Set the parking code in the query
			ResultSet rs = stmt.executeQuery(); //execute
			return rs.next(); // Return true if a matching active event was found
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
		// Query to check if the parking session with the given code was already extended
		String query = "SELECT wasExtended FROM parkingEvent WHERE parkingCode = ? AND exitDate IS NULL AND exitHour IS NULL LIMIT 1";
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setInt(1, parkingCode); // Set the parking code parameter
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) { // If a record is found, return the 'wasExtended' flag
				return rs.getBoolean("wasExtended");
			}
		} catch (SQLException e) {
			System.err.println("Error checking if parking code was extended: " + e.getMessage());
		}
		return false; // Return false if no record found or error occurred
	}


	/**
	 * Checks whether there's an upcoming reservation in the next 4 hours.
	 * This helps prevent extending if it might block someone else.
	 *
	 * @return true if there is a reservation coming soon
	 */
	public boolean hasReservationInNext4Hours() {
		// query to check if there's any active reservation starting within the next 4 hours
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
			return rs.next(); // If any result is returned, there is a reservation in the next 4 hours
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
		// query get the highest subscriberCode currently in the table
		String query = "SELECT MAX(subscriberCode) FROM bpark.subscriber";
		try (PreparedStatement stmt = conn.prepareStatement(query); ResultSet rs = stmt.executeQuery()) {
			if (rs.next()) { // If table is empty (max = 0), start from 1011. Otherwise, return next available code.
				int currentMax = rs.getInt(1);
				return (currentMax == 0 ? 1011 : currentMax + 1);
			}
		} catch (SQLException e) {
			System.err.println("Error generating subscriber code: " + e.getMessage());
		}
		return 1011; // // Default return value in case of failure
	}

	/**
	 * Generates the next unique tag ID for a new subscriber in the format "TAG_XXX",
	 * where XXX is a zero-padded sequence number based on the current number of subscribers.
	 * For example: if there are 15 subscribers, returns "TAG_016".
	 *
	 * @return the newly generated tag ID; returns "TAG_999" if an error occurs
	 */
	public String generateNextTagId() {
		// Query to count how many subscribers exist in the database
		String query = "SELECT COUNT(*) FROM bpark.subscriber";
		try (PreparedStatement stmt = conn.prepareStatement(query); ResultSet rs = stmt.executeQuery()) {
			if (rs.next()) { 
				// Generate new tag ID based on the count (e.g., TAG_001, TAG_045, etc.)
				int count = rs.getInt(1) + 1;
				return "TAG_" + String.format("%03d", count);
			}
		} catch (SQLException e) {
			System.err.println("Error generating tag ID: " + e.getMessage());
		}
		return "TAG_999"; 	// Fallback tag ID in case of failure
	}

	/**
	 * Inserts a new subscriber into the database.
	 *
	 * @param s the subscriber object to insert
	 * @return true if insertion succeeded
	 */
	public boolean insertSubscriber(Subscriber s) {
		//query to insert a new subscriber into the database
		String sql = "INSERT INTO bpark.subscriber "
				+ "(subscriberCode, userId, firstName, lastName, phoneNumber, email, username, tagId) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			// Set values from the Subscriber object into the prepared statement
			stmt.setInt(1, s.getSubscriberCode());
			stmt.setString(2, s.getUserId());
			stmt.setString(3, s.getFirstName());
			stmt.setString(4, s.getLastName());
			stmt.setString(5, s.getPhoneNum());
			stmt.setString(6, s.getEmail());
			stmt.setString(7, s.getUsername());
			stmt.setString(8, s.getTagId());
			return stmt.executeUpdate() == 1; // Execute the insert query and return true if one row was inserted
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
		// query to insert a new user into the 'user' table
		String sql = "INSERT INTO bpark.user (username, password, role) VALUES (?, ?, ?)";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			// Set username, password, and role values from the User object
			stmt.setString(1, user.getUsername());
			stmt.setString(2, user.getPassword());
			stmt.setString(3, user.getRole().toString());
			return stmt.executeUpdate() == 1; 	// Execute the insert and return true if exactly one row was inserted
		} catch (SQLException e) {
			return false; // Return false if an error occurred (no logging here)
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
		// query to insert a new vehicle associated with a subscriber
		String sql = "INSERT INTO vehicle (vehicleId, subscriberCode) VALUES (?, ?)";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			// Set vehicle ID and subscriber code
			stmt.setString(1, vehicleId);
			stmt.setInt(2, subscriberCode);
			return stmt.executeUpdate() == 1; 	// Execute the insert and return true if exactly one row was inserted
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
		// query to check if a vehicle with the given ID exists
		String query = "SELECT 1 FROM vehicle WHERE vehicleId = ? LIMIT 1";
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setString(1, vehicleId); // set the parameter in the query
			ResultSet rs = stmt.executeQuery(); // execture
			return rs.next(); // if there is next
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
		// query to check if an email already exists in the subscriber table
		String query = "SELECT 1 FROM bpark.subscriber WHERE email = ? LIMIT 1";
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setString(1, email); // set to the paramter in the query
			ResultSet rs = stmt.executeQuery();
			return rs.next(); // true if exists
		} catch (SQLException e) {
			System.err.println("Error checking email existence: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Checks whether a **case-sensitive** username already exists.
	 *
	 * @param username exact username to check
	 * @return true if username is taken
	 */
	public boolean usernameExists(String username) {
		// query to check if a username exists (case-sensitive due to BINARY)
		final String sql = """
				SELECT 1
				FROM   bpark.user
				WHERE  BINARY username = ?
				LIMIT  1
				""";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, username); //set the parameter to query
			return ps.executeQuery().next(); // Execute the query and return true if a match is found
		} catch (SQLException e) {
			System.err.println("[DB] usernameExists: " + e.getMessage());
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
		// query to check if a phone number already exists in the subscriber table
		String query = "SELECT 1 FROM bpark.subscriber WHERE phoneNumber = ? LIMIT 1";
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setString(1, phone); //set parameter to the query
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
		// query to check if a user ID already exists in the subscriber table
		String query = "SELECT 1 FROM bpark.subscriber WHERE userId = ? LIMIT 1";
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setString(1, userId); //set to the paramter in the query
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
		
		// List to hold late parking events that haven't received a notification
		ArrayList<ParkingEvent> list = new ArrayList<>();
		//  query to retrieve active parking events that haven't been flagged for late message
		String query = "SELECT * FROM parkingEvent WHERE exitDate IS NULL AND sendMsgForLating=FALSE";

		try (PreparedStatement ps = conn.prepareStatement(query); ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				ParkingEvent event = new ParkingEvent();

				// Populate ParkingEvent object from result set
				event.setEventId(rs.getInt("eventId"));
				event.setSubscriberCode(rs.getInt("subscriberCode"));
				event.setParkingSpace(rs.getInt("parking_space"));
				event.setEntryDate(rs.getDate("entryDate").toLocalDate());
				event.setEntryTime(rs.getTime("entryHour").toLocalTime());
				event.setWasExtended(rs.getBoolean("wasExtended"));
				event.setLot(rs.getString("nameParkingLot"));
				event.setVehicleID(rs.getString("vehicleId"));
				event.setParkingCode(rs.getString("parkingCode"));
				// Calculate how long the subscriber has been parked
				LocalDateTime now = LocalDateTime.now();
				LocalDateTime dateTimeEntry = event.getEntryDate().atTime(event.getEntryHour());
				Duration duration = Duration.between(dateTimeEntry, now);
				//check if subscriber is lating according if he extended
				double timeOfParking=duration.toMinutes()/60.0;
				// Add to list if the subscriber is late based on whether they extended or not
				if ((event.isWasExtended() && timeOfParking>8) || ((!event.isWasExtended() )&& timeOfParking>4)) {
					list.add(event);
				} 
			}
			return list;
		} catch (SQLException e) {
			System.err.println("Error retrieving active parking events: " + e.getMessage());
			e.getStackTrace();
		}

		return list; // Return empty list if error occurs
	}


	/**
	 * Marks a subscriber as having been notified for being late. This sets the
	 * 'sendMsgForLating' flag to true for their active parking event.
	 *
	 * @param subscriberCode the code identifying the subscriber
	 */
	public void markSendMail(int subscriberCode) {
		// query to mark that a late notification was sent for the subscriber
		String query = "UPDATE parkingEvent SET sendMsgForLating=TRUE WHERE subscriberCode=?";
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setInt(1, subscriberCode); // set the paramter in the query
			int rowsUpdated = stmt.executeUpdate(); //execute
		} catch (SQLException e) {
			System.err.print("Error mark that mail send for lating:"+ e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Returns the total number of parking spots in the lot.
	 *
	 * @return total number of parkingSpaces
	 */
	public int getTotalSpots() {
		// query to count the total number of parking spaces in the system
		final String sql = "SELECT COUNT(*) FROM bpark.parkingSpaces";

		try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

			if (rs.next()) { // If result is returned, extract and return the total count
				return rs.getInt(1);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		// Return 0 if query failed or no result
		return 0;
	}

	/**
	 * Returns the number of currently occupied parking spots.
	 *
	 * @return count of spots where is_occupied = TRUE
	 */
	public int getOccupiedSpots() {
		// SQL query to count the number of currently occupied parking spaces
		final String sql = "SELECT COUNT(*) FROM bpark.parkingSpaces WHERE is_occupied = TRUE";

		try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

			// Return the count if a result is found
			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		// Return 0 if query fails or no result
		return 0;
	}
	
	/**
	 * Counts how many upcoming reservations (orders) are scheduled
	 * to start in the next 4 hours from now.
	 *
	 * We only care about ACTIVE orders that are expected soon.
	 * This is used to limit how many people can extend their parking,
	 * so reserved spots won't be stolen.
	 *
	 * @return number of upcoming reservations within the next 4 hours
	 */
	public int getUpcomingReservations() {
	    int count = 0;

	    // Build the SQL query to count orders that are active and arriving soon
	    String query = """
	        SELECT COUNT(*) FROM `order`
	        WHERE `status` = 'ACTIVE'
	        AND TIMESTAMP(order_date, arrival_time) BETWEEN NOW() AND NOW() + INTERVAL 4 HOUR
	    """;

	    try (
	        PreparedStatement stmt = conn.prepareStatement(query);
	        ResultSet rs = stmt.executeQuery()
	    ) {
	        // If the query returned something, grab the count
	        if (rs.next()) {
	            count = rs.getInt(1);
	        }
	    } catch (SQLException e) {
	        // Just print the error so we know what went wrong
	        System.err.println("Error retrieving upcoming reservations: " + e.getMessage());
	    }

	    return count;
	}


	
	/**
	 * get all the data that we need to save on parking report.
	 * @param date
	 * @return parking report
	 */
	private ParkingReport getDataForParkingReport(Date date) {
		// query to retrieve parking events for the given month and year
		String query = "SELECT * FROM parkingEvent WHERE YEAR(entryDate)=? AND MONTH(entryDate)=?";
		
		// Extract year and month from the input date
		LocalDate local = date.toLocalDate();
		int year = local.getYear();
		int month = local.getMonthValue();
		System.out.println("year = " + year + ", month = " + month);
		
		// Counters for report statistics
		int totalExtends = 0;
		int totalEntries = 0;
		int totalLates=0;
		int lessThanFourHours=0;
		int betweenFourToEight=0;
		int moreThanEight=0;
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			// Set year and month in the SQL query
			stmt.setInt(1, year);
			stmt.setInt(2, month);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) { // Iterate through the results
				totalEntries++;
				// Skip if exit date or hour is missing (still active)
				if (rs.getDate("exitDate") == null || rs.getTime("exitHour") == null) {
					continue;
				}
				// Calculate duration of parking session in hours
				LocalDateTime entryDateTime = LocalDateTime.of(rs.getDate("entryDate").toLocalDate(), rs.getTime("entryHour").toLocalTime());
				LocalDateTime exitDateTime = LocalDateTime.of(rs.getDate("exitDate").toLocalDate(), rs.getTime("exitHour").toLocalTime());
				Duration duration = Duration.between(entryDateTime, exitDateTime);
				long minutes=duration.toMinutes();
				double durationOfParking=minutes/60.0;

				// Categorize duration into 3 buckets
				if(durationOfParking <4) {
					lessThanFourHours++;
				}
				else if(durationOfParking>=4 && durationOfParking<8) {
					betweenFourToEight++;
				}
				else {
					moreThanEight++;
				}
				// Count extensions and late sessions
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
			// Return a report object with the gathered stats
			return new ParkingReport(totalEntries, totalExtends, totalLates, lessThanFourHours, betweenFourToEight, moreThanEight);
		} catch (SQLException e) {
			System.err.println("Error get data for parking report: " + e.getMessage());
			e.printStackTrace();
		}
		return null; // Return null on failure
	}

	/**
	 * Creates a new parking report for the previous month and saves it to the database.
	 * The report data is calculated based on parking events and then inserted into the parkingReport table.
	 *
	 * @param date the date the report is being created for (usually the first day of the month)
	 */
	public void createParkingReport(Date date) {
	    ParkingReport parkingReport = getDataForParkingReport(date); // Generate parking report data for the given date

	    // SQL query to insert the report into the parkingReport table
	    String query = "INSERT INTO parkingReport(dateOfParkingReport, totalEntries, totalExtends, totalLates, lessThanFourHours, betweenFourToEight, moreThanEight) VALUES (?, ?, ?, ?, ?, ?, ?);";
	    try (PreparedStatement stmt = conn.prepareStatement(query)) {
	        // Set parameters from the ParkingReport object
	        stmt.setDate(1, date);
	        stmt.setInt(2, parkingReport.getTotalEntries());
	        stmt.setInt(3, parkingReport.getTotalExtends());
	        stmt.setInt(4, parkingReport.getTotalLates());
	        stmt.setInt(5, parkingReport.getLessThanFour());
	        stmt.setInt(6, parkingReport.getBetweenFourToEight());
	        stmt.setInt(7, parkingReport.getMoreThanEight());
	        stmt.executeUpdate(); // Insert the report
	        System.out.println("Parking report created!");
	    } catch (SQLException e) {
	        System.err.println("Error creating parking report: " + e.getMessage());
	        e.printStackTrace();
	    }
	}


	/**
	 * check existence of parking report on database for creating only the missing reports.
	 * @param date (of month that we want to check)
	 * @return true if exists. false if doesn't.
	 */
	public boolean parkingReportExists(Date date) {
		// SQL query to check if a parking report already exists for the given date
		String query="SELECT * FROM parkingReport WHERE dateOfParkingReport=?";
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setDate(1, date); // Set the date parameter
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) { // If a result exists, the report already exists
				return true;
			}
		} catch (SQLException e) {
			System.err.println("Error check existence of parking report: "+ e.getMessage());
			e.printStackTrace();
		}
		return false; // Return false if report does not exist or error occurred
	}

	/**
	 * Gets the parking report for a specific date from the database.
	 * This is usually used when a manager wants to view the report for a certain day.
	 *
	 * @param date the date of the parking report
	 * @return a ParkingReport object if found, or null if no report exists or an error occurs
	 */
	public ParkingReport getParkingReport(Date date) {
	    // SQL query to retrieve a parking report for the given date
	    String query = "SELECT * FROM bpark.parkingReport WHERE dateOfParkingReport=?";
	    try (PreparedStatement stmt = conn.prepareStatement(query)) {
	        // Set the date parameter in the query
	        stmt.setDate(1, date);
	        ResultSet rs = stmt.executeQuery();

	        // If a result exists, build and return the ParkingReport object
	        if (rs.next()) {
	            return new ParkingReport(
	                rs.getInt("totalEntries"),
	                rs.getInt("totalExtends"),
	                rs.getInt("totalLates"),
	                rs.getInt("lessThanFourHours"),
	                rs.getInt("betweenFourToEight"),
	                rs.getInt("moreThanEight")
	            );
	        }
	    } catch (SQLException e) {
	        System.err.println("Error getting parking report: " + e.getMessage());
	        e.printStackTrace();
	    }
	    // Return null if no report found or error occurred
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
		// SQL query to generate a live snapshot of subscriber parking activity for the given month and year
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
			// Set the year and month parameters in the query
			ps.setInt(1, year);
			ps.setInt(2, month);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) { // Build the result list from the result set
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
		return rows; // Return the list of status reports
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
			// Insert each subscriber's status row into the report table
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
		return inserted; // Return the number of inserted rows
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
		// query to retrieve stored monthly snapshot of subscriber status, joined with their names
		String sql =
				"SELECT subscriberCode, totalEntries, totalExtends, totalLates, totalHours, " +
						"       CONCAT(s.firstName,' ',s.lastName) AS fullName " +
						"FROM subscriberStatusReport r " +
						"JOIN subscriber s USING (subscriberCode) " +
						"WHERE reportMonth = ?";

		List<SubscriberStatusReport> rows = new ArrayList<>();

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			// Set the reportMonth date (first of month)
			ps.setDate(1, java.sql.Date.valueOf(String.format("%04d-%02d-01", year, month)));
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) { // Convert each row in the result set into a SubscriberStatusReport object
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
		return rows; // Return the list of reports
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
		// SQL query to check if a monthly subscriber status report already exists
		String sql =
				"SELECT 1 FROM subscriberStatusReport " +
						"WHERE MONTH(reportMonth)=? AND YEAR(reportMonth)=? " +
						"LIMIT 1";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			// Set parameters for month and year
			ps.setInt(1, month);
			ps.setInt(2, year);
			try (ResultSet rs = ps.executeQuery()) { // Execute the query and return true if a row is found
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
		// query to check if an active order already exists for the given subscriber, date, and time
		String query = "SELECT * FROM bpark.order WHERE subscriberCode = ? AND order_date = ? AND arrival_time = ? AND `status`='ACTIVE'";

		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			// set parameters to query
			stmt.setInt(1, subscriberCode);
			stmt.setDate(2, selectedDate);
			stmt.setTime(3, timeOfArrival);

			ResultSet rs = stmt.executeQuery(); //execute
			return rs.next(); // if there's at least 1 result of that, it means that there is an order like that that's exists
		} catch (SQLException e) {
			e.printStackTrace();
		}
		// Return false if no match or error occurred
		return false; 
	}


	/**
	 * Retrieves all existing dates for which a parking report has been generated.
	 *
	 * @return a list of dates representing existing parking reports;
	 *         null if an error occurs during retrieval
	 */
	public ArrayList<Date> getAllReportsDates(){
		ArrayList<Date> datesOfReports=new ArrayList<>(); // Create a list to store report dates
		// SQL query to retrieve all report dates from the parkingReport table
		String query="SELECT dateOfParkingReport FROM parkingReport";
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) { // Add each retrieved date to the list
				datesOfReports.add(rs.getDate("dateOfParkingReport"));
			}
			return datesOfReports;
		} catch (SQLException e) {
			System.err.println("Error getting reports dates: "+e.getMessage());
			e.printStackTrace();
		}
		return null; // Return null in case of exception
	}

	/**
	 * set inactive order after 15 minutes from arrival time
	 */
	public void inactiveReservations() {
		// query  to mark reservations as INACTIVE if their arrival time was more than 15 minutes ago
		String query="UPDATE `order` SET `status`='INACTIVE' WHERE `status`='ACTIVE' AND TIMESTAMP(order_date, arrival_time) <= NOW() - INTERVAL 15 MINUTE;";
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			int rowsUpdated = stmt.executeUpdate(); //execute
		} catch (SQLException e) {
			System.err.println("Error inactive reservations: "+e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Checks if this subscriber already has an ACTIVE reservation
	 * within +4 or -4 hours (240 min) of the date-time they just picked.
	 * Exact same time counts as a clash, so you can’t double-book.
	 *
	 * @param subscriberCode subscriber’s numeric code
	 * @param selectedDate   date the user chose 
	 * @param arrivalTime    time the user chose
	 * @return true  if we hit an overlap,
	 *         false if the slot is free
	 */
	public boolean hasReservationConflict(int subscriberCode,
	                                      Date selectedDate,
	                                      Time arrivalTime) {

	    // grab any ACTIVE order for this subscriber that starts
	    // less than 240 min away from the requested timestamp
	    String sql = """
	            SELECT 1
	            FROM `order`
	            WHERE subscriberCode = ?
	              AND `status` = 'ACTIVE'
	              AND ABS(
	                    TIMESTAMPDIFF(
	                        MINUTE,
	                        TIMESTAMP(order_date, arrival_time),
	                        ?
	                    )
	                ) < 240
	            LIMIT 1
	            """;

	    try (PreparedStatement stmt = conn.prepareStatement(sql)) {

	        // Merge separate SQL Date + Time into a single Timestamp
	        Timestamp requestedTs = Timestamp.valueOf(
	                selectedDate.toLocalDate()
	                            .atTime(arrivalTime.toLocalTime()));

	        stmt.setInt(1, subscriberCode);    // filter by subscriber
	        stmt.setTimestamp(2, requestedTs); // compare against this moment

	        // If we get at least one row → clash detected
	        try (ResultSet rs = stmt.executeQuery()) {
	            return rs.next();
	        }
	    } catch (SQLException ex) {
	        // DB blew up? play it safe and block the reservation
	        System.err.println("Error checking reservation conflict: " + ex.getMessage());
	        return true;
	    }
	}


	/**
	 * Tries to log in the given user by setting their is_logged_in flag to 1.
	 * This only works if the user was logged out before.
	 *
	 * @param username the exact username (case-sensitive)
	 * @return true  if the login flag was set (user was offline and is now marked as online)  
	 *         false if the user was already logged in
	 * @throws SQLException if something goes wrong with the database
	 */
	public boolean markUserLoggedIn(String username) throws SQLException {
	    // query to mark the user as logged in only if they are currently logged out
	    final String sql =
	            "UPDATE bpark.user " +
	                    "SET    is_logged_in = 1 " +
	                    "WHERE  BINARY username = ? " +
	                    "  AND  is_logged_in = 0";

	    try (PreparedStatement ps = conn.prepareStatement(sql)) {
	        ps.setString(1, username); // set parameter to query
	        return ps.executeUpdate() == 1; // exactly one row updated
	    }
	}


	/**
	 * Clears the online flag of the user (called on LOGOUT / disconnect).
	 *
	 * @param username exact username
	 */
	public void markUserLoggedOut(String username) {
		// query to mark the given user as logged out
		final String sql =
				"UPDATE bpark.user SET is_logged_in = 0 WHERE BINARY username = ?";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, username); // Set the username parameter
			ps.executeUpdate(); // execute
		} catch (SQLException e) {
			System.err.println("[DB] markUserLoggedOut: " + e.getMessage());
		}
	}

	/**
	 * Checks whether the given username is currently flagged as online
	 * (i.e., is_logged_in = 1 in the user table).
	 *
	 * @param username exact username to check (case sensitive)
	 * @return true if the user is marked online,  
	 *         false otherwise or if a DB error occurs
	 */
	public boolean isUserOnline(String username) {
		// SQL query to check user's login status
		String sql = "SELECT is_logged_in FROM bpark.user " +
				"WHERE BINARY username = ? LIMIT 1";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, username); // set parameter to query
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() && rs.getBoolean("is_logged_in"); // true if logged in
			}
		} catch (SQLException e) {
			System.err.println("[DB] isUserOnline: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Resets the is_logged_in flag for **all** users to 0.
	 * Called once at server startup to clear stale sessions.
	 */
	public void resetAllLoggedIn() {
		// query to reset login status for all users
		String sql = "UPDATE bpark.user SET is_logged_in = 0";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.executeUpdate(); // execute the reset
		} catch (SQLException e) {
			System.err.println("[DB] resetAllLoggedIn: " + e.getMessage());
		}
	}
}
