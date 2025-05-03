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
    public static DBController instance = null;

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
                "jdbc:mysql://localhost/bpark?serverTimezone=IST", "root", "Aa123456");
            System.out.println("SQL connection succeed");
        } catch (SQLException ex) {
            // Prints SQL error information if connection fails
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
    }

    /**
     * Checks if an order with the specified number exists in the database.
     *
     * @param order_number the order number to search for
     * @return true if found, false otherwise
     */
    public boolean getOrderByorder_number(int order_number) {
        String query = "SELECT * FROM `order` WHERE order_number=?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, order_number); // injects order_number into SQL query
            ResultSet rs = stmt.executeQuery(); // executes the query
            if (rs.next()) { // if a row is found
                return true;
            }
        } catch (Exception e) {
            System.out.println("Error! " + e.getMessage()); // log the error
        }
        return false; // order not found
    }

    /**
     * Retrieves all orders from the database into a list.
     *
     * @return ArrayList containing all orders
     */
    public ArrayList<Order> getAllOrders() {
        String query = "SELECT * FROM `order`";
        ArrayList<Order> orders = new ArrayList<>(); // initialize list
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery(); // run SELECT query
            while (rs.next()) {
                // Create Order object from each row in the ResultSet
                Order newOrder = new Order(
                    rs.getInt("order_number"),
                    rs.getInt("parking_space"),
                    rs.getDate("order_date"),
                    rs.getInt("confirmation_code"),
                    rs.getInt("subscriber_id"),
                    rs.getDate("date_of_placing_an_order")
                );
                orders.add(newOrder); // add to list
            }
        } catch (Exception e) {
            System.out.println("Error! " + e.getMessage()); // error fetching data
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
     * Updates the parking space value for an order.
     *
     * @param update_parking_space new parking space number
     * @param order_number         ID of the order to update
     * @return true if update succeeded, false otherwise
     */
    public boolean updateParking_space(int update_parking_space, int order_number) {
        String query = "UPDATE `order` SET parking_space=? WHERE order_number=?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, update_parking_space); // new parking space
            stmt.setInt(2, order_number); // target order
            stmt.executeUpdate(); // execute update
        } catch (Exception e) {
            System.out.println("Error! " + e.getMessage()); // log SQL error
            return false;
        }
        return true;
    }

    /**
     * Routes the update command to the appropriate method based on the field name.
     *
     * @param orderNumber order to update
     * @param field       name of the field ("parking_space", "order_date")
     * @param newValue    new value to apply
     * @return status code (1–6) indicating result
     */
    public int updateOrderField(int orderNumber, String field, String newValue) {
        boolean hasOrder = getOrderByorder_number(orderNumber);
        if (hasOrder) {
            if (field.equals("parking_space")) {
                if (updateParking_space(Integer.parseInt(newValue), orderNumber)) {
                    return 1; // parking space updated successfully
                } else {
                    return 2; // update failed
                }
            } else if (field.equals("order_date")) {
                if (updateOrderDateByOrderNumber(orderNumber, newValue)) {
                    return 3; // order date updated
                } else {
                    return 4; // update failed
                }
            } else {
                return 5; // invalid field name
            }
        }
        return 6; // order does not exist
    }

    /**
     * Updates the order date of a specific order.
     *
     * @param order_number the order number to update
     * @param newValue     new date in format "YYYY-MM-DD"
     * @return true if update succeeded, false otherwise
     */
    public boolean updateOrderDateByOrderNumber(int order_number, String newValue) {
        String query = "UPDATE `order` SET order_date = ? WHERE order_number = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setDate(1, Date.valueOf(newValue)); // convert String to SQL Date
            stmt.setInt(2, order_number); // order ID to update
            stmt.executeUpdate(); // perform the update
        } catch (Exception e) {
            System.out.println("Error! " + e.getMessage()); // log error
            return false;
        }
        return true;
    }
}
