package DB;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

import common.Order;

public class DBController {
	public static DBController instance = null;
	private Connection conn;

	/**
	 * create instance of DBController (according singletone- design pattern)
	 * 
	 * @return
	 */
	public static DBController getInstance() {
		if (instance == null) {
			instance = new DBController();
		}
		return instance;
	}

	/**
	 * This function connect to DB
	 * 
	 * @return conn(if the connection succeed) or null(if doesn't)
	 */
	public void connectToDB() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
			System.out.println("Driver definition succeed");
		} catch (Exception ex) {
			/* handle the error */
			System.out.println("Driver definition failed");
		}

		try {
			this.conn = DriverManager.getConnection("jdbc:mysql://localhost/bpark?serverTimezone=IST", "root",
					"Aa123456");
			// Connection conn =
			// DriverManager.getConnection("jdbc:mysql://192.168.3.68/test","root","Root");
			System.out.println("SQL connection succeed");
		} catch (SQLException ex) {/* handle any errors */
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
	}

	/**
	 * This function print in console the order that its num is order_num
	 * 
	 * @param con
	 * @param order_number
	 */
	public boolean getOrderByorder_number(int order_number) {
		String query = "SELECT * FROM `order` WHERE order_number=?";
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setInt(1, order_number);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				return true;
			}
		} catch (Exception e) {
			System.out.println("Error! " + e.getMessage());
		}
		return false;
	}

	/**
	 * this function collects all orders and return them
	 * 
	 * @param con
	 * @return arrayList of all orders
	 */
	public ArrayList<Order> getAllOrders() {
		String query = "SELECT * FROM `order`";
		ArrayList<Order> orders = new ArrayList<>();
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				Order newOrder = new Order(rs.getInt("order_number"), rs.getInt("parking_space"),
						rs.getDate("order_date"), rs.getInt("confirmation_code"), rs.getInt("subscriber_id"),
						rs.getDate("date_of_placing_an_order"));
				orders.add(newOrder);
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
	public void disconnectFromDB() {
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
	public boolean updateParking_space(int update_parking_space, int order_number) {
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
	 * nevigate the update according the field
	 * 
	 * @param orderNumber
	 * @param field
	 * @param newValue
	 * @return true if succeed, else false
	 */
	public int updateOrderField(int orderNumber, String field, String newValue) {
		boolean hasOrder = getOrderByorder_number(orderNumber);
		if (hasOrder) {
			if (field.equals("parking_space")) {
				if (updateParking_space(Integer.parseInt(newValue), orderNumber)) {
					return 1; //succeed
				}
				else {
					return 2; //failed
				}
			} else if (field.equals("order_date")) {
				if (updateOrderDateByOrderNumber(orderNumber, newValue)) {
					return 3; //succeed
				}
				else {
					return 4;//failed
				}
			}
			else {
				return 5;//error in field
			}
		}
		return 6; //no such order
	}

	/**
	 * This function update date by order number
	 * 
	 * @param con
	 * @param order_number
	 */
	public boolean updateOrderDateByOrderNumber(int order_number, String newValue) {
		String query = "UPDATE `order` SET order_date = ? WHERE order_number = ?";
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
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
