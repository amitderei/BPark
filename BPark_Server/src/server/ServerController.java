package server;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller for the server connection screen of BPARK.
 * Handles user input for port selection and starts the server.
 */
public class ServerController {

    @FXML
    private Button btnSend = null;  // Button for triggering the server start

    @FXML
    private TextField txtPort;      // Input field for the server port

    @FXML
    private Label lblConnection;    // Label for displaying connection status (not used yet)

    @FXML
    private Label lblEx;            // Label for displaying exceptions or guidance (not used yet)

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
     * Validates the port, starts the server on the specified port,
     * and hides the current window.
     *
     * @param event The action event triggered by the button click.
     * @throws Exception if loading the next stage fails.
     */
    public void connect(ActionEvent event) throws Exception {
        String p = getport().trim();

        // Check if input is empty
        if (p.isEmpty()) {
            System.out.println("You must enter a port number");
            lblEx.setText("You must enter a port number");
            return;
        }

        int port;

        // Validate that input is a number
        try {
            port = Integer.parseInt(p);
        } catch (NumberFormatException e) {
            System.out.println("Port must be a number");
            lblEx.setText("Port must be a number");
            return;
        }

        // Validate port range (1024–65535)
        if (port < 1024 || port > 65535) {
            System.out.println("Port must be between 1024 and 65535");
            lblEx.setText("Port must be between 1024 and 65535");
            return;
        }

        // Hide the current window
        ((Node) event.getSource()).getScene().getWindow().hide();

        // Start the server with the validated port
        ServerApp.runServer(String.valueOf(port));
    }


}

