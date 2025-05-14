package client;

import common.*;
import ocsf.client.AbstractClient;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Represents the client-side network handler of the BPARK system. Extends
 * AbstractClient from the OCSF framework to communicate with the server, and
 * passes data to the JavaFX GUI controller.
 */
public class Client extends AbstractClient {

	private ClientController controller; // Reference to the client's GUI controller

	/**
	 * Constructs a new Client instance with the specified server address and port.
	 * Initializes the connection parameters but does not open the connection yet.
	 *
	 * @param host the server's hostname or IP address
	 * @param port the server's listening port
	 */
	public Client(String host, int port) {
		super(host, port); // Passes host and port to the parent class (AbstractClient)
	}

	/**
	 * Links this client with the GUI controller to allow GUI updates.
	 *
	 * @param controller the JavaFX controller for updating the interface.
	 */
	public void setController(ClientController controller) {
		this.controller = controller;
	}

	/**
	 * @return the currently assigned GUI controller.
	 */
	public ClientController getController() {
		return controller;
	}

	/**
	 * Processes a response received from the server. - Shows a pop-up message to
	 * the user indicating success or failure. - If the response contains a list of
	 * Order objects, updates the orders table in the GUI. - If the response is
	 * invalid or does not contain relevant data, no table update occurs.
	 *
	 * @param msg the server response, expected to be of type ServerResponse
	 */
	@Override
	protected void handleMessageFromServer(Object msg) {
		// Handle server shutdown notification
		if (msg instanceof String && msg.equals("server_shutdown")) {
		    Platform.runLater(() -> {
		        Alert alert = new Alert(Alert.AlertType.INFORMATION);
		        alert.setTitle("Server Shutdown");
		        alert.setHeaderText("The server is shutting down.");
		        alert.setContentText("The application will now close.");
		        alert.showAndWait();
		        System.exit(0);
		    });
		    return;
		}
		
		// Validate that the received message is indeed a ServerResponse
		if (!(msg instanceof ServerResponse)) {
			System.err.println("Received unexpected message type from server: " + msg.getClass());
			return; // Exit early if message is not as expected
		}

		// Safely cast the message to ServerResponse
		ServerResponse response = (ServerResponse) msg;

		// Ensure that all GUI updates are executed on the JavaFX Application Thread
		Platform.runLater(() -> {
			// Update the status label in all cases
			if (controller != null) {
				controller.showStatus(response.getMsg(), response.isSucceed());
			}

			// Show pop-up only if the response is a failure
			if (!response.isSucceed()) {
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setTitle("System Message");
				alert.setContentText(response.getMsg());
				alert.showAndWait();
			}

			// Proceed to update the table only if the response is successful and contains
			// data
			if (response.isSucceed() && response.getData() instanceof ArrayList<?> dataList && !dataList.isEmpty()) {
				// Check if the first element is an Order (basic type-safety validation)
				if (dataList.get(0) instanceof Order) {
					@SuppressWarnings("unchecked")
					ArrayList<Order> orders = (ArrayList<Order>) dataList;
					controller.showStatus(response.getMsg(), true);

					// Update the GUI table if a controller is linked to this client
					if (controller != null) {
						controller.displayOrders(orders);
					}
				}
			}
		});
	}

	/**
	 * Sends a request to the server asking for all existing orders. Sends the
	 * command "getAllOrders" to trigger data retrieval on the server side.
	 */
	public void requestAllOrders() {
		try {
			// Send a command to the server requesting all orders
			sendToServer("getAllOrders");
			controller.showStatus("Orders loaded successfully", true);
		} catch (IOException e) {
			// Log the error if the message could not be sent to the server
			System.err.println("Failed to send 'getAllOrders' request to server: " + e.getMessage());
		}
	}

	/**
	 * Sends a request to the server asking for specific order. Sends the command
	 * "getOrder" and the order numberץ
	 * 
	 * @param orderNumber the ID of the order to watch
	 */
	public void requestOrderByOrderNum(int orderNumber) {
		try {
			// Prepare the request command as an object array and send to server
			sendToServer(new Object[] { "getOrder", orderNumber });
		} catch (IOException e) {
			// Log the error if the update request fails to send
			System.err.println("Failed to send 'getOrder' request to server: " + e.getMessage());
		}
	}

	/**
	 * Sends a request to the server to update a specific field in an order. The
	 * server is expected to process the update and return a confirmation or error
	 * message.
	 *
	 * @param orderNumber the ID of the order to update
	 * @param field       the name of the field to update (e.g., "order_date",
	 *                    "parking_space")
	 * @param newValue    the new value to set for the specified field
	 */
	public void updateOrder(int orderNumber, String field, String newValue) {
		try {
			// Prepare the update command as an object array and send to server
			sendToServer(new Object[] { "updateOrder", orderNumber, field, newValue });
		} catch (IOException e) {
			// Log the error if the update request fails to send
			System.err.println("Failed to send 'updateOrder' request to server: " + e.getMessage());
		}
	}

}
