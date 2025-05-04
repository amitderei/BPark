package DB;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;
<<<<<<< HEAD

public class DBController {

	public static void main(String[] args) {
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
=======

import common.Order;

public class DBController {
	public static DBController instance = null;
	private Connection conn;

	/**
	 * create instance of DBController (according singletone- design pattern)
	 * 
	 * @return instance
	 */
	public static DBController getInstance() {
		if (instance == null) {
			instance = new DBController();
		}
		return instance;
>>>>>>> AmitBranch
	}

	/**
	 * This function connect to DB
	 * 
	 * @return conn(if the connection succeed) or null(if doesn't)
	 */
<<<<<<< HEAD
	public static Connection connectToDB() {
=======
	public void connectToDB() {
>>>>>>> AmitBranch
		try {
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
			System.out.println("Driver definition succeed");
		} catch (Exception ex) {
			/* handle the error */
			System.out.println("Driver definition failed");
<<<<<<< HEAD
			return null;
		}

		try {
			Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/bpark?serverTimezone=IST", "root",
					"Aa123456");
			// Connection conn =
			// DriverManager.getConnection("jdbc:mysql://192.168.3.68/test","root","Root");
			System.out.println("SQL connection succeed");
			return conn;
=======
		}

		try {
			this.conn = DriverManager.getConnection("jdbc:mysql://localhost/bpark?serverTimezone=UTC", "root",
					"Aa123456"); //time zone is UTC
			// Connection conn =
			// DriverManager.getConnection("jdbc:mysql://192.168.3.68/test","root","Root");
			System.out.println("SQL connection succeed");
>>>>>>> AmitBranch
		} catch (SQLException ex) {/* handle any errors */
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
<<<<<<< HEAD
			return null;
=======
>>>>>>> AmitBranch
		}
	}

	/**
	 * This function print in console the order that its num is order_num
	 * 
	 * @param con
	 * @param order_number
	 */
<<<<<<< HEAD
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
=======
	public boolean getOrderByOrderNumber(int order_number) {
		String query = "SELECT * FROM `order` WHERE order_number=?";
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setInt(1, order_number); //replace '?' in query
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				return true;
>>>>>>> AmitBranch
			}
		} catch (Exception e) {
			System.out.println("Error! " + e.getMessage());
		}
<<<<<<< HEAD
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
				newOrder.append(rs.getString("order_date"));
				newOrder.append(", Confirmation: ");
				newOrder.append(rs.getInt("confirmation_code"));
				newOrder.append(", Subscriber:");
				newOrder.append(rs.getInt("subscriber_id"));
				newOrder.append(", Placed:");
				newOrder.append(rs.getString("date_of_placing_an_order"));
				orders.add(newOrder.toString());
=======
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
						rs.getDate("date_of_placing_an_order")); //create new order from order in sql table
				orders.add(newOrder);
>>>>>>> AmitBranch
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
<<<<<<< HEAD
	public static void disconnectFromDB(Connection conn) {
=======
	public void disconnectFromDB() {
>>>>>>> AmitBranch
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
<<<<<<< HEAD
	public static boolean updateParking_space(Connection conn, int update_parking_space, int order_number) {
		String query = "UPDATE `order` SET parking_space=? WHERE order_number=?";
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setInt(1, update_parking_space);
			stmt.setInt(2, order_number);
=======
	public boolean updateParkingSpace(int update_parking_space, int order_number) {
		String query = "UPDATE `order` SET parking_space=? WHERE order_number=?";
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setInt(1, update_parking_space); //replace first '?'
			stmt.setInt(2, order_number); //replace second '?'
>>>>>>> AmitBranch
			stmt.executeUpdate();
		} catch (Exception e) {
			System.out.println("Error! " + e.getMessage());
			return false;
		}
		return true;
	}

<<<<<<< HEAD
	
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
			
=======
	/**
	 * nevigate the update according the field
	 * 
	 * @param orderNumber
	 * @param field
	 * @param newValue
	 * @return true if succeed, else false
	 */
	public int updateOrderField(int orderNumber, String field, String newValue) {
		boolean hasOrder = getOrderByOrderNumber(orderNumber);
		if (hasOrder) {
			if (field.equals("parking_space")) {
				if (updateParkingSpace(Integer.parseInt(newValue), orderNumber)) {
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
		return 6; //no such order num in sql table
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
			stmt.setDate(1, Date.valueOf(newValue)); //replace first '?'
			stmt.setInt(2, order_number); //replace second '?' in query

			stmt.executeUpdate();

>>>>>>> AmitBranch
		} catch (Exception e) {
			System.out.println("Error! " + e.getMessage());
			return false;
		}
		return true;
	}
}

