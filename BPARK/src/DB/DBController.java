package DB;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.TimeZone;

public class DBController {

	public static void main(String[] args) {
		System.out.println(TimeZone.getDefault());
		Connection conn = connectToDB();
		if (conn != null) {
			Scanner scanner = new Scanner(System.in);
			System.out.print("Enter order number: ");
			//int order_num = scanner.nextInt();
			System.out.print("Enter update parking space: ");
			//int update_parking_space = scanner.nextInt();
			//updateParking_space(conn, update_parking_space, order_num);
			//getOrderByorder_number(conn, order_num);
			getOrders(conn);
			disconnectFromDB(conn);
		}
	}

	/**
	 * This function connect to DB
	 * 
	 * @return conn(if the connection succeed) or null(if doesn't)
	 */
	public static Connection connectToDB() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
			System.out.println("Driver definition succeed");
		} catch (Exception ex) {
			/* handle the error */
			System.out.println("Driver definition failed");
			return null;
		}

		try {
			Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/bpark?serverTimezone=IST", "root",
					"Aa123456");
			// Connection conn =
			// DriverManager.getConnection("jdbc:mysql://192.168.3.68/test","root","Root");
			System.out.println("SQL connection succeed");
			return conn;
		} catch (SQLException ex) {/* handle any errors */
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
			return null;
		}
	}

	/**
	 * This function print in console the order that its num is order_num
	 * 
	 * @param con
	 * @param order_number
	 */
	public static void getOrderByorder_number(Connection con, int order_number) {
		String query = "SELECT * FROM `order` WHERE order_number=?";
		try (PreparedStatement stmt = con.prepareStatement(query)) {
			stmt.setInt(1, order_number);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				System.out.println("parking space: " + rs.getInt("parking_space"));
				System.out.println("order number: " + rs.getInt("order_number"));
				System.out.println("order_date: " + rs.getDate("order_date"));
				System.out.println("confirmation code: " + rs.getInt("confirmation_code"));
				System.out.println("subscriber id: " + rs.getInt("subscriber_id"));
				System.out.println("date of placing an order: " + rs.getDate("date_of_placing_an_order"));
			} else {
				System.out.println("no order with num " + order_number);
			}
		} catch (Exception e) {
			System.out.println("Error! " + e.getMessage());
		}
	}

	/**
	 * print all orders
	 * 
	 * @param con
	 */
	public static ArrayList<String> getOrders(Connection con) {
		String query = "SELECT * FROM `order`";
		ArrayList<String> orders= new ArrayList<>();
		try (PreparedStatement stmt = con.prepareStatement(query)) {
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				StringBuilder newOrder=new StringBuilder();
				newOrder.append("Order #");
				newOrder.append(rs.getInt("order_number"));
				newOrder.append(": Parking: ");
				newOrder.append(rs.getInt("parking_space"));
				newOrder.append(", Date: ");
				newOrder.append(rs.getDate("order_date"));
				newOrder.append(", Confirmation: ");
				newOrder.append(rs.getInt("confirmation_code"));
				newOrder.append(", Subscriber:");
				newOrder.append(rs.getInt("subscriber_id"));
				newOrder.append(", Placed:");
				newOrder.append(rs.getDate("date_of_placing_an_order"));
				orders.add(newOrder.toString());
			}
		} catch (Exception e) {
			System.out.println("Error! " + e.getMessage());
		}
		return orders;
	}

	/**
	 * This function disconnect from sql
	 * 
	 * @param conn
	 */
	public static void disconnectFromDB(Connection conn) {
		try {
			conn.close();
		} catch (Exception e) {
			System.out.println("Error! " + e.getMessage());
		}
	}

	/**
	 * This function update the parking space of order that its number is
	 * order_number
	 * 
	 * @param conn
	 * @param update_parking_space
	 * @param order_number
	 */
	public static boolean updateParking_space(Connection conn, int update_parking_space, int order_number) {
		String query = "UPDATE `order` SET parking_space=? WHERE order_number=?";
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setInt(1, update_parking_space);
			stmt.setInt(2, order_number);
			stmt.executeUpdate();
		} catch (Exception e) {
			System.out.println("Error! " + e.getMessage());
			return false;
		}
		return true;
	}

	
	/**
	 * This function update date by order number
	 * @param con
	 * @param order_number
	 */
	public static boolean updateOrderDateByOrderNumber(Connection con, int order_number, String newValue) {
		String query = "UPDATE `order` SET order_date = ? WHERE order_number = ?";
		try (PreparedStatement stmt = con.prepareStatement(query)) {
			stmt.setDate(1, Date.valueOf(newValue));
			stmt.setInt(2, order_number);
			
			stmt.executeUpdate();
			
		} catch (Exception e) {
			System.out.println("Error! " + e.getMessage());
			return false;
		}
		return true;
	}
}

