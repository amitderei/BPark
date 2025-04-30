package server;

import ocsf.server.*;
import java.io.IOException;
import java.sql.Connection;
import DB.DBController;

/**
 * OCSF server that receives client requests and delegates actions to DBController.
 * Works with existing DBController logic including Scanner-based input.
 */
public class PrototypeServer extends AbstractServer {

    private Connection conn;

    public PrototypeServer(int port) {
        super(port);
    }

    /**
     * Called when the server starts. Connects to the database.
     */
    @Override
    protected void serverStarted() {
        conn = DBController.connectToDB();
        System.out.println("Server started on port " + getPort());
    }

    /**
     * Called when the server stops. Disconnects from the database.
     */
    @Override
    protected void serverStopped() {
        DBController.disconnectFromDB(conn);
        System.out.println("Server stopped.");
    }

    /**
     * Handles messages received from a client.
     * Supported operations:
     *   - "getAllOrders": prints all orders to console via DBController
     *   - ["updateOrder", orderNumber, field, newValue]: updates specific field
     */
    @Override
    protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
        try {
            if (msg instanceof String) {
                String command = (String) msg;

                if (command.equals("getAllOrders")) {
                    System.out.println("Client requested all orders:");
                    DBController.getOrders(conn); // outputs to console only
                }

            } else if (msg instanceof Object[]) {
                Object[] data = (Object[]) msg;

                if (data.length == 4 && data[0].equals("updateOrder")) {
                    int orderNumber = (int) data[1];
                    String field = (String) data[2];
                    String newValue = (String) data[3];

                    // Handle update based on field type
                    if (field.equals("parking_space")) {
                        int parking = Integer.parseInt(newValue);
                        DBController.updateParking_space(conn, parking, orderNumber);
                        client.sendToClient("Parking space updated.");
                    } else if (field.equals("order_date")) {
                        // Uses Scanner for input (as implemented in DBController)
                        DBController.updateOrderDateByOrderNumber(conn, orderNumber);
                        client.sendToClient("Order date update initiated (via console).");
                    } else {
                        client.sendToClient("Unsupported field: " + field);
                    }
                }

            } else {
                client.sendToClient("Unknown message format.");
            }

        } catch (IOException e) {
            System.out.println("Communication error: " + e.getMessage());
        } catch (Exception ex) {
            System.out.println("General error: " + ex.getMessage());
        }
    }
}

