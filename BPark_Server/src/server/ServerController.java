package server;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Controller for the server connection screen of BPARK.
 * Handles user input for port selection and starts the server.
 */
public class ServerController {

    @FXML
    private Button btnSend;         // Button to trigger server start

    @FXML
    private TextField txtPort;      // Text field for entering the server port

    @FXML
    private Label lblConnection;    // Label for displaying connection status (currently not used)

    @FXML
    private Label lblEx;            // Label for displaying static instructions 

    /**
     * Retrieves the port number entered by the user.
     *
     * @return The port number as a String.
     */
    private String getport() {
        return txtPort.getText();
    }

    /**
     * Triggered when the "Connect" button is clicked.
     * Validates the port input, starts the server if valid,
     * or shows appropriate warnings if invalid.
     *
     * @param event The button click event.
     * @throws Exception if server startup fails.
     */
    public void connect(ActionEvent event) throws Exception {
        String p = getport().trim();

        // Validate that the port field is not empty
        if (p.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Connection Error", "You must enter a port number");
            return;
        }

        int port;

        // Validate that the port is a valid number
        try {
            port = Integer.parseInt(p);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Connection Error", "Port must be a number");
            return;
        }

        // Validate that the port number is in the valid range (1024–65535)
        if (port < 1024 || port > 65535) {
            showAlert(Alert.AlertType.WARNING, "Connection Error", "Port must be between 1024 and 65535");
            return;
        }

        // Hide the connection window after successful validation
        ((Node) event.getSource()).getScene().getWindow().hide();

        // Start the server with the validated port
        ServerApp.runServer(String.valueOf(port));
    }

    /**
     * Utility method to display a customizable alert popup.
     *
     * @param type    The type of alert (e.g., INFORMATION, WARNING, ERROR).
     * @param title   The title of the popup window.
     * @param message The message to display in the popup.
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);              // Set the alert title dynamically
        alert.setHeaderText(null);          // No header text for cleaner appearance
        alert.setContentText(message);      // Set the actual message content
        alert.showAndWait();                // Display the alert and wait for user action
    }

}

