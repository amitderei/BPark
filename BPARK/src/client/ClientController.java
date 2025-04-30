package client;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.ArrayList;

/**
 * JavaFX Controller that handles user interaction in the GUI.
 */
public class ClientController {

    @FXML private TextArea outputArea;
    @FXML private TextField orderNumberField;
    @FXML private TextField fieldField;
    @FXML private TextField newValueField;

    private PrototypeClient client;

    public void setClient(PrototypeClient client) {
        this.client = client;
        client.requestAllOrders();
    }

    /**
     * Called when the "Refresh" button is clicked.
     */
    @FXML
    void onRefreshClicked() {
        client.requestAllOrders();
    }

    /**
     * Called when the "Update" button is clicked.
     */
    @FXML
    void onUpdateClicked() {
        try {
            int orderNumber = Integer.parseInt(orderNumberField.getText());
            String field = fieldField.getText().trim();
            String newValue = newValueField.getText().trim();

            if (!field.equals("parking_space") && !field.equals("order_date")) {
                appendOutput("Only 'parking_space' or 'order_date' can be updated.");
                return;
            }

            client.updateOrder(orderNumber, field, newValue);
        } catch (NumberFormatException e) {
            appendOutput("Order number must be a number.");
        }
    }

    public void appendOutput(String text) {
        outputArea.appendText(text + "\n");
    }

    public void displayOrderList(ArrayList<String> orders) {
        outputArea.clear();
        for (String order : orders) {
            outputArea.appendText(order + "\n");
        }
    }
}
