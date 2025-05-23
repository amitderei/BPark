package controllers;

import java.net.InetAddress;

import client.ClientController;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import ui.UiUtils;

/**
 * Controller for the Connect screen in the BPARK client application.
 * Allows users to input a server IP, connect to the server,
 * and exit the application safely.
 */
public class ConnectController {

    /** Displays local host and IP after successful connection */
    @FXML
    private Label connectionLabel;

    /** Screen title label */
    @FXML
    private Label connectHeadline;

    /** Displays connection status messages (success or error) */
    @FXML
    private Label statusLabel;

    /** Button that initiates the connection to the server */
    @FXML
    private Button connectButton;

    /** Button to exit the application */
    @FXML
    private Button exitButton;

    /** Input field for server IP address */
    @FXML
    private TextField ipTextField;

    /** Manages the client-side connection to the server */
    private ClientController client;

    /**
     * Sets the connected client instance and displays local host info if available.
     *
     * @param client the connected ClientController instance
     */
    public void setClient(ClientController client) {
        this.client = client;

        if (client == null) {
            UiUtils.setStatus(statusLabel, "No connection to server. Please connect first.", false);
            UiUtils.showAlert("BPARK - Message", "No connection to server. Please connect first.", Alert.AlertType.WARNING);
            System.err.println("Client is null. Cannot establish connection.");
            return;
        }

        try {
            // Display local host name and IP address
            String host = InetAddress.getLocalHost().getHostName();
            String ip = InetAddress.getLocalHost().getHostAddress();
            connectionLabel.setText("Connected to: " + host + " (" + ip + ")");
        } catch (Exception e) {
            UiUtils.setStatus(statusLabel, "Could not retrieve network information.", false);
            UiUtils.showAlert("BPARK - Message", "Could not retrieve network information: " + e.getMessage(), Alert.AlertType.WARNING);
            System.err.println("Error retrieving network information: " + e.getMessage());
        }
    }

    /**
     * Handles the "Connect to Server" button action.
     * Requires the user to provide a valid IP address in the text field.
     * Attempts to connect to the server on port 5555.
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

            // Create a new client instance with the provided IP and port 5555
            ClientController newClient = new ClientController(ip, 5555);
            newClient.openConnection();

            // Store the connected client instance for future interactions
            this.client = newClient;
            setClient(newClient);

            // Display success message and update UI
            UiUtils.setStatus(statusLabel, "Connected successfully to server at " + ip + ":5555", true);
            connectButton.setText("Connected");
            connectButton.setDisable(true);
        } catch (Exception e) {
            UiUtils.setStatus(statusLabel, "Failed to connect to server.", false);
            UiUtils.showAlert("BPARK - Message", "Could not connect to server: " + e.getMessage(), Alert.AlertType.ERROR);
            System.err.println("Failed to connect to server: " + e.getMessage());
        }
    }

    /**
     * Triggered when the "Exit" button is clicked.
     * Sends disconnect notification to server, closes connection, and exits.
     */
    @FXML
    public void exitApplication() {
        try {
            if (client != null && client.isConnected()) {
                client.sendToServer(new Object[]{"disconnect"});
                client.closeConnection();
                System.out.println("Client disconnected successfully.");
            }
        } catch (Exception e) {
            System.err.println("Failed to disconnect client: " + e.getMessage());
        }

        System.exit(0);
    }
}

