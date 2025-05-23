package controllers;

import java.net.InetAddress;
import java.time.LocalDate;
import java.util.ArrayList;

import client.ClientController;
import common.Order;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import ui.UiUtils;

/**
 * Controller for the Order View screen.
 * Allows loading and updating orders through communication with the server.
 */
public class OrderViewController {

    @FXML private Button updateButton;
    @FXML private Button loadButton;
    @FXML private TableView<Order> orderTable;
    @FXML private TableColumn<Order, Integer> orderNumberCol;
    @FXML private TableColumn<Order, Integer> parkingCol;
    @FXML private TableColumn<Order, String> orderDateCol;
    @FXML private TableColumn<Order, Integer> confirmCol;
    @FXML private TableColumn<Order, Integer> subscriberCol;
    @FXML private TableColumn<Order, String> placingDateCol;
    @FXML private TextField updateOrderId;
    @FXML private ComboBox<String> fieldComboBox;
    @FXML private TextField updateValue;
    @FXML private DatePicker updateDatePicker;
    @FXML private Label connectionLabel;
    @FXML private Label statusLabel;
    @FXML private Button connectButton;
    @FXML private Button searchOrder;
    @FXML private Button exitButton;
    @FXML private TextField ipTextField;

    private ClientController client;

    /**
     * Sets the connected client instance and updates UI elements accordingly.
     *
     * @param client the connected ClientController instance
     */
    public void setClient(ClientController client) {
        this.client = client;

        if (client == null) {
            UiUtils.setStatus(statusLabel, "No connection to server. Please connect first.", false);
            UiUtils.showAlert("BPARK - Message", "No connection to server. Please connect first.", Alert.AlertType.WARNING);
            updateButton.setDisable(true);
            loadButton.setDisable(true);
            searchOrder.setDisable(true);
            return;
        }

        try {
            String host = InetAddress.getLocalHost().getHostName();
            String ip = InetAddress.getLocalHost().getHostAddress();
            connectionLabel.setText("Connected to: " + host + " (" + ip + ")");
            updateButton.setDisable(false);
            loadButton.setDisable(false);
            searchOrder.setDisable(false);
        } catch (Exception e) {
            UiUtils.setStatus(statusLabel, "Could not retrieve network information.", false);
            UiUtils.showAlert("BPARK - Message", "Could not retrieve network information: " + e.getMessage(), Alert.AlertType.WARNING);
            updateButton.setDisable(true);
            loadButton.setDisable(true);
        }
    }

    /**
     * Attempts to connect to the server using the IP address provided by the user.
     */
    @FXML
    public void connectToServer() {
        if (ipTextField == null || ipTextField.getText().trim().isEmpty()) {
            UiUtils.setStatus(statusLabel, "Please enter the server IP address.", false);
            UiUtils.showAlert("BPARK - Message", "Please enter the server IP address.", Alert.AlertType.WARNING);
            return;
        }

        try {
            String ip = ipTextField.getText().trim();
            ClientController newClient = new ClientController(ip, 5555);
            newClient.openConnection();
            this.client = newClient;
            newClient.setController(this);
            setClient(newClient);
            UiUtils.setStatus(statusLabel, "Connected successfully to server at " + ip + ":5555", true);
            connectButton.setText("Connected");
            connectButton.setDisable(true);
        } catch (Exception e) {
            UiUtils.setStatus(statusLabel, "Failed to connect to server.", false);
            UiUtils.showAlert("BPARK - Message", "Could not connect to server: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Requests all orders from the server and displays them in the TableView.
     */
    @FXML
    public void loadOrders() {
        try {
            client.requestAllOrders();
            UiUtils.setStatus(statusLabel, "Requested all orders from server.", true);
        } catch (Exception e) {
            UiUtils.setStatus(statusLabel, "Failed to request orders.", false);
            UiUtils.showAlert("BPARK - Message", "Failed to request orders: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Requests a specific order by ID and displays it.
     */
    @FXML
    public void loadSpecificOrder() {
        try {
            int orderId = Integer.parseInt(updateOrderId.getText().trim());
            client.requestOrderByOrderNum(orderId);
            updateOrderId.clear();
        } catch (NumberFormatException e) {
            UiUtils.setStatus(statusLabel, "Order number must be a valid integer.", false);
            UiUtils.showAlert("BPARK - Message", "Order number must be a valid integer.", Alert.AlertType.WARNING);
        }
    }

    /**
     * Sends a request to update a specific field in an order.
     */
    @FXML
    public void updateOrder() {
        try {
            int orderId = Integer.parseInt(updateOrderId.getText().trim());
            String field = fieldComboBox.getValue();
            String value;

            if ("order_date".equals(field)) {
                if (updateDatePicker.getValue() == null) {
                    UiUtils.setStatus(statusLabel, "Please select a date.", false);
                    UiUtils.showAlert("BPARK - Message", "Please select a date.", Alert.AlertType.WARNING);
                    return;
                }
                value = updateDatePicker.getValue().toString();
            } else {
                value = updateValue.getText().trim();
                if (value.isEmpty()) {
                    UiUtils.setStatus(statusLabel, "Please fill in the 'New Value'.", false);
                    UiUtils.showAlert("BPARK - Message", "Please fill in the 'New Value'.", Alert.AlertType.WARNING);
                    return;
                }
            }

            if (field == null || field.isEmpty()) {
                UiUtils.setStatus(statusLabel, "Please select a field to update.", false);
                UiUtils.showAlert("BPARK - Message", "Please select a field to update.", Alert.AlertType.WARNING);
                return;
            }

            client.updateOrder(orderId, field, value);
            updateOrderId.clear();
            updateValue.clear();
            updateDatePicker.setValue(null);

        } catch (NumberFormatException e) {
            UiUtils.setStatus(statusLabel, "Order number must be a valid integer.", false);
            UiUtils.showAlert("BPARK - Message", "Order number must be a valid integer.", Alert.AlertType.WARNING);
        } catch (Exception e) {
            UiUtils.setStatus(statusLabel, "Update failed.", false);
            UiUtils.showAlert("BPARK - Message", "Update failed: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Populates the TableView with a list of orders.
     *
     * @param orders list of orders to display
     */
    public void displayOrders(ArrayList<Order> orders) {
        ObservableList<Order> data = FXCollections.observableArrayList(orders);
        orderTable.setItems(data);
    }

    /**
     * Closes the client connection and exits the application.
     */
    @FXML
    public void exitApplication() {
        try {
            if (client != null && client.isConnected()) {
                client.sendToServer(new Object[] { "disconnect" });
                client.closeConnection();
            }
        } catch (Exception e) {
            System.err.println("Failed to disconnect client: " + e.getMessage());
        }
        System.exit(0);
    }

    /**
     * Initializes TableView bindings and ComboBox behavior.
     * Automatically called after FXML is loaded.
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

        ObservableList<String> allowedFields = FXCollections.observableArrayList("parking_space", "order_date");
        fieldComboBox.setItems(allowedFields);
        fieldComboBox.setValue("parking_space");

        fieldComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isDateField = "order_date".equals(newVal);
            updateDatePicker.setVisible(isDateField);
            updateDatePicker.setManaged(isDateField);
            updateValue.setVisible(!isDateField);
            updateValue.setManaged(!isDateField);
        });

        updateDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date.isBefore(LocalDate.now())) {
                    setDisable(true);
                }
            }
        });
    }
}
