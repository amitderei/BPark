package controllers;

import java.net.InetAddress;

import client.ClientController;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Controller for the Connect screen in the BPARK client application.
 * Allows users to input a server IP, connect to the server,
 * and exit the application safely.
 */
public class ConnectController {

    @FXML
    private Label connectionLabel; // Displays local host and IP after successful connection

    @FXML
    private Label connectHeadline; // Screen title label

    @FXML
    private Label statusLabel; // Displays connection status messages (success or error)

    @FXML
    private Button connectButton; // Button that initiates the connection to the server

    @FXML
    private Button exitButton; // Button to exit the application

    @FXML
    private TextField ipTextField; // Input field for server IP address

    private ClientController client; // Manages the client-side connection to the server

    /**
     * Sets the connected client instance and displays local host info if available.
     *
     * @param client the connected ClientController instance
     */
    public void setClient(ClientController client) {
        this.client = client;

        if (client == null) {
            // No active connection – show error and exit
            showStatus("No connection to server. Please connect first.", false);
            showAlert("No connection to server. Please connect first.", Alert.AlertType.WARNING);
            System.err.println("Client is null. Cannot establish connection.");
            return;
        }

        try {
            // Fetch local machine hostname and IP address
            String host = InetAddress.getLocalHost().getHostName();
            String ip = InetAddress.getLocalHost().getHostAddress();

            // Show connected host/IP to the user
            connectionLabel.setText("Connected to: " + host + " (" + ip + ")");

        } catch (Exception e) {
            // Failed to retrieve local network info
            showStatus("Could not retrieve network information.", false);
            showAlert("Could not retrieve network information: " + e.getMessage(), Alert.AlertType.WARNING);
            System.err.println("Error retrieving network information: " + e.getMessage());
        }
    }

    /**
     * Attempts to connect to the server using the IP provided in the text field.
     * On success, updates the status label and disables the connect button.
     */
    @FXML
    public void connectToServer() {
        try {
            // Validate input – must enter an IP address
            if (ipTextField == null || ipTextField.getText().trim().isEmpty()) {
                showStatus("Please enter the server IP address.", false);
                showAlert("Please enter the server IP address.", Alert.AlertType.WARNING);
                return;
            }

            // Get IP address from user input
            String ip = ipTextField.getText().trim();

            // Create a new client and try to connect to the given IP on port 5555
            ClientController newClient = new ClientController(ip, 5555);
            newClient.openConnection();

            // Store and link the client
            this.client = newClient;
            newClient.setController(this);

            // Update connection UI
            setClient(newClient);
            showStatus("Connected successfully to server at " + ip + ":5555", true);

            // Update the button to indicate success
            connectButton.setText("Connected");
            connectButton.setDisable(true);

        } catch (Exception e) {
            // Connection failed – inform the user
            showStatus("Failed to connect to server.", false);
            showAlert("Could not connect to server: " + e.getMessage(), Alert.AlertType.ERROR);
            System.err.println("Failed to connect to server: " + e.getMessage());
        }
    }

    /**
     * Disconnects the client (if connected) and closes the application.
     * Sends a "disconnect" message to the server before exiting.
     */
    @FXML
    public void exitApplication() {
        try {
            if (client != null && client.isConnected()) {
                // Let the server know we're disconnecting
                client.sendToServer(new Object[]{"disconnect"});

                // Gracefully close the connection
                client.closeConnection();
                System.out.println("Client disconnected successfully.");
            }
        } catch (Exception e) {
            System.err.println("Failed to disconnect client: " + e.getMessage());
        }

        // Exit the JavaFX application
        System.exit(0);
    }

    /**
     * Displays an alert dialog to the user with the specified message and type.
     *
     * @param message the message to display
     * @param type    the type of alert (INFO, WARNING, ERROR)
     */
    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("BPARK - Message");
        alert.setHeaderText(null); // No header for cleaner dialog
        alert.setContentText(message);
        alert.show();
    }

    /**
     * Updates the status label with the given message in green (success) or red (error).
     *
     * @param message   the status message to display
     * @param isSuccess true if the status indicates success, false for error
     */
    public void showStatus(String message, boolean isSuccess) {
        statusLabel.setText(message);
        statusLabel.setStyle(isSuccess ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
    }
}

