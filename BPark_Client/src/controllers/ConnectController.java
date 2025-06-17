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
 * Controller for the “Connect to Server” screen.
 * Lets the user type an IP, open a socket connection,
 * and exit the application if needed.
 */
public class ConnectController implements ClientAware {

    /** Main JavaFX application, used later for scene changes */
    private ClientApp app;

    /** Shows local host name and IP once a connection is active */
    @FXML private Label connectionLabel;

    @FXML private Label connectHeadline;
    @FXML private Label statusLabel;
    @FXML private Button connectButton;
    @FXML private Button exitButton;

    /** Text field where the user types or edits the server IP */
    @FXML private TextField ipTextField;

    /** Shared client instance that owns the socket connection */
    private ClientController client;

    /**
     * Stores the reference to the main JavaFX application.
     *
     * @param app main application instance
     */
    public void setApp(ClientApp app) {
        this.app = app;
    }

    /**
     * Supplies the active client instance and prints local
     * host details on screen when possible.
     *
     * @param client an already-connected ClientController,
     *               or null before a connection exists
     */
    @Override
    public void setClient(ClientController client) {
        this.client = client;

        if (client == null) {
            UiUtils.setStatus(statusLabel,
                    "No connection to server. Please connect first.", false);
            UiUtils.showAlert("BPARK - Message",
                    "No connection to server. Please connect first.",
                    Alert.AlertType.WARNING);
            return;
        }

        try {
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
     * Triggered by the “Connect” button.
     * Checks the IP field, attempts to open the socket,
     * updates the UI, and loads the next screen on success.
     *
     * @throws IllegalStateException if the IP field is empty
     */
    @FXML
    public void connectToServer() {
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

            // Create a fresh client controller and open the socket on port 5555
            ClientController newClient = new ClientController(ip, 5555);
            newClient.openConnection();

            this.client = newClient;
            setClient(newClient); // reuse existing UI update logic

            UiUtils.setStatus(statusLabel,
                    "Connected successfully to server at " + ip + ":5555", true);
            connectButton.setText("Connected");
            connectButton.setDisable(true);

            // Store globally so other controllers can fetch it
            UiUtils.client = this.client;

            // Switch to the selection screen (login / guest choice)
            UiUtils.loadScreen(connectButton,
                    "/client/SelectionScreen.fxml", null, client);

        } catch (Exception e) {
            UiUtils.setStatus(statusLabel, "Failed to connect to server.", false);
            UiUtils.showAlert("Connection Failed",
                    "Could not connect to the server. "
                    + "Please check the IP address and try again.",
                    Alert.AlertType.ERROR);

            System.err.println("[ERROR] Server connection failed: " + e.getMessage());
        }
    }

    /**
     * Triggered by the “Exit” button.
     * Sends a graceful disconnect to the server (if connected)
     * and terminates the JVM.
     */
    @FXML
    public void exitApplication() {
        try {
            if (client != null && client.isConnected()) {
                // Inform server before closing the socket
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
     * JavaFX initialise hook.
     * Prefills the IP field with the local machine address so the user
     * can copy-paste it easily when testing on the same PC.
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
