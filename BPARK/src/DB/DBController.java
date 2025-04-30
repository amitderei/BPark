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
                           + ", Order Date: " + rs.getDate("order_date")
                           + ", Confirmation Code: " + rs.getInt("confirmation_code")
                           + ", Subscriber ID: " + rs.getInt("subscriber_id")
                           + ", Placing Date: " + rs.getDate("date_of_placing_an_order");
                orders.add(row);
            }
        } catch (SQLException e) {
            System.out.println("Fetch error: " + e.getMessage());
        }
        return orders;
    }


    /**
     * Updates a specific field in the 'Order' table based on the order number.
     *
     * @param orderNumber The unique order ID to be updated.
     * @param field The name of the column to update (e.g., 'parking_space', 'order_date').
     * @param newValue The new value as a string. Should match the expected SQL type of the column.
     * @return true if the update was successful, false otherwise.
     */
    public boolean updateOrderField(int orderNumber, String field, String newValue) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE `Order` SET " + field + " = ? WHERE order_number = ?");

            // handle types based on field name
            if (field.equalsIgnoreCase("order_date") || field.equalsIgnoreCase("date_of_placing_an_order")) {
                // convert string to SQL Date
                ps.setDate(1, java.sql.Date.valueOf(newValue));
            } else {
                // use integer for parking_space or confirmation_code etc.
                ps.setInt(1, Integer.parseInt(newValue));
            }

            ps.setInt(2, orderNumber);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
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
    
    public static void main(String[] args) {
        DBController db = new DBController();
        db.connectToDB();

        // Test fetching all orders
        ArrayList<String> orders = db.getAllOrders();
        System.out.println("=== Orders in Database ===");
        for (String o : orders) {
            System.out.println(o);
        }

        // Test updating a field (example: change parking_space of order 1001 to 99)
        boolean updated = db.updateOrderField(1001, "parking_space", "99");
        System.out.println(updated ? "Update succeeded." : "Update failed.");

        // Re-fetch to verify the change
        System.out.println("=== After Update ===");
        ArrayList<String> updatedOrders = db.getAllOrders();
        for (String o : updatedOrders) {
            System.out.println(o);
        }


    }
}
