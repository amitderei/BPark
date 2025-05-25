package server;

import ocsf.server.*;
import java.io.IOException;
import java.util.ArrayList;

import common.Order;
import common.ServerResponse;
import common.User;
import db.DBController;

/**
 * Represents the server side of the BPARK prototype.
 * Handles client connections and request processing using the OCSF framework.
 */
public class Server extends AbstractServer {

    /** Singleton database controller used for all DB operations */
    private DBController db;

    /**
     * Constructs a new server on the specified port.
     *
     * @param port the port number the server will listen on
     */
    public Server(int port) {
        super(port);
        db = DBController.getInstance();
    }

    /**
     * Called automatically when the server starts.
     * Establishes the database connection.
     */
    @Override
    protected void serverStarted() {
        db.connectToDB();
        System.out.println("Server started on port " + getPort());
    }

    /**
     * Handles all incoming messages from connected clients.
     *
     * @param msg    the message sent by the client
     * @param client the client connection instance
     */
    @Override
    protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
        try {
            if (msg instanceof Object[] data) {

                // Handle client disconnect request
                if (data.length == 1 && "disconnect".equals(data[0])) {
                    logClientDisconnect(client);
                    return;
                }

                // Handle login request: ["login", username, password]
                else if (data.length == 3 && "login".equals(data[0])) {
                    String username = (String) data[1];
                    String password = (String) data[2];

                    System.out.println("[SERVER] Login attempt from username: " + username);

                    User user = db.authenticateUser(username, password);
                    System.out.println("[DEBUG] DB returned user: " + user);


                    if (user != null) {
                        // Successful login – send User object back
                    	System.out.println("[SERVER] Sending successful login response to client...");
                        client.sendToClient(new ServerResponse(true, user, "Login successful"));
                    } else {
                        // Login failed – send generic error
                        client.sendToClient(new ServerResponse(false, null, "Invalid username or password."));
                    }
                }

                // Handle "getAllOrders" request
                else if (data.length == 1 && "getAllOrders".equals(data[0])) {
                    ArrayList<Order> orders = db.getAllOrders();

                    if (orders.isEmpty()) {
                        client.sendToClient(new ServerResponse(false, null, "There are no orders in the system"));
                    } else {
                        client.sendToClient(new ServerResponse(true, orders, "Orders are displayed successfully."));
                    }
                }

                // Handle request for specific order: ["getOrder", orderNumber]
                else if (data.length == 2 && "getOrder".equals(data[0])) {
                    int orderNumber = (int) data[1];
                    ArrayList<Order> list = db.orderExists(orderNumber);

                    if (list.isEmpty()) {
                        client.sendToClient(new ServerResponse(false, null,
                                "There are no order with this order number in the system"));
                    } else {
                        client.sendToClient(new ServerResponse(true, list, "Order is displayed successfully."));
                    }
                }

                // Handle update request: ["updateOrder", orderNumber, field, newValue]
                else if (data.length == 4 && "updateOrder".equals(data[0])) {
                    int orderNumber = (int) data[1];
                    String field = (String) data[2];
                    String newValue = (String) data[3];

                    try {
                        int success = db.updateOrderField(orderNumber, field, newValue);

                        switch (success) {
                            case 1 -> client.sendToClient(new ServerResponse(true, db.getAllOrders(),
                                    "Parking space was successfully changed for the order."));
                            case 2 -> client.sendToClient(new ServerResponse(false, null,
                                    "Parking space was unsuccessfully changed for the order."));
                            case 3 -> client.sendToClient(new ServerResponse(true, db.getAllOrders(),
                                    "Order date was successfully changed for the order."));
                            case 4 -> client.sendToClient(new ServerResponse(false, null,
                                    "order_date cannot be before date_of_placing_an_order."));
                            case 5 -> client.sendToClient(new ServerResponse(false, null,
                                    "Order date was unsuccessfully changed for the order."));
                            case 6 -> client.sendToClient(new ServerResponse(false, null,
                                    "This order number does not exist in the system."));
                            case 7 -> client.sendToClient(new ServerResponse(false, null,
                                    "order_date cannot be in the past."));
                        }

                    } catch (Exception ex) {
                        client.sendToClient(new ServerResponse(false, null, "Update failed: " + ex.getMessage()));
                    }
                }
            }

        } catch (IOException e) {
            System.out.println("Client communication error: " + e.getMessage());
        }
    }

    /**
     * Logs the IP and host of a newly connected client.
     *
     * @param client the connecting client
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
     * Logs disconnection of a client.
     *
     * @param client the disconnecting client
     */
    @Override
    protected void clientDisconnected(ConnectionToClient client) {
        logClientDisconnect(client);
    }

    /**
     * Logs the disconnection of a client.
     *
     * @param client the client to log
     */
    private void logClientDisconnect(ConnectionToClient client) {
        try {
            String clientIP = client.getInetAddress().getHostAddress();
            String clientHost = client.getInetAddress().getHostName();
            System.out.println("Client disconnected from: " + clientHost + " (" + clientIP + ")");
        } catch (Exception e) {
            System.out.println("Could not retrieve disconnected client info: " + e.getMessage());
        }
    }

    /**
     * Returns a list of all currently connected clients (host and IP).
     *
     * @return list of strings describing connected clients
     */
    public ArrayList<String> getConnectedClientInfoList() {
        ArrayList<String> connectedClients = new ArrayList<>();

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

