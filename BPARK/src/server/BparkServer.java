package server;

import ocsf.server.*;
import java.io.IOException;
import java.util.ArrayList;
import DB.DBController;
import common.Order;
import common.ServerResponse;

/**
 * Represents the server side of the BPARK prototype.
 * Extends AbstractServer from OCSF to handle client communication.
 */
public class BparkServer extends AbstractServer {

    private DBController db;

    /**
     * Constructs the server with the specified port and initializes DBController (Singleton).
     *
     * @param port The port the server will listen on.
     */
    public BparkServer(int port) {
        super(port);                           // Initialize AbstractServer with port
        db = DBController.getInstance();       // Get singleton DB controller instance
    }

    /**
     * Called automatically when the server starts listening.
     * Initializes the DB connection and logs the startup.
     */
    @Override
    protected void serverStarted() {
        db.connectToDB(); // Connect to the database once at startup
        System.out.println("Server started on port " + getPort()); // Log server status
    }

    /**
     * Handles incoming messages from clients.
     * Supports two types of messages:
     *   1. String command "getAllOrders" - returns all orders in the system.
     *   2. Object[] for updating a specific field in an order.
     *
     * @param msg    The message sent by the client.
     * @param client The connection instance representing the client.
     */
    @Override
    protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
    	try {
    	    // Case 1: Message is a String command
    	    if (msg instanceof String) {
    	        String command = (String) msg;

    	        // If command is to get all orders
    	        if (command.equals("getAllOrders")) {
    	            ArrayList<Order> orders = db.getAllOrders();

    	            if (orders.isEmpty()) {
    	                // No orders found – return failure message
    	                client.sendToClient(new ServerResponse(false, null, "There are no orders in the system"));
    	            } else {
    	                // Orders found – return them to client
    	                client.sendToClient(new ServerResponse(true, orders, "Orders are displayed successfully."));
    	            }
    	        }

    	    // Case 2: Message is an Object[] indicating an update request
    	    } else if (msg instanceof Object[]) {
    	        Object[] data = (Object[]) msg;

    	        // Expected format: {"updateOrder", orderNumber, field, newValue}
    	        if (data.length == 4 && "updateOrder".equals(data[0])) {
    	            int orderNumber = (int) data[1];
    	            String field = (String) data[2];
    	            String newValue = (String) data[3];

    	            // Perform the update via DBController
    	            int success = db.updateOrderField(orderNumber, field, newValue);

    	            // Respond based on result code from update
    	            switch (success) {
    	                case 1:
    	                    // Parking space updated
    	                    client.sendToClient(new ServerResponse(true, db.getAllOrders(), "Parking space was successfully changed for the order."));
    	                    break;
    	                case 2:
    	                    client.sendToClient(new ServerResponse(false, null, "Parking space was unsuccessfully changed for the order."));
    	                    break;
    	                case 3:
    	                    // Order date updated
    	                    client.sendToClient(new ServerResponse(true, db.getAllOrders(), "Order date was successfully changed for the order."));
    	                    break;
    	                case 4:
    	                    client.sendToClient(new ServerResponse(false, null, "Order date was unsuccessfully changed for the order."));
    	                    break;
    	                case 5:
    	                    client.sendToClient(new ServerResponse(false, null, "The field entered is incorrect."));
    	                    break;
    	                case 6:
    	                    client.sendToClient(new ServerResponse(false, null, "This order number does not exist in the system."));
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
     * This method is automatically called when a client successfully connects to the server.
     * It retrieves and prints the client's hostname and IP address for logging purposes.
     * This is used to fulfill the assignment requirement of displaying client network information.
     *
     * @param client The client that just connected to the server.
     */
    @Override
    protected void clientConnected(ConnectionToClient client) {
        try {
            String clientIP = client.getInetAddress().getHostAddress();   // Get the client's IP address
            String clientHost = client.getInetAddress().getHostName();    // Get the client's hostname
            System.out.println("Client connected from: " + clientHost + " (" + clientIP + ")");
        } catch (Exception e) {
            System.out.println("Could not retrieve client info: " + e.getMessage());
        }
    }

}

