package client;

import ocsf.client.AbstractClient;
import java.io.IOException;
import java.util.ArrayList;

/**
 * PrototypeClient connects to the server and prints responses to console.
 */
public class PrototypeClient extends AbstractClient {

    public PrototypeClient(String host, int port) {
        super(host, port);
    }

    /**
     * Handles messages received from the server.
     */
    @Override
    protected void handleMessageFromServer(Object msg) {
        if (msg instanceof String) {
            System.out.println("[SERVER] " + msg);
        } else if (msg instanceof ArrayList) {
            System.out.println("=== Orders ===");
            ArrayList<String> orders = (ArrayList<String>) msg;
            for (String order : orders) {
                System.out.println(order);
            }
        }
    }

    /**
     * Request all orders from the server.
     */
    public void requestAllOrders() {
        try {
            sendToServer("getAllOrders");
        } catch (IOException e) {
            System.out.println("Error sending request: " + e.getMessage());
        }
    }

    /**
     * Request to update a specific field in an order.
     */
    public void updateOrder(int orderNumber, String field, String newValue) {
        try {
            sendToServer(new Object[]{"updateOrder", orderNumber, field, newValue});
        } catch (IOException e) {
            System.out.println("Error sending update: " + e.getMessage());
        }
    }
}
