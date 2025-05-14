package server;

import java.util.ArrayList;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.util.Duration;

/**
 * Controller for the server connection screen of BPARK. Handles user input for
 * port selection and starts the server.
 */
public class ServerController {

	@FXML
	private Button btnSend; // Button to trigger server start

	@FXML
	private TextField txtPort; // Text field for entering the server port

	@FXML
	private Label lblConnection; // Label for displaying connection status (currently not used)

	@FXML
	private Label lblEx; // Label for displaying static instructions
	
    @FXML
    private TextArea txtConnectedClients; // TextArea to display connected clients
    
    @FXML
    private Button btnExit; // Button to exit the server application

    // Instance of the server (to access connected clients)
    private Server serverInstance;

    // Reference to the ServerApp instance to control server startup
    private ServerApp app;


    private Timeline clientUpdateTimeline;

    /**
     * Sets the ServerApp instance to allow calling its methods from the controller.
     *
     * @param app the ServerApp instance to link with this controller
     */
    public void setApp(ServerApp app) {
        this.app = app;
    }

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
     * and updates the UI to reflect that the server is running.
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

        // Start the server with the validated port and store the instance for later use
        serverInstance = app.runServer(String.valueOf(port));

        // UI feedback after successful server start
        lblConnection.setText("Server is running on port " + port); // Update the connection label
        txtPort.setDisable(true); // Disable the port input field
        btnSend.setDisable(true); // Disable the connect button
        startAutoClientListUpdater();
    }

	
	/**
     * Triggered when the "Show Connected Clients" button is clicked.
     * Fetches the list of connected clients and displays them in the TextArea.
     */
    @FXML
    public void showConnectedClients() {
        if (serverInstance == null) {
            showAlert(Alert.AlertType.WARNING, "Server Not Running", "Please start the server first.");
            return;
        }

        // Get the list of connected client addresses
        ArrayList<String> clients = serverInstance.getConnectedClientInfoList();

        // Build the display text
        if (clients.isEmpty()) {
            txtConnectedClients.setText("No clients connected.");
        } else {
            StringBuilder builder = new StringBuilder();
            for (String clientInfo : clients) {
                builder.append(clientInfo).append("\n");
            }
            txtConnectedClients.setText(builder.toString());
        }
    }
    
    /**
     * Triggered when the "Exit" button is clicked.
     * Safely closes the server if running and exits the application.
     */
    @FXML
    public void exitApplication() {
        try {
            if (serverInstance != null) {
                // Close server if it was started
                serverInstance.close();
                System.out.println("Server stopped successfully.");
            }
        } catch (Exception e) {
            System.err.println("Failed to stop server: " + e.getMessage());
        }

        // Exit the application
        System.exit(0);
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
		alert.setTitle(title); // Set the alert title dynamically
		alert.setHeaderText(null); // No header text for cleaner appearance
		alert.setContentText(message); // Set the actual message content
		alert.showAndWait(); // Display the alert and wait for user action
	}
	
	private void startAutoClientListUpdater() {
		clientUpdateTimeline=new Timeline(new KeyFrame(Duration.seconds(5), e->showConnectedClients()));
		clientUpdateTimeline.setCycleCount(Timeline.INDEFINITE);
		clientUpdateTimeline.play();
	}

}
