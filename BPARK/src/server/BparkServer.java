package server;

import ocsf.server.*;
import java.io.IOException;
import java.util.ArrayList;
import DB.DBController;
import common.Order;

/**
 * Represents the server side of the BPARK prototype application.
 * Inherits from AbstractServer provided by OCSF and handles incoming client messages,
 * delegating database actions to DBController.
 */
public class BparkServer extends AbstractServer {

    private DBController db;

    /**
     * Constructs the server with the specified port number.
     * Initializes the database controller.
     *
     * @param port The port on which the server will listen.
     */
    public BparkServer(int port) {
        super(port);
        db = new DBController();
    }

    /**
     * This method is automatically called when the server starts listening for connections.
     * It initializes the database connection and logs the server status.
     */
    @Override
    protected void serverStarted() {
        db.connectToDB();
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
                    ArrayList<Order> orders = db.getAllOrders();
                    client.sendToClient(orders);
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
}
