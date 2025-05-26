package controllers;

import java.net.InetAddress;

import client.ClientApp;
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
public class ConnectController implements ClientAware {

    /** Reference to the main JavaFX application instance */
    private ClientApp app;

    /** Displays the connected hostname and IP address */
    @FXML private Label connectionLabel;

    /** Label for the screen's main title */
    @FXML private Label connectHeadline;

    /** Label for displaying connection status messages */
    @FXML private Label statusLabel;

    /** Button that initiates the connection to the server */
    @FXML private Button connectButton;

    /** Button that closes the application */
    @FXML private Button exitButton;

    /** Text field for entering the server's IP address */
    @FXML private TextField ipTextField;

    /** Client controller for managing the connection to the server */
    private ClientController client;

    /**
     * Injects the reference to the main JavaFX application.
     *
     * @param app the main client application instance
     */
    public void setApp(ClientApp app) {
        this.app = app;
    }

    /**
     * Sets the connected client instance and displays local host information.
     * If client is null, shows an appropriate warning to the user.
     *
     * @param client the connected ClientController instance
     */
    @Override
    public void setClient(ClientController client) {
        this.client = client;

        if (client == null) {
            UiUtils.setStatus(statusLabel, "No connection to server. Please connect first.", false);
            UiUtils.showAlert("BPARK - Message", "No connection to server. Please connect first.", Alert.AlertType.WARNING);
            return;
        }

        try {
            String host = InetAddress.getLocalHost().getHostName();
            String ip = InetAddress.getLocalHost().getHostAddress();
            connectionLabel.setText("Connected to: " + host + " (" + ip + ")");
        } catch (Exception e) {
            UiUtils.setStatus(statusLabel, "Could not retrieve network information.", false);
            UiUtils.showAlert("BPARK - Message", "Could not retrieve network information: " + e.getMessage(), Alert.AlertType.WARNING);
        }
    }

    /**
     * Handles the "Connect to Server" button click.
     * Validates the IP input, attempts to open a connection,
     * and if successful, navigates to the MainScreen.
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

            // Establish connection to server on port 5555
            ClientController newClient = new ClientController(ip, 5555);
            newClient.openConnection();

            this.client = newClient;
            setClient(newClient);

            UiUtils.setStatus(statusLabel, "Connected successfully to server at " + ip + ":5555", true);
            connectButton.setText("Connected");
            connectButton.setDisable(true);

            // Load main screen (Login + Guest)
            UiUtils.loadScreen(connectButton,
                               "/client/MainScreen.fxml",
                               "BPARK - Welcome",
                               client);

        } catch (Exception e) {
            UiUtils.setStatus(statusLabel, "Failed to connect to server.", false);
            UiUtils.showAlert("BPARK - Message", "Could not connect to server or load next screen: " + e.getMessage(),
                    Alert.AlertType.ERROR);
            System.err.println("Connection or navigation error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handles the "Exit" button click.
     * Gracefully disconnects from the server and terminates the application.
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
