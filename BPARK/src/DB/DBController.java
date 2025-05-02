package DB;

import java.sql.*;
import java.util.ArrayList;
import common.Order;

/**
 * Handles all database operations related to the 'Order' table.
 * Implements Singleton to ensure centralized and consistent access to the database.
 */
public class DBController {

    private static DBController instance; // Single static instance
    private Connection conn; // Database connection object

    /**
     * Private constructor to prevent instantiation from outside.
     */
    private DBController() { }

    /**
     * Provides access to the single instance of DBController.
     * @return the singleton instance
     */
    public static DBController getInstance() {
        if (instance == null) {
            instance = new DBController();
        }
        return instance;
    }

    /**
     * Establishes a connection to the MySQL 'bpark' database.
     * If the connection fails, an error message is printed to the console.
     */
    public void connectToDB() {
        try {
            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();

            // Connect to 'bpark' DB on localhost with timezone setting
            conn = DriverManager.getConnection(
                "jdbc:mysql://localhost/bpark?serverTimezone=IST", "root", "Aa123456");

        } catch (Exception e) {
            // Print error if connection fails
            System.out.println("Database connection error: " + e.getMessage());
        }
    }

    /**
     * Retrieves all orders from the 'Order' table.
     * Converts each row into an Order object and adds it to a list.
     *
     * @return List of all orders in the database.
     */
    public ArrayList<Order> getAllOrders() {
        ArrayList<Order> orders = new ArrayList<>();

        try {
            // Create SQL statement
            Statement stmt = conn.createStatement();

            // Execute query to select all rows from the 'Order' table
            ResultSet rs = stmt.executeQuery("SELECT * FROM `Order`");

            // Loop through each result row
            while (rs.next()) {
                // Create Order object from current row
                Order o = new Order(
                    rs.getInt("order_number"),
                    rs.getInt("parking_space"),
                    Date.valueOf(rs.getString("order_date")), // ensures correct date format
                    rs.getInt("confirmation_code"),
                    rs.getInt("subscriber_id"),
                    rs.getDate("date_of_placing_an_order")
                );
                // Add order to the list
                orders.add(o);
            }

        } catch (SQLException e) {
            // Print error if query fails
            System.out.println("Fetch error: " + e.getMessage());
        }

        return orders;
    }


    /**
     * Updates only 'order_date' or 'parking_space' in an order.
     *
     * @param orderNumber The ID of the order to update.
     * @param field The column to update (must be 'order_date' or 'parking_space').
     * @param newValue The new value as a string.
     * @return true if the update succeeded, false otherwise.
     */
    public boolean updateOrderField(int orderNumber, String field, String newValue) {
        // Only allow specific fields to be updated
        if (!field.equalsIgnoreCase("order_date") && !field.equalsIgnoreCase("parking_space")) {
            System.out.println("Update failed: field not allowed.");
            return false;
        }

        try {
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE `Order` SET " + field + " = ? WHERE order_number = ?");

            // Handle date or integer based on field name
            if (field.equalsIgnoreCase("order_date")) {
                ps.setDate(1, java.sql.Date.valueOf(newValue));
            } else {
                ps.setInt(1, Integer.parseInt(newValue)); // parking_space is an int
            }

            ps.setInt(2, orderNumber);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.out.println("Update failed: " + e.getMessage());
            return false;
        }
    }


}
