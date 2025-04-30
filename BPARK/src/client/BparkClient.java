package client;

import common.Order;
import ocsf.client.AbstractClient;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Represents the client-side network handler of the BPARK system.
 * Extends AbstractClient from the OCSF framework to communicate with the server,
 * and passes data to the JavaFX GUI controller.
 */
public class BparkClient extends AbstractClient {

    private BparkClientController controller;

    /**
     * Constructs a new PrototypeClient instance with specified host and port.
     *
     * @param host the IP or hostname of the server.
     * @param port the server's listening port.
     */
    public BparkClient(String host, int port) {
        super(host, port);
    }

    /**
     * Links this client with the GUI controller to allow GUI updates.
     *
     * @param controller the JavaFX controller for updating the interface.
     */
    public void setController(BparkClientController controller) {
        this.controller = controller;
    }

    /**
     * @return the currently assigned GUI controller.
     */
    public BparkClientController getController() {
        return controller;
    }

    /**
     * Handles messages received from the server.
     * - If the message is a String, it is shown as a pop-up alert.
     * - If the message is a list of Order objects, they are passed to the controller for display.
     *
     * @param msg the object received from the server.
     */
    @Override
    protected void handleMessageFromServer(Object msg) {
        if (msg instanceof String) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Server Message");
                alert.setHeaderText(null);
                alert.setContentText(String.valueOf(msg));
                alert.showAndWait();
            });
        } else if (msg instanceof ArrayList<?>) {
            ArrayList<?> list = (ArrayList<?>) msg;
            if (!list.isEmpty() && list.get(0) instanceof Order) {
                ArrayList<Order> orders = (ArrayList<Order>) msg;
                Platform.runLater(() -> {
                    if (controller != null) {
                        controller.displayOrders(orders);
                    }
                });
            }
        }
    }

    /**
     * Sends a request to the server to fetch all orders.
     */
    public void requestAllOrders() {
        try {
            sendToServer("getAllOrders");
        } catch (IOException e) {
            System.out.println("Error sending request: " + e.getMessage());
        }
    }

    /**
     * Sends a request to the server to update a specific field in an order.
     *
     * @param orderNumber the ID of the order to update.
     * @param field the name of the field to update.
     * @param newValue the new value to set.
     */
    public void updateOrder(int orderNumber, String field, String newValue) {
        try {
            sendToServer(new Object[]{"updateOrder", orderNumber, field, newValue});
        } catch (IOException e) {
            System.out.println("Error sending update: " + e.getMessage());
        }
    }
}

