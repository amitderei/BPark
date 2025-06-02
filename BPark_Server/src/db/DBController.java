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

import common.*;

/**
 * DBController handles all DB operations for the 'Order' table in the bpark
 * database. Uses Singleton pattern to ensure only one active DB connection
 * exists during runtime.
 */
public class DBController {

	// Singleton instance (static so it's shared among all classes using this)
	private static DBController instance = null;

	// JDBC connection object
	private static Connection conn;

	/**
	 * Singleton accessor - returns the unique instance of the controller. Creates
	 * one if it doesn't exist yet.
	 *
	 * @return DBController singleton instance
	 */
	public static DBController getInstance() {
		if (instance == null) {
			instance = new DBController(); // initialization
		}
		return instance;
	}

	/**
	 * Establishes connection to the MySQL database. Sets up the JDBC driver and
	 * opens the connection.
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
	 * Checks if an order with the specified order number exists in the database.
	 * Executes a SELECT query using a prepared statement to safely check for
	 * existence.
	 *
	 * @param orderNumber The unique order number to check.
	 * @return true if the order exists, false otherwise.
	 */
	public ArrayList<Order> orderExists(int orderNumber) {
		// SQL query to check if an order with the given number exists
		String query = "SELECT * FROM `order` WHERE order_number=?";

		ArrayList<Order> list = new ArrayList<>();

		// Use try-with-resources to ensure the PreparedStatement is closed
		// automatically
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			// Set the order number parameter in the query
			stmt.setInt(1, orderNumber);

			// Use try-with-resources to ensure the ResultSet is closed automatically
			try (ResultSet rs = stmt.executeQuery()) {
				// Return true if a record exists in the result set
				while (rs.next()) {
					Order newOrder = new Order(rs.getInt("order_number"), rs.getInt("parking_space"),
							rs.getDate("order_date"), rs.getTime("arrival_time"), rs.getString("confirmation_code"),
							rs.getInt("subscriberCode"), rs.getDate("date_of_placing_an_order"));
					list.add(newOrder);
				}
			}

		} catch (SQLException e) {
			// Log the error details for debugging purposes
			System.err.println("Error checking if order exists: " + e.getMessage());
		}

		return list;
	}

	/**
	 * Retrieves all orders from the database and returns them as a list. Executes a
	 * SELECT * query on the 'order' table and maps each row to an Order object.
	 *
	 * @return List of all orders from the database.
	 */
	public ArrayList<Order> getAllOrders() {
		// Define the SQL query to fetch all columns from the 'order' table
		System.out.println("DB");
		String query = "SELECT * FROM `order`";
		ArrayList<Order> orders = new ArrayList<>();

		try (PreparedStatement stmt = conn.prepareStatement(query); ResultSet rs = stmt.executeQuery()) {

			// Go through each row and create an Order object
			while (rs.next()) {
				Order newOrder = new Order(rs.getInt("order_number"), rs.getInt("parking_space"),
						rs.getDate("order_date"), rs.getTime("arrival_time"), rs.getString("confirmation_code"),
						rs.getInt("subscriberCode"), rs.getDate("date_of_placing_an_order"));
				orders.add(newOrder);
			}

		} catch (SQLException e) {
			System.err.println("Error retrieving orders: " + e.getMessage());
		}

		return orders;
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
	 * Routes an update command to the correct update method based on the field
	 * name. Validates the existence of the order and the field name before
	 * attempting the update.
	 *
	 * Supported fields: "parking_space", "order_date"
	 *
	 * @param orderNumber The ID of the order to be updated.
	 * @param field       The name of the field to update (e.g., "parking_space",
	 *                    "order_date").
	 * @param newValue    The new value to apply to the specified field.
	 * @return int status code indicating the result: 1 - Parking space updated
	 *         successfully 2 - Parking space update failed 3 - Order date updated
	 *         successfully 4 - Order date update failed 5 - Invalid field name
	 *         provided 6 - Order with given ID does not exist 7 - Order date is in
	 *         the past and not allowed
	 */

	public int updateOrderField(int orderNumber, String field, String newValue) {
		// First, check if the order exists in the database
		boolean hasOrder = true;
		if (orderExists(orderNumber).isEmpty()) {
			hasOrder = false;
		}

		if (hasOrder) {
			// Handle update for parking space
			if (field.equals("parking_space")) {
				// Convert newValue to int and perform the update
				if (updateParkingSpace(Integer.parseInt(newValue), orderNumber)) {
					return 1; // Success
				} else {
					return 2; // Failure to update parking space
				}
			}

			// Handle update for order date
			else if (field.equals("order_date")) {
				int status = updateOrderDate(orderNumber, newValue);
				switch (status) {
				case 5:
					return 3;
				case 1:
					return 4;
				case 6:
					return 7;
				default:
					return 5;
				}

			}
		}
		// No order found with the provided order number
		return 6;
	}

	/**
	 * Updates the 'order_date' of an order after performing validation checks.
	 * 
	 * Validations performed: - The order must exist in the database. - The new date
	 * must not be earlier than the placing date. - The new date must not be in the
	 * past.
	 *
	 * @param orderNumber The unique identifier of the order to update.
	 * @param newValue    The new date to set (format: "YYYY-MM-DD").
	 * @return Status code: 1 - New date is before placing date (invalid) 2 - Order
	 *         not found 3 - Error during validation check 4 - Error during update
	 *         execution 5 - Update successful 6 - New date is in the past (invalid)
	 */
	public int updateOrderDate(int orderNumber, String newValue) {
		String query = "UPDATE `order` SET order_date = ? WHERE order_number = ?";
		String checkQuery = "SELECT date_of_placing_an_order FROM `order` WHERE order_number = ?";

		try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
			// Set the order number for the validation query
			checkStmt.setInt(1, orderNumber);
			ResultSet rs = checkStmt.executeQuery();

			if (rs.next()) {
				// Extract placing date from the database
				Date placingDate = rs.getDate("date_of_placing_an_order");
				Date newOrderDate = Date.valueOf(newValue);

				// Check if the new date is before the placing date
				if (newOrderDate.before(placingDate)) {
					System.out.println("Error! order_date cannot be before date_of_placing_an_order.");
					return 1;
				}

				// Check if the new date is in the past
				Date today = Date.valueOf(java.time.LocalDate.now());
				if (newOrderDate.before(today)) {
					System.out.println("Error! order_date cannot be in the past.");
					return 6;
				}

			} else {
				// No matching order found
				System.out.println("Error! Order not found.");
				return 2;
			}

		} catch (Exception e) {
			// Error during validation query
			System.out.println("Validation failed: " + e.getMessage());
			return 3;
		}

		// Perform the update if validations passed
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setDate(1, Date.valueOf(newValue)); // Set new date
			stmt.setInt(2, orderNumber); // Set order number
			stmt.executeUpdate(); // Execute update
		} catch (Exception e) {
			// Error during update execution
			System.out.println("Error! " + e.getMessage());
			return 4;
		}

		// Update completed successfully
		return 5;
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
	 * Retrieves the subscriber code associated with a given tag ID.
	 * This version performs a case-sensitive match using the BINARY keyword.
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
	 * Retrieves the latest open parking event for the specified subscriber and parking code.
	 * An event is considered open if its exitDate is null (i.e., the vehicle has not exited yet).
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
	                LocalDate exitDate = rs.getDate("exitDate") != null
	                                   ? rs.getDate("exitDate").toLocalDate()
	                                   : null;
	                LocalTime exitHour = rs.getTime("exitHour") != null
	                                   ? rs.getTime("exitHour").toLocalTime()
	                                   : null;

	                ParkingEvent event = new ParkingEvent(
	                    rs.getInt("subscriberCode"),
	                    rs.getInt("parking_space"),
	                    rs.getDate("entryDate").toLocalDate(),
	                    rs.getTime("entryHour").toLocalTime(),
	                    exitDate,
	                    exitHour,
	                    rs.getBoolean("wasExtended"),
	                    rs.getString("vehicleId"),
	                    rs.getString("NameParkingLot"),
	                    rs.getString("parkingCode")
	                );

	                event.setEventId(rs.getInt("eventId")); // Set ID after object construction
	                return event;
	            }
	        }
	    }

	    return null;
	}




	/**
	 * Handles vehicle pickup for a subscriber by finalizing the parking event,
	 * calculating the total parking duration, and determining if the pickup is allowed
	 * based on whether an extension was used.
	 *
	 * - Allows pickup within 4 hours by default.
	 * - If an extension was used, allows up to 8 hours.
	 * - If over 8 hours, notifies the subscriber and registers a delay.
	 *
	 * @param subscriberCode the subscriber's ID
	 * @param parkingCode    the code entered by the subscriber to identify the parking event
	 * @return ServerResponse indicating the result and a message to be shown to the user
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

	        // Calculate time difference in hours between entry and now
	        LocalDateTime entryTime = LocalDateTime.of(event.getEntryDate(), event.getEntryHour());
	        long hours = (System.currentTimeMillis() -
	                      entryTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
	                     / (1000 * 60 * 60);

	        int allowedHours = event.isWasExtended() ? 8 : 4;

	        if (hours <= allowedHours) {
	            return new ServerResponse(true, null, "Vehicle pickup successful (" + hours + " hours).");
	        }

	        if (!event.isWasExtended()) {
	            return new ServerResponse(false, null, "Parking exceeded 4 hours. Please extend your session before pickup.");
	        }

	        // More than 8 hours even after extension → considered delayed
	        sendNotification(subscriberCode, "You have exceeded the extended parking limit. A delay over 8 hours was recorded.");

	        return new ServerResponse(true, null,
	                "Pickup successful. Delay over 8 hours registered. A notification was sent to your email and phone.");

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
	 * Sends the active parking code to the subscriber by retrieving it from the
	 * latest open parkingEvent and "sending" it to email and SMS.
	 *
	 * @param subscriberCode the subscriber's code
	 * @return ServerResponse with success or failure message
	 */
	public ServerResponse sendParkingCodeToSubscriber(int subscriberCode) {
		String query = """
				SELECT e.parkingCode, s.email, s.phoneNumber
				FROM parkingEvent e
				JOIN subscriber s ON e.subscriberCode = s.subscriberCode
				WHERE e.subscriberCode = ? AND e.exitDate IS NULL
				ORDER BY e.eventId DESC
				LIMIT 1
				  """;

		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setInt(1, subscriberCode);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				int parkingCode = rs.getInt("parkingCode");

				// Send notification to subscriber with the parking code
				sendNotification(subscriberCode, "Your parking code is " + parkingCode);

				return new ServerResponse(true, null, "Parking code sent to your email and phone.");
			} else {
				// No open event found for this subscriber
				return new ServerResponse(false, null, "No active parking session found.");
			}

		} catch (SQLException e) {
			// Database error occurred
			System.err.println("Error sending parking code: " + e.getMessage());
			return new ServerResponse(false, null, "An error occurred while retrieving your parking code.");
		}
	}

	/**
	 * Simulates sending a message to the subscriber via email and SMS.
	 *
	 * @param subscriberCode the subscriber to notify
	 * @param message        the message content to send
	 */
	private void sendNotification(int subscriberCode, String message) {
		String notifyQuery = """
				SELECT email, phoneNumber
				FROM subscriber
				WHERE subscriberCode = ?
				  """;

		try (PreparedStatement stmt = conn.prepareStatement(notifyQuery)) {
			stmt.setInt(1, subscriberCode);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				String email = rs.getString("email");
				String phone = rs.getString("phoneNumber");

				System.out.println("EMAIL to " + email + ": " + message);
				System.out.println("SMS to " + phone + ": " + message);
			}
		} catch (SQLException e) {
			System.err.println("Error sending notification: " + e.getMessage());
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
	 * Counts the number of available (unoccupied) parking spots in a specific
	 * parking lot. Currently supports "Braude" lot.
	 *
	 * @return number of available spots, or -1 if an error occurred
	 */
	public int countAvailableSpots() {
		String query = "SELECT totalSpots, occupiedSpots FROM bpark.parkingLot WHERE NameParkingLot = 'Braude'";

		try (PreparedStatement stmt = conn.prepareStatement(query); ResultSet rs = stmt.executeQuery()) {

			if (rs.next()) {
				int total = rs.getInt("totalSpots");
				int occupied = rs.getInt("occupiedSpots");
				return total - occupied;
			}

		} catch (SQLException e) {
			System.err.println("Error counting available spots: " + e.getMessage());
		}

		return -1;
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
	 * Checks whether the given subscriber has a valid reservation at the current date and time.
	 * A reservation is considered valid only if:
	 * - The reservation date is today.
	 * - The current time is within 15 minutes after the scheduled arrival time.
	 *
	 * For example: if a reservation is for 08:00, it is only valid between 08:00 and 08:15.
	 * After 08:15, entry is no longer allowed.
	 *
	 * @param subscriberCode The unique identifier of the subscriber to check.
	 * @return true if a valid reservation exists now, false otherwise.
	 */
	public boolean checkSubscriberHasReservationNow(int subscriberCode) {

		String query = "SELECT order_date, arrival_time FROM bpark.order WHERE subscriberCode = ?";

		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setInt(1, subscriberCode);
			ResultSet rs = stmt.executeQuery();

			LocalDate today = LocalDate.now();   // Current date
			LocalTime now = LocalTime.now();     // Current time

			// Going over all of the subscriber's reservation to check if there's a reservation in the range
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
	 * Checks if a subscriber has a valid reservation with a specific confirmation code
	 * and that the current time is within 15 minutes from the arrival time.
	 *
	 * @param subscriberCode The subscriber code to check
	 * @param confirmationCode The confirmation code to verify
	 * @return true if a valid reservation with the given confirmation code exists and the current time is within the allowed window, false otherwise
	 */
	public boolean checkConfirmationCode(int subscriberCode, int confirmationCode) {
		String query = "SELECT order_date, arrival_time FROM bpark.order WHERE subscriberCode = ? AND confirmation_code = ?";

		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setInt(1, subscriberCode);
			stmt.setInt(2, confirmationCode);

			// Execute the query
			ResultSet rs = stmt.executeQuery();

			LocalDate today = LocalDate.now();   // Current date
			LocalTime now = LocalTime.now();     // Current time

			// Going over all of the subscriber's reservation to check if there's a reservation in the range
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
	 * @return true if there are available spots (occupied < total), false otherwise.
	 */
	public boolean hasAvailableSpots(String parkingLotName) {
		String query = "SELECT totalSpots, occupiedSpots FROM bpark.parkinglot WHERE NameParkingLot = ?";

		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setString(1, parkingLotName);

			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				int totalSpots = rs.getInt("totalSpots");
				int occupiedSpots = rs.getInt("occupiedSpots");

				//return true if there are more parking spots that occupied spots, false otherwise				
				return occupiedSpots < totalSpots;
			}

		} catch (SQLException e) {
			System.err.println("Error checking available spots: " + e.getMessage());
		}
		
		System.out.println("error");
		return false; // Return false if parking lot not found or error occurred
	}

	/**
	 * Finds the first available parking space that is not currently occupied.
	 *
	 * Queries the 'parkingspaces' table for a parking space where 'is_occupied' is false.
	 * Returns the 'parking_space' value of the first free spot found.
	 *
	 * @return the parking space number if available, if not found then -1.
	 */
	public int findParkingSpace() {
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
	 * @param parkingEvent The ParkingEvent object containing all the event data to be stored.
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
	 * Increments the number of occupied parking spots in the 'parkinglot' table by 1.
	 */
	public void addOccupiedParkingSpace() {
	    String query = "UPDATE bpark.parkinglot SET occupiedSpots = occupiedSpots + 1 WHERE NameParkingLot = 'Braude'";

	    try (PreparedStatement stmt = conn.prepareStatement(query)) {
	        stmt.executeUpdate();
	    } catch (SQLException e) {
	        System.err.println("Error updating occupied parking spots: " + e.getMessage());
	    }
	}

	/**
	 * Updates the specified parking space to be marked as occupied in the 'parkingspaces' table.
	 *
	 * @param parkingSpace The parking space number to mark as occupied (as a String).
	 */
	public void updateParkingSpaceOccupied(int parkingSpace) {
	    String query = "UPDATE bpark.parkingspaces SET is_occupied = 1 WHERE parking_space = ?";

	    try (PreparedStatement stmt = conn.prepareStatement(query)) {
	        stmt.setInt(1, parkingSpace);  // Parse String to int
	        stmt.executeUpdate();
	    } catch (SQLException e) {
	        System.err.println("Error updating parking space occupancy: " + e.getMessage());
	    } catch (NumberFormatException e) {
	        System.err.println("Invalid parking space format: " + parkingSpace);
	    }
	}


	/**
	 * Finds the vehicle ID associated with the given subscriber code.

	 * Executes a query on the 'vehicle' table to retrieve the vehicleId
	 * for the specified subscriberCode.
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
	
	public ArrayList<Order> returnReservationOfSubscriber(Subscriber subscriber){
		System.out.println("here3");
		String query="SELECT * FROM `order` WHERE subscriberCode=? AND order_date>CURDATE()+INTERVAL 1 DAY";
		ArrayList<Order> orders=new ArrayList<>();
		int subsCode= subscriber.getSubscriberCode();
		try(PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setInt(1, subsCode);
			try(ResultSet rs = stmt.executeQuery()){
				while(rs.next()) {
					Order newOrder = new Order(rs.getInt("order_number"), rs.getInt("parking_space"),
							rs.getDate("order_date"), rs.getTime("arrival_time"), rs.getString("confirmation_code"),
							rs.getInt("subscriberCode"), rs.getDate("date_of_placing_an_order"));
					orders.add(newOrder);
				}
				return orders;
			}
		}catch (SQLException e) {
			System.err.println("Error finding reservations: " + e.getMessage());
		}
		return null;
	}

}
