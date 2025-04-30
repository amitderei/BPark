package DB;

import java.sql.*;
import java.util.ArrayList;

/**
 * Handles connection and operations on the 'Order' table.
 */
public class DBController {
    private Connection conn; //dsfdsffs

    /**
     * Connects to the MySQL 'bpark' database.
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
     * Returns all rows from the 'Order' table as formatted strings.
     */
    public ArrayList<String> getAllOrders() {
        ArrayList<String> orders = new ArrayList<>();
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM `Order`");

            while (rs.next()) {
                String row = "Order #" + rs.getInt("order_number")
                           + ", Parking: " + rs.getInt("parking_space")
                           + ", Date: " + rs.getDate("order_date");
                orders.add(row);
            }
        } catch (SQLException e) {
            System.out.println("Fetch error: " + e.getMessage());
        }
        return orders;
    }

    /**
     * Updates a specific field in the 'Order' table.
     * @param orderNumber The order to update.
     * @param field The column name (e.g., 'parking_space', 'order_date').
     * @param newValue New value as String.
     */
    public boolean updateOrderField(int orderNumber, String field, String newValue) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE `Order` SET " + field + " = ? WHERE order_number = ?");
            ps.setString(1, newValue);
            ps.setInt(2, orderNumber);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Update failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Inserts a new order row into the table.
     */
    public boolean insertNewOrder(int parkingSpace, int orderNumber, String orderDate,
                                  int confirmationCode, int subscriberId, String placingDate) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO `Order` (parking_space, order_number, order_date, confirmation_code, subscriber_id, date_of_placing_an_order) " +
                "VALUES (?, ?, ?, ?, ?, ?)");
            ps.setInt(1, parkingSpace);
            ps.setInt(2, orderNumber);
            ps.setString(3, orderDate);
            ps.setInt(4, confirmationCode);
            ps.setInt(5, subscriberId);
            ps.setString(6, placingDate);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Insert failed: " + e.getMessage());
            return false;
        }
    }
}
