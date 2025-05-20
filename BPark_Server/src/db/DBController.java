package db;
import common.User;
import java.sql.Connection;

import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
			instance = new DBController(); // lazy initialization
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
			conn = DriverManager.getConnection("jdbc:mysql://localhost/bpark?serverTimezone=Asia/Jerusalem",
					"root", "Yosi2311");
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
							rs.getDate("order_date"), rs.getInt("confirmation_code"), rs.getInt("subscriberCode"),
							rs.getDate("date_of_placing_an_order"));
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
						rs.getDate("order_date"), rs.getInt("confirmation_code"), rs.getInt("subscriberCode"),
						rs.getDate("date_of_placing_an_order"));
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

	public void placingAnNewOrder(Order newOrder) {
		String query = "INSERT INTO `order` (parking_space, order_number, order_date, confirmation_code, subscriberCode, date_of_placing_an_order) VALUES (?, ?, ?, ?, ?, ?)";
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setInt(0, newOrder.getParkingSpace());
			stmt.setInt(1, newOrder.getOrderNumber());
			stmt.setDate(2, newOrder.getOrderDate());
			stmt.setInt(3, newOrder.getConfirmationCode());
			stmt.setInt(4, newOrder.getSubscriberId());
			stmt.setDate(0, newOrder.getDateOfPlacingAnOrder());
			
			int succeed=stmt.executeUpdate();
			if(succeed>0) {
				System.out.println("data in table");
			}
		} catch (Exception e) {
			// Error during update execution
			System.out.println("Error! " + e.getMessage());
		}
	}
	public static User validateLogin(String userId, String password) {
	    try {
	        // Create a prepared SQL statement to avoid SQL injection
	        String sql = "SELECT role FROM user WHERE userId = ? AND password = ?";
	        PreparedStatement stmt = conn.prepareStatement(sql);

	        // Fill in the parameters
	        stmt.setString(1, userId);
	        stmt.setString(2, password);

	        // Execute the query
	        ResultSet rs = stmt.executeQuery();

	        // If a match is found, return a new User with the retrieved role
	        if (rs.next()) {
	            String role = rs.getString("role");
	            return new User(userId, role);
	        }

	    } catch (SQLException e) {
	        e.printStackTrace(); // Print error if query fails
	    }

	    // If no match was found or an error occurred
	    return null;
	}

	
	
}
