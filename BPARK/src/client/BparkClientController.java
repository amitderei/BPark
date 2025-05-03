package client;

import common.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.net.InetAddress;
import java.util.ArrayList;

/**
 * JavaFX controller class for the BPARK client UI.
 * Responsible for handling user interactions, updating the UI,
 * and communicating with the PrototypeClient (which handles the network logic).
 */
public class BparkClientController {

    // TableView and column bindings for displaying Order data
    @FXML private TableView<Order> orderTable;
    @FXML private TableColumn<Order, Integer> orderNumberCol;
    @FXML private TableColumn<Order, Integer> parkingCol;
    @FXML private TableColumn<Order, String> orderDateCol;
    @FXML private TableColumn<Order, Integer> confirmCol;
    @FXML private TableColumn<Order, Integer> subscriberCol;
    @FXML private TableColumn<Order, String> placingDateCol;

    // Input fields for updating orders
    @FXML private TextField updateOrderId;
    @FXML private TextField updateField;
    @FXML private TextField updateValue;

    // Label for displaying network connection information
    @FXML private Label connectionLabel;

    // Reference to the client logic (OCSF communication)
    private BparkClient client;

    /**
     * Initializes the controller with a reference to the connected client,
     * and displays the local host and IP information.
     *
     * @param client The active client instance used for communication.
     */
    public void setClient(BparkClient client) {
        this.client = client;
        try {
            String host = InetAddress.getLocalHost().getHostName();
            String ip = InetAddress.getLocalHost().getHostAddress();
            connectionLabel.setText("Connected to: " + host + " (" + ip + ")");
        } catch (Exception e) {
            showAlert("Could not retrieve network information", Alert.AlertType.WARNING);
        }
    }

    /**
     * Called when the "Load Orders" button is clicked.
     * Sends a request to the server to fetch all order records.
     */
    @FXML
    public void loadOrders() {
        try {
            client.requestAllOrders();
        } catch (Exception e) {
            showAlert("Failed to request orders: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Called when the "Update" button is clicked.
     * Sends an update request for a specific order field.
     * Reads input from the form, validates it, and sends a message to the server.
     */
    @FXML
    public void updateOrder() {
        try {
            // Parse order ID from the text field (must be a number)
            int orderId = Integer.parseInt(updateOrderId.getText().trim());

            // Read the field to update (e.g., "parking_space" or "order_date")
            String field = updateField.getText().trim();

            // Read the new value the user wants to apply
            String value = updateValue.getText().trim();

            // Validate that both field and value are not empty
            if (field.isEmpty() || value.isEmpty()) {
                showAlert("Field and value cannot be empty.", Alert.AlertType.WARNING);
                return; // Stop execution – don't send invalid request
            }

            // Send update request to the server
            // Format: ["updateOrder", orderId, field, value]
            client.updateOrder(orderId, field, value);

        } catch (NumberFormatException e) {
            // If orderId is not a valid integer (e.g., user typed text), show warning
            showAlert("Order number must be a valid integer.", Alert.AlertType.WARNING);

        } catch (Exception e) {
            // Catch-all for unexpected errors – show error popup
            showAlert("Update failed: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }


    /**
     * Updates the TableView UI with the list of orders received from the server.
     *
     * @param orders List of Order objects to display in the TableView.
     */
    public void displayOrders(ArrayList<Order> orders) {
        // Convert the ArrayList to an ObservableList so JavaFX can track changes
        ObservableList<Order> data = FXCollections.observableArrayList(orders);

        // Load the data into the TableView, replacing any previous content
        orderTable.setItems(data);
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
     * Called automatically by JavaFX after the FXML file has been loaded.
     * Binds each TableColumn to the corresponding property of the Order object.
     * This ensures that when an Order is displayed in the TableView,
     * the correct values appear in each column.
     */
    @FXML
    public void initialize() {
        // Bind the 'orderNumber' column to the 'getOrderNumber()' property of the Order object
        orderNumberCol.setCellValueFactory(cell ->
            new javafx.beans.property.SimpleIntegerProperty(cell.getValue().getOrderNumber()).asObject());

        // Bind the 'parking' column to the 'getParkingSpace()' property
        parkingCol.setCellValueFactory(cell ->
            new javafx.beans.property.SimpleIntegerProperty(cell.getValue().getParkingSpace()).asObject());

        // Bind the 'orderDate' column to the string representation of the order date
        orderDateCol.setCellValueFactory(cell ->
            new javafx.beans.property.SimpleStringProperty(cell.getValue().getOrderDate().toString()));

        // Bind the 'confirmation code' column to the 'getConfirmationCode()' property
        confirmCol.setCellValueFactory(cell ->
            new javafx.beans.property.SimpleIntegerProperty(cell.getValue().getConfirmationCode()).asObject());

        // Bind the 'subscriber ID' column to the 'getSubscriberId()' property
        subscriberCol.setCellValueFactory(cell ->
            new javafx.beans.property.SimpleIntegerProperty(cell.getValue().getSubscriberId()).asObject());

        // Bind the 'placing date' column to the string of 'getDateOfPlacingAnOrder()'
        placingDateCol.setCellValueFactory(cell ->
            new javafx.beans.property.SimpleStringProperty(cell.getValue().getDateOfPlacingAnOrder().toString()));
    }

}

