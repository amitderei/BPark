package client;

import common.Order;
import common.ServerResponse;
import common.User;
import controllers.LoginController;
import controllers.OrderViewController;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import ocsf.client.AbstractClient;
import ui.UiUtils;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Handles network communication between the BPARK client and server.
 * Wraps requests and responses using OCSF and updates the assigned controller.
 */
public class ClientController extends AbstractClient {

    private OrderViewController controller;
    private LoginController loginController;

    /**
     * Constructs a new Client instance with the specified server address and port.
     *
     * @param host the server's hostname or IP address
     * @param port the server's listening port
     */
    public ClientController(String host, int port) {
        super(host, port);
    }

    /**
     * Assigns the active GUI controller for use in updating the interface.
     *
     * @param orderViewController the JavaFX controller
     */
    public void setController(OrderViewController orderViewController) {
        this.controller = orderViewController;
    }

    /**
     * @return the currently assigned controller
     */
    public OrderViewController getController() {
        return controller;
    }
    
    /**
     * Assigns the LoginController for handling login responses.
     *
     * @param loginController the login controller instance
     */
    public void setLoginController(LoginController loginController) {
        this.loginController = loginController;
    }


    /**
     * Processes messages received from the server and updates the GUI accordingly.
     *
     * @param msg the incoming server message (expected to be a ServerResponse or String)
     */
    @Override
    protected void handleMessageFromServer(Object msg) {
        // Handle special shutdown message
        if (msg instanceof String && msg.equals("server_shutdown")) {
            Platform.runLater(() -> {
                UiUtils.showAlert("Server Shutdown", "The server is shutting down. The application will now close.",
                        Alert.AlertType.INFORMATION);
                System.exit(0);
            });
            return;
        }

        // Handle unexpected message type
        if (!(msg instanceof ServerResponse response)) {
            System.err.println("Received unexpected message type from server: " + msg.getClass());
            return;
        }

        Platform.runLater(() -> {
            // Handle login success
            if (response.isSucceed() && response.getData() instanceof User user) {
                if (loginController != null) {
                    loginController.handleLoginSuccess(user);
                }
                return;
            }

            // Handle login failure
            if (!response.isSucceed() && loginController != null && response.getMsg().toLowerCase().contains("invalid")) {
                loginController.handleLoginFailure(response.getMsg());
                return;
            }

            // Generic UI status update (for order controllers)
            if (controller != null) {
                UiUtils.setStatus(controller.getStatusLabel(), response.getMsg(), response.isSucceed());
            }

            // Show failure popup
            if (!response.isSucceed()) {
                UiUtils.showAlert("System Message", response.getMsg(), Alert.AlertType.ERROR);
            }

            // Handle list of orders
            if (response.isSucceed() && response.getData() instanceof ArrayList<?> dataList && !dataList.isEmpty()) {
                if (dataList.get(0) instanceof Order) {
                    @SuppressWarnings("unchecked")
                    ArrayList<Order> orders = (ArrayList<Order>) dataList;
                    if (controller != null) {
                        controller.displayOrders(orders);
                    }
                }
            }
        });
    }


    /**
     * Sends a request to retrieve all orders from the server.
     */
    public void requestAllOrders() {
        try {
            sendToServer(new Object[]{"getAllOrders"});
            if (controller != null)
                UiUtils.setStatus(controller.getStatusLabel(), "Orders loaded successfully", true);
        } catch (IOException e) {
            System.err.println("Failed to send 'getAllOrders' request: " + e.getMessage());
        }
    }

    /**
     * Sends a request to retrieve a specific order by its ID.
     *
     * @param orderNumber the ID of the order
     */
    public void requestOrderByOrderNum(int orderNumber) {
        try {
            sendToServer(new Object[]{"getOrder", orderNumber});
        } catch (IOException e) {
            System.err.println("Failed to send 'getOrder' request: " + e.getMessage());
        }
    }

    /**
     * Sends a request to update a field in a specific order.
     *
     * @param orderNumber the order ID
     * @param field       the field to update
     * @param newValue    the new value to apply
     */
    public void updateOrder(int orderNumber, String field, String newValue) {
        try {
            sendToServer(new Object[]{"updateOrder", orderNumber, field, newValue});
        } catch (IOException e) {
            System.err.println("Failed to send 'updateOrder' request: " + e.getMessage());
        }
    }

    /**
     * Sends a request to add a new order to the system.
     *
     * @param newOrder the new Order object to insert
     */
    public void addNewOrder(Order newOrder) {
        try {
            sendToServer(new Object[]{"addNewOrder", newOrder});
        } catch (IOException e) {
            System.err.println("Failed to send 'addNewOrder' request: " + e.getMessage());
        }
    }
}
