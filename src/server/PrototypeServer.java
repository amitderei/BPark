package server;

import ocsf.server.*;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;

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
                    ArrayList<String> orders = DBController.getOrders(conn);
                    client.sendToClient(orders);
                }

            } else if (msg instanceof Object[]) {
                Object[] data = (Object[]) msg;

                if (data.length == 4 && data[0].equals("updateOrder")) {
                    int orderNumber = (int) data[1];
                    String field = (String) data[2];
                    String newValue = (String) data[3];

                    
                    if (((String)field).equals("parking_space")) {
                    	boolean success=DBController.updateParking_space(conn,Integer.parseInt(newValue) , orderNumber);
                    	client.sendToClient(success ? "Order updated." : "Update failed.");
                    }
                    else if (((String)field).equals("date")) {
                    	boolean success=DBController.updateOrderDateByOrderNumber(conn, orderNumber, newValue);
                    	client.sendToClient(success ? "Order updated." : "Update failed.");
                    }
                    
                }
            }
        } catch (IOException e) {
            System.out.println("Client communication error: " + e.getMessage());
        }
         catch (Exception ex) {
            System.out.println("General error: " + ex.getMessage());
        }
    }
}

