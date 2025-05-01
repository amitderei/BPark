package server;

import ocsf.server.*;
import java.io.IOException;
import java.util.ArrayList;
import DB.DBController;
import common.Order;

/**
 * Represents the server side of the BPARK prototype application.
 * Inherits from AbstractServer provided by OCSF and handles incoming client messages,
 * delegating database actions to DBController (Singleton).
 */
public class BparkServer extends AbstractServer {

    private DBController db;

    /**
     * Constructs the server with the specified port number.
     * Initializes the database controller using Singleton pattern.
     *
     * @param port The port on which the server will listen.
     */
    public BparkServer(int port) {
        super(port);
        db = DBController.getInstance(); // Singleton: ensures single access point to DB
    }

    /**
     * This method is automatically called when the server starts listening for connections.
     * It initializes the database connection and logs the server status.
     */
    @Override
    protected void serverStarted() {
        db.connectToDB(); // One-time connection to MySQL database
        System.out.println("Server started on port " + getPort());
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
            if (msg instanceof String) {
                String command = (String) msg;

                if (command.equals("getAllOrders")) {
                    ArrayList<Order> orders = db.getAllOrders();       // Fetch from DB
                    client.sendToClient(orders);                      // Send to client
                }

            } else if (msg instanceof Object[]) {
                Object[] data = (Object[]) msg;

                if (data.length == 4 && data[0].equals("updateOrder")) {
                    int orderNumber = (int) data[1];
                    String field = (String) data[2];
                    String newValue = (String) data[3];

                    boolean success = db.updateOrderField(orderNumber, field, newValue);
                    client.sendToClient(success ? "Order updated." : "Update failed.");
                }
            }
        } catch (IOException e) {
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
            String clientIP = client.getInetAddress().getHostAddress();   // IP address of the client
            String clientHost = client.getInetAddress().getHostName();    // Host name (computer name)
            System.out.println("Client connected from: " + clientHost + " (" + clientIP + ")");
        } catch (Exception e) {
            System.out.println("Could not retrieve client info: " + e.getMessage());
        }
    }

}

