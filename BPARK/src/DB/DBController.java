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
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            conn = DriverManager.getConnection(
                "jdbc:mysql://localhost/bpark?serverTimezone=IST", "root", "Aa123456");
        } catch (Exception e) {
            System.out.println("Database connection error: " + e.getMessage());
        }
    }

    /**
     * Retrieves all orders from the 'Order' table.
     * Each row is converted into an Order object and added to a list.
     *
     * @return A list of all orders in the database.
     */
    public ArrayList<Order> getAllOrders() {
        ArrayList<Order> orders = new ArrayList<>();
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM `Order`");

            while (rs.next()) {
                Order o = new Order(
                    rs.getInt("order_number"),
                    rs.getInt("parking_space"),
                    Date.valueOf(rs.getString("order_date")), // fixed for timezone accuracy
                    rs.getInt("confirmation_code"),
                    rs.getInt("subscriber_id"),
                    rs.getDate("date_of_placing_an_order")
                );
                orders.add(o);
            }
        } catch (SQLException e) {
            System.out.println("Fetch error: " + e.getMessage());
        }
        return orders;
    }

    /**
     * Updates a specific field in an order identified by its order number.
     * Supports both integer and date fields depending on the column.
     *
     * @param orderNumber ID of the order to update.
     * @param field Column name to be updated.
     * @param newValue New value as a string, to be parsed accordingly.
     * @return True if the update was successful, false otherwise.
     */
    public boolean updateOrderField(int orderNumber, String field, String newValue) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE `Order` SET " + field + " = ? WHERE order_number = ?");

            if (field.equalsIgnoreCase("order_date") || field.equalsIgnoreCase("date_of_placing_an_order")) {
                ps.setDate(1, java.sql.Date.valueOf(newValue));
            } else {
                ps.setInt(1, Integer.parseInt(newValue));
            }

            ps.setInt(2, orderNumber);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.out.println("Update failed: " + e.getMessage());
            return false;
        }
    }

}
