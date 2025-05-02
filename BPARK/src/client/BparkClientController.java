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
     */
    @FXML
    public void updateOrder() {
        try {
            int orderId = Integer.parseInt(updateOrderId.getText().trim());
            String field = updateField.getText().trim();
            String value = updateValue.getText().trim();

            if (field.isEmpty() || value.isEmpty()) {
                showAlert("Field and value cannot be empty.", Alert.AlertType.WARNING);
                return;
            }

            client.updateOrder(orderId, field, value);
        } catch (NumberFormatException e) {
            showAlert("Order number must be a valid integer.", Alert.AlertType.WARNING);
        } catch (Exception e) {
            showAlert("Update failed: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Updates the TableView UI with the list of orders received from the server.
     *
     * @param orders List of orders to display in the table.
     */
    public void displayOrders(ArrayList<Order> orders) {
        ObservableList<Order> data = FXCollections.observableArrayList(orders);
        orderTable.setItems(data);
    }

    /**
     * Utility method for displaying pop-up alerts.
     *
     * @param message The message to show in the alert dialog.
     * @param type The type of the alert (INFORMATION, WARNING, ERROR).
     */
    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("BPARK - Message");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    /**
     * Called automatically by JavaFX after the FXML is loaded.
     * Initializes column bindings to Order properties.
     */
    @FXML
    public void initialize() {
        orderNumberCol.setCellValueFactory(cell ->
            new javafx.beans.property.SimpleIntegerProperty(cell.getValue().getOrderNumber()).asObject());

        parkingCol.setCellValueFactory(cell ->
            new javafx.beans.property.SimpleIntegerProperty(cell.getValue().getParkingSpace()).asObject());

        orderDateCol.setCellValueFactory(cell ->
            new javafx.beans.property.SimpleStringProperty(cell.getValue().getOrderDate().toString()));

        confirmCol.setCellValueFactory(cell ->
            new javafx.beans.property.SimpleIntegerProperty(cell.getValue().getConfirmationCode()).asObject());

        subscriberCol.setCellValueFactory(cell ->
            new javafx.beans.property.SimpleIntegerProperty(cell.getValue().getSubscriberId()).asObject());

        placingDateCol.setCellValueFactory(cell ->
            new javafx.beans.property.SimpleStringProperty(cell.getValue().getDateOfPlacingAnOrder().toString()));
    }
}

