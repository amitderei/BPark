package controllers;

import java.net.InetAddress;
import java.net.UnknownHostException;

import client.ClientApp;
import client.ClientController;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import ui.UiUtils;

/**
 * Controller for the "Connect to Server" screen.
 * Allows the user to enter an IP address, attempt a socket connection,
 * and move forward to the selection screen if successful.
 */
public class ConnectController implements ClientAware {

    /** Reference to the main JavaFX application, used for screen transitions */
    private ClientApp app;

    /** Label that displays connection status (e.g., IP + hostname) */
    @FXML private Label connectionLabel;

    @FXML private Label connectHeadline;
    @FXML private Label statusLabel;
    @FXML private Button connectButton;
    @FXML private Button exitButton;

    /** Input field for entering the server's IP address */
    @FXML private TextField ipTextField;

    /** The client instance responsible for communication with the server */
    private ClientController client;

    /**
     * Injects a reference to the main JavaFX application.
     * This is used to switch scenes later.
     *
     * @param app main application instance
     */
    public void setApp(ClientApp app) {
        this.app = app;
    }

    /**
     * Injects the ClientController and updates the UI with local host info.
     * If the client is null, a warning message is shown.
     *
     * @param client the active client, or null if not connected yet
     */
    @Override
    public void setClient(ClientController client) {
        this.client = client;

        if (client == null) {
            // No active connection
            UiUtils.setStatus(statusLabel,
                    "No connection to server. Please connect first.", false);
            UiUtils.showAlert("BPARK - Message",
                    "No connection to server. Please connect first.",
                    Alert.AlertType.WARNING);
            return;
        }

        try {
            // Show local host details once connected
            String host = InetAddress.getLocalHost().getHostName();
            String ip   = InetAddress.getLocalHost().getHostAddress();
            connectionLabel.setText("Connected to: " + host + " (" + ip + ")");
        } catch (Exception e) {
            UiUtils.setStatus(statusLabel,
                    "Could not retrieve network information.", false);
            UiUtils.showAlert("BPARK - Message",
                    "Could not retrieve network information: " + e.getMessage(),
                    Alert.AlertType.WARNING);
        }
    }

    /**
     * Called when the "Connect" button is pressed.
     * Validates the IP input, opens a socket connection,
     * and transitions to the next screen if successful.
     */
    @FXML
    public void connectToServer() {
        // Check if user entered an IP
        if (ipTextField == null || ipTextField.getText().trim().isEmpty()) {
            UiUtils.setStatus(statusLabel,
                    "Please enter the server IP address.", false);
            UiUtils.showAlert("BPARK - Message",
                    "Please enter the server IP address.",
                    Alert.AlertType.WARNING);
            return;
        }

        try {
            String ip = ipTextField.getText().trim();

            // Create and connect a new client to the given IP on port 5555
            ClientController newClient = new ClientController(ip, 5555);
            newClient.openConnection();

            // Store client and update UI
            this.client = newClient;
            setClient(newClient);

            UiUtils.setStatus(statusLabel,
                    "Connected successfully to server at " + ip + ":5555", true);
            connectButton.setText("Connected");
            connectButton.setDisable(true);

            // Save globally in UiUtils so all controllers can use it
            UiUtils.client = this.client;

            // Load the selection screen (login / guest mode)
            UiUtils.loadScreen(connectButton,
                    "/client/SelectionScreen.fxml", null, client);

        } catch (Exception e) {
            // Connection failed – show error
            UiUtils.setStatus(statusLabel, "Failed to connect to server.", false);
            UiUtils.showAlert("Connection Failed",
                    "Could not connect to the server. "
                    + "Please check the IP address and try again.",
                    Alert.AlertType.ERROR);

            System.err.println("[ERROR] Server connection failed: " + e.getMessage());
        }
    }

    /**
     * Called when the "Exit" button is pressed.
     * If connected, informs the server before disconnecting.
     * Then exits the entire application.
     */
    @FXML
    public void exitApplication() {
        try {
            if (client != null && client.isConnected()) {
                // Notify server that we're disconnecting
                client.sendToServer(new Object[] { "disconnect" });
                client.closeConnection();
                System.out.println("Client disconnected successfully.");
            }
        } catch (Exception e) {
            System.err.println("Failed to disconnect client: " + e.getMessage());
        }
        System.exit(0);
    }

    /**
     * JavaFX initialize hook – called automatically on screen load.
     * Prefills the IP field with the local machine IP for convenience.
     */
    public void initialize() {
        try {
            String ip = InetAddress.getLocalHost().getHostAddress();
            ipTextField.setText(ip);
        } catch (UnknownHostException e) {
            ipTextField.setText("");
        }
    }
}
