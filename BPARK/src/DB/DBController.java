package DB;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import common.Order;

/**
 * DBController handles all DB operations for the 'Order' table in the bpark database.
 * Uses Singleton pattern to ensure only one active DB connection exists during runtime.
 */
public class DBController {

    // Singleton instance (static so it's shared among all classes using this)
    private static DBController instance = null;

    // JDBC connection object
    private Connection conn;

    /**
     * Singleton accessor - returns the unique instance of the controller.
     * Creates one if it doesn't exist yet.
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
     * Establishes connection to the MySQL database.
     * Sets up the JDBC driver and opens the connection.
     */
    public void connectToDB() {
        try {
            // Loads and initializes the MySQL JDBC driver class
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            System.out.println("Driver definition succeed");
        } catch (Exception ex) {
            System.out.println("Driver definition failed"); // error in driver loading
        }

        try {
            // Connects to the local MySQL server (replace credentials as needed)
            this.conn = DriverManager.getConnection(
                "jdbc:mysql://localhost/bpark?serverTimezone=Asia/Jerusalem", "root", "Aa123456");
            System.out.println("SQL connection succeed");
        } catch (SQLException ex) {
            // Prints SQL error information if connection fails
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
    }

    /**
     * Checks whether an order with the specified order number exists in the database.
     * Executes a SELECT query using a prepared statement to prevent SQL injection.
     *
     * @param order_number The unique order number to look for.
     * @return true if the order exists, false otherwise.
     */
    public boolean getOrderByOrderNumber(int order_number) {
        // SQL query to retrieve a specific order by its primary key
        String query = "SELECT * FROM `order` WHERE order_number=?";

        // Try-with-resources ensures that stmt and rs are closed automatically
        try (PreparedStatement stmt = conn.prepareStatement(query)) {

            // Inject the provided order number into the query's first parameter
            stmt.setInt(1, order_number);

            // Execute the query and store the result in a ResultSet
            ResultSet rs = stmt.executeQuery();

            // If a record is found, the order exists
            if (rs.next()) {
                return true;
            }

        } catch (Exception e) {
            // Log any exception that occurs during the query execution
            System.out.println("Error! " + e.getMessage());
        }

        // If no result was found or an error occurred, return false
        return false;
    }


    /**
     * Retrieves all orders from the database into a list.
     * This method executes a SELECT * query on the 'order' table and 
     * maps each row to an Order object.
     *
     * @return ArrayList<Order> containing all orders from the DB
     */
    public ArrayList<Order> getAllOrders() {
        // Define SQL query to fetch all orders
        String query = "SELECT * FROM `order`";

        // Initialize list to hold order objects
        ArrayList<Order> orders = new ArrayList<>();

        // Prepare and execute the SQL query
        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            // Iterate over each row in the result set
            while (rs.next()) {
                // Extract values from the current row and construct an Order object
                Order newOrder = new Order(
                    rs.getInt("order_number"),                // primary key
                    rs.getInt("parking_space"),               // assigned parking spot
                    rs.getDate("order_date"),                 // date order created
                    rs.getInt("confirmation_code"),           // confirmation code
                    rs.getInt("subscriber_id"),               // ID of the subscriber
                    rs.getDate("date_of_placing_an_order")    // actual placing date
                );

                // Add the order to the result list
                orders.add(newOrder);
            }

        } catch (Exception e) {
            // Log exception details for debugging
            System.out.println("Error! " + e.getMessage());
        }

        // Return the list of retrieved orders
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
     * Updates the parking space assigned to a specific order.
     * Executes an UPDATE query on the 'order' table using a prepared statement.
     *
     * @param update_parking_space The new parking space number to assign.
     * @param order_number The unique identifier of the order to update.
     * @return true if the update was successful, false if an error occurred.
     */
    public boolean updateParkingSpace(int update_parking_space, int order_number) {
        // SQL query to update the parking_space field of a specific order
        String query = "UPDATE `order` SET parking_space=? WHERE order_number=?";

        // Try-with-resources ensures statement is closed automatically
        try (PreparedStatement stmt = conn.prepareStatement(query)) {

            // Set the new parking space value as the first parameter in the query
            stmt.setInt(1, update_parking_space);

            // Set the order number to identify which record to update
            stmt.setInt(2, order_number);

            // Execute the update command; affects one or more rows in the table
            stmt.executeUpdate();

        } catch (Exception e) {
            // Print the exception message if the update fails
            System.out.println("Error! " + e.getMessage());
            return false;
        }

        // Return true if the update was executed without exceptions
        return true;
    }


    /**
     * Routes an update command to the correct update method based on the field name.
     * Validates the existence of the order and the field name before attempting the update.
     *
     * Supported fields: "parking_space", "order_date"
     *
     * @param orderNumber The ID of the order to be updated.
     * @param field The name of the field to update (e.g., "parking_space", "order_date").
     * @param newValue The new value to apply to the specified field.
     * @return int status code indicating the result:
     *         1 - Parking space updated successfully
     *         2 - Parking space update failed
     *         3 - Order date updated successfully
     *         4 - Order date update failed
     *         5 - Invalid field name provided
     *         6 - Order with given ID does not exist
     */
    public int updateOrderField(int orderNumber, String field, String newValue) {
        // First, check if the order exists in the database
        boolean hasOrder = getOrderByOrderNumber(orderNumber);

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
            	int status=updateOrderDateByOrderNumber(orderNumber, newValue);
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
     * Updates the order_date field of a specific order in the database.
     * Converts the provided string into a java.sql.Date and applies it to the matching order record.
     *
     * @param order_number The unique identifier of the order to be updated.
     * @param newValue A date string in the format "YYYY-MM-DD" representing the new order date.
     * @return true if the update was executed successfully, false otherwise.
     */
    public int updateOrderDateByOrderNumber(int order_number, String newValue) {
        // SQL query to update the order_date field for a specific order
        String query = "UPDATE `order` SET order_date = ? WHERE order_number = ?";

        // Check current placing date for validation
        String checkQuery = "SELECT date_of_placing_an_order FROM `order` WHERE order_number = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            checkStmt.setInt(1, order_number);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                Date placingDate = rs.getDate("date_of_placing_an_order");
                Date newOrderDate = Date.valueOf(newValue);

                // Validate that the new order date is not before placing date
                if (newOrderDate.before(placingDate)) {
                    System.out.println("Error! order_date cannot be before date_of_placing_an_order.");
                    return 1;
                }
                
                // Validate that the new order date is not in the past
                Date today = new Date(System.currentTimeMillis());
                if (newOrderDate.before(today)) {
                    System.out.println("Error! order_date cannot be in the past.");
                    return 6;
                }
                
            } else { //this else never act!!!!!
                System.out.println("Error! Order not found.");
                return 2;
            }
        } catch (Exception e) {
            System.out.println("Validation failed: " + e.getMessage());
            return 3;
        }

        // Try-with-resources ensures that stmt is closed automatically
        try (PreparedStatement stmt = conn.prepareStatement(query)) {

            // Convert the input string to a java.sql.Date and set as the first parameter
            stmt.setDate(1, Date.valueOf(newValue));

            // Set the second parameter: the order number to identify which record to update
            stmt.setInt(2, order_number);

            // Execute the update command on the database
            stmt.executeUpdate();

        } catch (Exception e) {
            // Print the error message if an exception occurs during the update
            System.out.println("Error! " + e.getMessage());
            return 4;
        }

        // Return true if the update completed without exceptions
        return 5;
    }


}
