package server;

import ocsf.server.*;
import java.io.IOException;
import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;

import common.Order;
import common.ServerResponse;
import common.User;
import db.DBController;

/**
 * Represents the BPARK server.
 * Handles client connections and dispatches requests using OCSF.
 */
public class Server extends AbstractServer {

    /** Singleton DB controller used for all database operations */
    private DBController db;

    /**
     * Constructs a new server instance.
     *
     * @param port the TCP port the server will listen on
     */
    public Server(int port) {
        super(port);
        db = DBController.getInstance();
    }

    /**
     * Called automatically when the server starts.
     * Connects to the database.
     */
    @Override
    protected void serverStarted() {
        db.connectToDB();
        System.out.println("Server started on port " + getPort());
    }

    /**
     * Processes incoming messages from connected clients.
     * Each message is expected to be an Object[] with an action string as the first element.
     * Supported actions include login, order management, vehicle pickup, and parking availability.
     *
     * @param msg    the message received from the client
     * @param client the sending client
     */
    @Override
    protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
        try {
            if (msg instanceof Object[] data) {

                // Disconnect request
                if (data.length == 1 && "disconnect".equals(data[0])) {
                    logClientDisconnect(client);
                    return;
                }

                // Login request: ["login", username, password]
                else if (data.length == 3 && "login".equals(data[0])) {
                    handleLogin(data, client);
                    return;
                }

                // Get all orders: ["getAllOrders"]
                else if (data.length == 1 && "getAllOrders".equals(data[0])) {
                    ArrayList<Order> orders = db.getAllOrders();
                    if (orders.isEmpty()) {
                        client.sendToClient(new ServerResponse(false, null,
                                "There are no orders in the system"));
                    } else {
                        client.sendToClient(new ServerResponse(true, orders,
                                "Orders are displayed successfully."));
                    }
                    return;
                }

                // Get single order: ["getOrder", orderNumber]
                else if (data.length == 2 && "getOrder".equals(data[0])) {
                    int orderNumber = (int) data[1];
                    ArrayList<Order> list = db.orderExists(orderNumber);
                    if (list.isEmpty()) {
                        client.sendToClient(new ServerResponse(false, null,
                                "No order with this number exists."));
                    } else {
                        client.sendToClient(new ServerResponse(true, list,
                                "Order displayed successfully."));
                    }
                    return;
                }

                // Update order: ["updateOrder", orderNumber, field, newValue]
                else if (data.length == 4 && "updateOrder".equals(data[0])) {
                    handleUpdateOrder(data, client);
                    return;
                }

                // Validate subscriber: ["validateSubscriber", subscriberCode]
                else if (data.length == 2 && "validateSubscriber".equals(data[0])) {
                    int subscriberCode = (int) data[1];
                    boolean exists = db.subscriberExists(subscriberCode);
                    if (exists) {
                        client.sendToClient(new ServerResponse(true, null,
                                "Subscriber verified successfully."));
                    } else {
                        client.sendToClient(new ServerResponse(false, null,
                                "Subscriber code not found."));
                    }
                    return;
                }

                // Collect car: ["collectCar", subscriberCode, parkingCode]
                else if (data.length == 3 && "collectCar".equals(data[0])) {
                    int subCode = (int) data[1];
                    int parkCode = (int) data[2];
                    try {
                        ServerResponse response = db.handleVehiclePickup(subCode, parkCode);
                        client.sendToClient(response);
                    } catch (Exception e) {
                        client.sendToClient(new ServerResponse(false, null,
                                "An error occurred while collecting the vehicle."));
                        System.err.println("Error: collectCar - " + e.getMessage());
                    }
                    return;
                }

                // Extend parking: ["extendParking", subscriberCode]
                else if (data.length == 2 && "extendParking".equals(data[0])) {
                    int subCode = (int) data[1];
                    try {
                        boolean success = db.updateWasExtended(subCode);
                        if (success) {
                            client.sendToClient(new ServerResponse(true, null,
                                    "Parking was successfully extended."));
                        } else {
                            client.sendToClient(new ServerResponse(false, null,
                                    "No active event found. Extension failed."));
                        }
                    } catch (Exception e) {
                        client.sendToClient(new ServerResponse(false, null,
                                "An error occurred while extending parking."));
                        System.err.println("Error: extendParking - " + e.getMessage());
                    }
                    return;
                }

                // Resend parking code: ["sendLostCode", subscriberCode]
                else if (data.length == 2 && "sendLostCode".equals(data[0])) {
                    int subCode = (int) data[1];
                    try {
                        ServerResponse response = db.sendParkingCodeToSubscriber(subCode);
                        client.sendToClient(response);
                    } catch (Exception e) {
                        client.sendToClient(new ServerResponse(false, null,
                                "An error occurred while sending your parking code."));
                        System.err.println("Error: sendLostCode - " + e.getMessage());
                    }
                    return;
                }

                // Check parking availability: ["CheckParkingAvailability"]. ** Author - Ravid **
                else if (data.length == 1 && "CheckParkingAvailability".equals(data[0])) {
                    int count = db.countAvailableSpots();
                    client.sendToClient(new ServerResponse(true, count, "Available spots: " + count));
                    return;
                }
                
                else if(data.length==3 && "checkAvailability".equals(data[0])) {
                    System.out.println("checkAvailability-server");
                    boolean possible=db.parkingSpaceCheckingForNewOrder((Date) data[1],(Time) data[2]);
                    if (possible) {
                        client.sendToClient(new ServerResponse(true, null, "Can make resarvation"));
                    }
                    else {
                        client.sendToClient(new ServerResponse(false, null, "Can't make resarvation"));
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("Client communication error: " + e.getMessage());
        }
    }


    /**
     * Handles a login request.
     *
     * @param data   the message parts: ["login", username, password]
     * @param client the requesting client
     */
    private void handleLogin(Object[] data, ConnectionToClient client) {
        String username = (String) data[1];
        String password = (String) data[2];

        System.out.println("[SERVER] Login attempt from username: " + username);
        User user = db.authenticateUser(username, password);
        System.out.println("[DEBUG] DB returned user: " + user);

        try {
            if (user != null) {
                client.sendToClient(new ServerResponse(true, user, "Login successful"));
            } else {
                client.sendToClient(new ServerResponse(false, null, "Invalid username or password."));
            }
        } catch (IOException e) {
            System.err.println("Error sending login response: " + e.getMessage());
        }
    }

    /**
     * Handles an order update request.
     *
     * @param data   the message parts: ["updateOrder", orderNumber, field, newValue]
     * @param client the requesting client
     */
    private void handleUpdateOrder(Object[] data, ConnectionToClient client) {
        int orderNumber = (int) data[1];
        String field = (String) data[2];
        String newValue = (String) data[3];

        try {
            int success = db.updateOrderField(orderNumber, field, newValue);
            switch (success) {
                case 1 -> client.sendToClient(new ServerResponse(true, db.getAllOrders(),
                        "Parking space updated successfully."));
                case 2 -> client.sendToClient(new ServerResponse(false, null,
                        "Failed to update parking space."));
                case 3 -> client.sendToClient(new ServerResponse(true, db.getAllOrders(),
                        "Order date updated successfully."));
                case 4 -> client.sendToClient(new ServerResponse(false, null,
                        "Order date cannot be before placement date."));
                case 5 -> client.sendToClient(new ServerResponse(false, null,
                        "Failed to update order date."));
                case 6 -> client.sendToClient(new ServerResponse(false, null,
                        "Order number does not exist."));
                case 7 -> client.sendToClient(new ServerResponse(false, null,
                        "Order date cannot be in the past."));
            }

        } catch (Exception ex) {
            try {
                client.sendToClient(new ServerResponse(false, null, "Update failed: " + ex.getMessage()));
            } catch (IOException e) {
                System.err.println("Error sending update response: " + e.getMessage());
            }
        }
    }

    /**
     * Logs new client connections.
     *
     * @param client the connecting client
     */
    @Override
    protected void clientConnected(ConnectionToClient client) {
        try {
            String ip = client.getInetAddress().getHostAddress();
            String host = client.getInetAddress().getHostName();
            System.out.println("Client connected from: " + host + " (" + ip + ")");
        } catch (Exception e) {
            System.out.println("Could not retrieve client info: " + e.getMessage());
        }
    }

    /**
     * Logs when a client disconnects.
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
            String ip = client.getInetAddress().getHostAddress();
            String host = client.getInetAddress().getHostName();
            System.out.println("Client disconnected from: " + host + " (" + ip + ")");
        } catch (Exception e) {
            System.out.println("Could not retrieve disconnected client info: " + e.getMessage());
        }
    }

    /**
     * Returns a list of all connected client hostnames and IPs.
     *
     * @return list of client connection strings
     */
    public ArrayList<String> getConnectedClientInfoList() {
        ArrayList<String> list = new ArrayList<>();

        for (Thread t : this.getClientConnections()) {
            if (t instanceof ConnectionToClient client) {
                try {
                    String ip = client.getInetAddress().getHostAddress();
                    String host = client.getInetAddress().getHostName();
                    list.add("Host: " + host + " (" + ip + ")");
                } catch (Exception e) {
                    list.add("Unknown client");
                }
            } else {
                list.add("Unknown connection type");
            }
        }

        return list;
    }
}


