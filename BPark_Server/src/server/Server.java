package server;

import ocsf.server.*;
import java.io.IOException;
import java.util.ArrayList;

import common.Order;
import common.ServerResponse;
import db.DBController;

/**
 * Represents the server side of the BPARK prototype. Extends AbstractServer
 * from OCSF to handle client communication.
 */
public class Server extends AbstractServer {

	private DBController db;

	/**
	 * Constructs the server with the specified port and initializes DBController
	 * (Singleton).
	 *
	 * @param port The port the server will listen on.
	 */
	public Server(int port) {
		super(port); // Initialize AbstractServer with port
		db = DBController.getInstance(); // Get singleton DB controller instance
	}

	/**
	 * Called automatically when the server starts listening. Initializes the DB
	 * connection and logs the startup.
	 */
	@Override
	protected void serverStarted() {
		db.connectToDB(); // Connect to the database once at startup
		System.out.println("Server started on port " + getPort()); // Log server status
	}

	/**
	 * Handles incoming messages from clients. Supports two types of messages: 1.
	 * String command "getAllOrders" - returns all orders in the system. 2. Object[]
	 * for updating a specific field in an order.
	 *
	 * @param msg    The message sent by the client.
	 * @param client The connection instance representing the client.
	 */
	@Override
	protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
		try {
			// Message is an Object[] indicating an update\watch specific order
			// request
			System.out.println("server1");
			if (msg instanceof Object[]) {
				Object[] data = (Object[]) msg;
				if (data.length == 1 && "disconnect".equals(data[0])) {
					try {
						String clientIP = client.getInetAddress().getHostAddress();
						String clientHost = client.getInetAddress().getHostName();
						System.out.println("Client requested disconnect from: " + clientHost + " (" + clientIP + ")");
					} catch (Exception e) {
						System.out.println("Client requested disconnect, but could not retrieve client info.");
					}
					return; // Stop processing further
				}

				// If command is to get all orders
				else if (data.length == 1 && "getAllOrders".equals(data[0])) {
					System.out.println("server2");
					ArrayList<Order> orders = db.getAllOrders();

					if (orders.isEmpty()) {
						// No orders found – return failure message
						client.sendToClient(new ServerResponse(false, null, "There are no orders in the system"));
					} else {
						// Orders found – return them to client
						client.sendToClient(new ServerResponse(true, orders, "Orders are displayed successfully."));
					}
				}
				// watch specific order request. Excepted format:{"getOrder", orderNumber}
				else if (data.length == 2 && "getOrder".equals(data[0])) {
					int orderNumber = (int) data[1];

					ArrayList<Order> list = db.orderExists(orderNumber);

					if (list.isEmpty()) {
						// No order found – return failure message
						client.sendToClient(new ServerResponse(false, null,
								"There are no order with this order number in the system"));
					} else {
						// Order found – return it to client
						client.sendToClient(new ServerResponse(true, list, "Order is displayed successfully."));
					}
				}

				// Expected format: {"updateOrder", orderNumber, field, newValue}
				else if (data.length == 4 && "updateOrder".equals(data[0])) {
					int orderNumber = (int) data[1];
					String field = (String) data[2];
					String newValue = (String) data[3];

					int success;

					try {
						// Perform the update via DBController
						success = db.updateOrderField(orderNumber, field, newValue);
					} catch (Exception ex) {
						client.sendToClient(new ServerResponse(false, null, "Update failed: " + ex.getMessage()));
						return;
					}

					// Respond based on result code from update
					switch (success) {
					case 1:
						// Parking space updated
						client.sendToClient(new ServerResponse(true, db.getAllOrders(),
								"Parking space was successfully changed for the order."));
						break;
					case 2:
						client.sendToClient(new ServerResponse(false, null,
								"Parking space was unsuccessfully changed for the order."));
						break;
					case 3:
						// Order date updated
						client.sendToClient(new ServerResponse(true, db.getAllOrders(),
								"Order date was successfully changed for the order."));
						break;
					case 4:
						client.sendToClient(new ServerResponse(false, null,
								"order_date cannot be before date_of_placing_an_order."));
						break;
					case 5:
						client.sendToClient(new ServerResponse(false, null,
								"Order date was unsuccessfully changed for the order."));
						break;
					case 6:
						client.sendToClient(
								new ServerResponse(false, null, "This order number does not exist in the system."));
						break;
					case 7:
						client.sendToClient(new ServerResponse(false, null, "order_date cannot be in the past."));
						break;
					}
				}
			}
		} catch (IOException e) {
			// Handle unexpected client communication failure
			System.out.println("Client communication error: " + e.getMessage());
		}
	}

	/**
	 * Logs the IP address and hostname of a newly connected client.
	 *
	 * @param client The client that connected.
	 */
	@Override
	protected void clientConnected(ConnectionToClient client) {
		try {
			String clientIP = client.getInetAddress().getHostAddress();
			String clientHost = client.getInetAddress().getHostName();
			System.out.println("Client connected from: " + clientHost + " (" + clientIP + ")");
		} catch (Exception e) {
			System.out.println("Could not retrieve client info: " + e.getMessage());
		}
	}

	/**
	 * Called automatically when a client disconnects from the server.
	 *
	 * @param client The client that disconnected.
	 */
	@Override
	protected void clientDisconnected(ConnectionToClient client) {
		try {
			String clientIP = client.getInetAddress().getHostAddress();
			String clientHost = client.getInetAddress().getHostName();
			System.out.println("Client disconnected from: " + clientHost + " (" + clientIP + ")");
		} catch (Exception e) {
			System.out.println("Could not retrieve disconnected client info: " + e.getMessage());
		}
	}

	/**
	 * Returns a list of all currently connected clients with host and IP address.
	 * Iterates over the client connections, casting each to ConnectionToClient.
	 *
	 * @return A list of strings representing each connected client's host and IP.
	 */
	public ArrayList<String> getConnectedClientInfoList() {
		ArrayList<String> connectedClients = new ArrayList<>();

		// Iterate over all client threads and cast them to ConnectionToClient
		for (Thread t : this.getClientConnections()) {
			if (t instanceof ConnectionToClient client) {
				try {
					String clientIP = client.getInetAddress().getHostAddress();
					String clientHost = client.getInetAddress().getHostName();
					connectedClients.add("Host: " + clientHost + " (" + clientIP + ")");
				} catch (Exception e) {
					connectedClients.add("Unknown client");
				}
			} else {
				connectedClients.add("Unknown connection type");
			}
		}

		return connectedClients;
	}

}
