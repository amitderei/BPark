package client;

import common.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.net.InetAddress;
import java.util.ArrayList;

/**
 * JavaFX controller class for the BPARK client UI. Responsible for handling
 * user interactions, updating the UI, and communicating with the
 * PrototypeClient (which handles the network logic).
 */
public class ClientController {

	@FXML
	private Button updateButton;

	@FXML
	private Button loadButton;
	
	// TableView and column bindings for displaying Order data
	@FXML
	private TableView<Order> orderTable;
	@FXML
	private TableColumn<Order, Integer> orderNumberCol;
	@FXML
	private TableColumn<Order, Integer> parkingCol;
	@FXML
	private TableColumn<Order, String> orderDateCol;
	@FXML
	private TableColumn<Order, Integer> confirmCol;
	@FXML
	private TableColumn<Order, Integer> subscriberCol;
	@FXML
	private TableColumn<Order, String> placingDateCol;

	// Input fields for updating orders
	@FXML
	private TextField updateOrderId;
	@FXML
	private ComboBox<String> fieldComboBox; // changed from TextField to ComboBox
	@FXML
	private TextField updateValue;

	// Label for displaying network connection information
	@FXML
	private Label connectionLabel;

	// Reference to the client logic (OCSF communication)
	private Client client;
	
	@FXML
	private Label statusLabel;

	/**
	 * Initializes the controller with a reference to the connected client,
	 * displays local host and IP information, and enables/disables controls based on connection state.
	 *
	 * @param client The active client instance used for server communication.
	 */
	public void setClient(Client client) {
	    this.client = client;

	    // Check if connection failed
	    if (client == null) {
	    	showStatus("No connection to server. Please connect first.",false);
            showAlert("No connection to server. Please connect first.", Alert.AlertType.WARNING);
	        System.err.println("Client is null. Cannot establish connection.");
	        updateButton.setDisable(true);
	        loadButton.setDisable(true);
	        return;
	    }

	    try {
	        // Get local host information
	        String host = InetAddress.getLocalHost().getHostName();
	        String ip = InetAddress.getLocalHost().getHostAddress();

	        // Update connection status label
	        connectionLabel.setText("Connected to: " + host + " (" + ip + ")");

	        // Enable action buttons after successful connection
	        updateButton.setDisable(false);
	        loadButton.setDisable(false);

	    } catch (Exception e) {
	        // Show warning if host info cannot be retrieved
	    	showStatus("Could not retrieve network information.",false);
	        showAlert("Could not retrieve network information: " + e.getMessage(), Alert.AlertType.WARNING);
	        System.err.println("Error retrieving network information: " + e.getMessage());
	        updateButton.setDisable(true);
	        loadButton.setDisable(true);
	    }
	}



	
	/**
	 * Handles the "Connect to Server" button action.
	 * Attempts to connect to the server at localhost on port 5555,
	 * updates the controller and application state, and shows the connection status.
	 */
	@FXML
	public void connectToServer() {
	    try {
	        // Create and open a new client connection
	        Client newClient = new Client("localhost", 5555);
	        // Attempt to open the connection to the server (establishes socket communication)
	        newClient.openConnection();

	        // Store the client in both the controller and the application-wide reference
	        this.client = newClient;

	        // Link the controller to the client
	        newClient.setController(this);

	        // Update the UI with connection info
	        setClient(newClient);

	        // Show success message
	        showStatus("Connected successfully to server.",true);

	    } catch (Exception e) {
	        // Show error if connection fails and log to console
	    	showStatus("Failed to connect to server.",false);
	        showAlert("Could not connect to server: " + e.getMessage(), Alert.AlertType.ERROR);
	        System.err.println("Failed to connect to server: " + e.getMessage());
	    }
	}


    /**
     * Handles the "Load Orders" button action.
     * Sends a request to the server to load and display all existing orders.
     */
    @FXML
    public void loadOrders() {
        try {
            client.requestAllOrders();
            showStatus("Requested all orders from server.",true);
        } catch (Exception e) {
            // Show error to user and log the technical details to console
        	showStatus("Failed to request orders.",false);
            showAlert("Failed to request orders: " + e.getMessage(), Alert.AlertType.ERROR);
            System.err.println("Failed to request orders: " + e.getMessage());
        }
    }


	/**
	 * Called when the "Update" button is clicked. Sends an update request for a
	 * specific order field. Reads input from the form, validates it, and sends a
	 * message to the server.
	 */
	@FXML
	public void updateOrder() {
		try {
			// Parse order ID from the text field (must be a number)
			int orderId = Integer.parseInt(updateOrderId.getText().trim());

			// Read the field to update from the ComboBox
			String field = fieldComboBox.getValue();

			// Read the new value the user wants to apply
			String value = updateValue.getText().trim();

			// Validate that both field and value are not empty
			if (field == null || field.isEmpty()) {
				showStatus("Please select a field to update.",false);
				showAlert("Please select a field to update.", Alert.AlertType.WARNING);
				return;
			}
			if (value.isEmpty()) {
				showStatus("Please fill in the 'New Value'.",false);
				showAlert("Please fill in the 'New Value'.", Alert.AlertType.WARNING);
				return;
			}

			// Send update request to the server
			// Format: ["updateOrder", orderId, field, value]
			client.updateOrder(orderId, field, value);
			updateOrderId.clear();
			updateValue.clear();
			

        } catch (NumberFormatException e) {
            // Show warning if the order number is not a valid integer
        	showStatus("Order number must be a valid integer.",false);
            showAlert("Order number must be a valid integer.", Alert.AlertType.WARNING);
            System.err.println("Invalid order number input: " + e.getMessage());

        } catch (Exception e) {
            // Show error and log unexpected issues
        	showStatus("Update failed.",false);
            showAlert("Update failed: " + e.getMessage(), Alert.AlertType.ERROR);
            System.err.println("Unexpected error during update: " + e.getMessage());
        }
	}

    /**
     * Replaces the current TableView content with the list of orders received from the server.
     * Converts the list to an ObservableList so JavaFX can display and track the data.
     *
     * @param orders List of Order objects to display in the TableView.
     */
    public void displayOrders(ArrayList<Order> orders) {
        // Convert the list to an ObservableList for JavaFX data binding (because JavaFX  not support ArrayList)
        ObservableList<Order> data = FXCollections.observableArrayList(orders);

        // Set the new data in the TableView (overwrites previous data)
        orderTable.setItems(data);
        
        //showStatus("Orders loaded successfully.",true);
    }
    
    public void displayTrueMsg(String str) {
    	showStatus(str ,true);
    }


	/**
	 * Utility method for displaying pop-up alerts (message boxes) in the GUI.
	 *
	 * @param message The message to show in the alert dialog.
	 * @param type    The type of alert (INFORMATION, WARNING, or ERROR).
	 */
	private void showAlert(String message, Alert.AlertType type) {
		// Create a new Alert of the specified type (e.g., INFO, WARNING, ERROR)
		Alert alert = new Alert(type);

		// Set a consistent title for all alert popups
		alert.setTitle("BPARK - Message");

		// No header text (cleaner look)
		alert.setHeaderText(null);

		// Set the actual message to be shown in the dialog box
		alert.setContentText(message);

		// Display the alert window and wait until user closes it
		alert.show();
	}

	/**
	 * Called automatically by JavaFX after the FXML file has been loaded. Binds
	 * each TableColumn to the corresponding property of the Order object. This
	 * ensures that when an Order is displayed in the TableView, the correct values
	 * appear in each column.
	 */
	@FXML
	public void initialize() {
		// Bind the 'orderNumber' column to the 'getOrderNumber()' property of the Order
		// object
		orderNumberCol.setCellValueFactory(
				cell -> new javafx.beans.property.SimpleIntegerProperty(cell.getValue().getOrderNumber()).asObject());

		// Bind the 'parking' column to the 'getParkingSpace()' property
		parkingCol.setCellValueFactory(
				cell -> new javafx.beans.property.SimpleIntegerProperty(cell.getValue().getParkingSpace()).asObject());

		// Bind the 'orderDate' column to the string representation of the order date
		orderDateCol.setCellValueFactory(
				cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getOrderDate().toString()));

		// Bind the 'confirmation code' column to the 'getConfirmationCode()' property
		confirmCol.setCellValueFactory(
				cell -> new javafx.beans.property.SimpleIntegerProperty(cell.getValue().getConfirmationCode())
						.asObject());

		// Bind the 'subscriber ID' column to the 'getSubscriberId()' property
		subscriberCol.setCellValueFactory(
				cell -> new javafx.beans.property.SimpleIntegerProperty(cell.getValue().getSubscriberId()).asObject());

		// Bind the 'placing date' column to the string of 'getDateOfPlacingAnOrder()'
		placingDateCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
				cell.getValue().getDateOfPlacingAnOrder().toString()));

		// Initialize ComboBox with allowed fields
		ObservableList<String> allowedFields = FXCollections.observableArrayList("parking_space", "order_date");
		fieldComboBox.setItems(allowedFields);
		fieldComboBox.setValue("parking_space"); // Default value
	}
	
	/**
	 * Displays a status message in the status label with appropriate color.
	 * @param message The status message to display.
	 * @param isSuccess True for success (green), False for error/warning (red).
	 */
	public void showStatus(String message, boolean isSuccess) {
	    statusLabel.setText(message);
	    if (isSuccess) {
	        statusLabel.setStyle("-fx-text-fill: green;");
	    } else {
	        statusLabel.setStyle("-fx-text-fill: red;");
	    }
	}


}
