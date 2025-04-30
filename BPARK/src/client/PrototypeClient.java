package client;

import ocsf.client.AbstractClient;
import java.io.IOException;
import java.util.ArrayList;

/**
 * OCSF Client that connects to the server and sends/receives messages.
 */
public class PrototypeClient extends AbstractClient {

    private ClientController controller;

    public PrototypeClient(String host, int port, ClientController controller) {
        super(host, port);
        this.controller = controller;
    }

    /**
     * Handles messages received from the server.
     */
    @Override
    protected void handleMessageFromServer(Object msg) {
        if (msg instanceof String) {
            controller.appendOutput((String) msg);
        } else if (msg instanceof ArrayList) {
            controller.displayOrderList((ArrayList<String>) msg);
        }
    }

    /**
     * Sends a request to the server to fetch all orders.
     */
    public void requestAllOrders() {
        try {
            sendToServer("getAllOrders");
        } catch (IOException e) {
            controller.appendOutput("Failed to send request: " + e.getMessage());
        }
    }

    /**
     * Sends an update request to the server.
     * @param orderNumber the order to update
     * @param field field to update ("parking_space" or "order_date")
     * @param value new value to set
     */
    public void updateOrder(int orderNumber, String field, String value) {
        try {
            sendToServer(new Object[] { "updateOrder", orderNumber, field, value });
        } catch (IOException e) {
            controller.appendOutput("Failed to send update: " + e.getMessage());
        }
    }
}
