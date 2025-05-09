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
        String p = getport();

        if (p.trim().isEmpty()) {
            // User did not enter a port number
            System.out.println("You must enter a port number");
        } else {
            // Hide the current window
            ((Node) event.getSource()).getScene().getWindow().hide();

            // Prepare the server with the specified port
            Stage primaryStage = new Stage();
            FXMLLoader loader = new FXMLLoader();
            ServerApp.runServer(p);
        }
    }
}

