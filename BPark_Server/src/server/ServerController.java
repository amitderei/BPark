package server;

import java.util.ArrayList;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Controller for the BPARK server connection screen.
 * Handles port input from the user, starts the server, and manages UI updates.
 */
public class ServerController {

    /** Button to connect and start the server */
    @FXML
    private Button btnSend;

    /** Text field for entering the desired port */
    @FXML
    private TextField txtPort;

    /** Label that shows server connection status */
    @FXML
    private Label lblConnection;

    /** Label with static connection instructions */
    @FXML
    private Label lblEx;

    /** Text area to display a list of connected clients */
    @FXML
    private TextArea txtConnectedClients;

    /** Button to shut down the server application */
    @FXML
    private Button btnExit;

    /** Instance of the running server */
    private Server serverInstance;

    /** Reference to the ServerApp, used to start the server */
    private ServerApp app;

    /** Timeline for auto-refreshing the connected clients list */
    private Timeline clientUpdateTimeline;
    
    /** AnchorPane area that handles drag gestures for moving the undecorated window */
    @FXML
    private AnchorPane dragArea;

    /** Offset from the mouse click X position to the window X */
    private double xOffset = 0;

    /** Offset from the mouse click Y position to the window Y */
    private double yOffset = 0;

    /** Reference to the Stage, used to allow window dragging */
    private Stage stageRef;

    /**
     * Links this controller with the ServerApp instance.
     *
     * @param app the ServerApp used to launch the server
     */
    public void setApp(ServerApp app) {
        this.app = app;
    }
    
    /**
     * Passes the stage from ServerApp to this controller.
     * Required in order to move the undecorated window with mouse drag.
     *
     * @param stage the primary JavaFX stage (window)
     */
    public void setStage(Stage stage) {
        this.stageRef = stage;
    }

    /**
     * Reads the port value from the input field.
     *
     * @return the port number as a string
     */
    private String getport() {
        return txtPort.getText();
    }

    /**
     * Called when the "Connect" button is clicked.
     * Validates the port input and starts the server.
     *
     * @param event the button click event
     * @throws Exception if server startup fails
     */
    public void connect(ActionEvent event) throws Exception {
        String p = getport().trim(); // read and trim input

        // Check if input is empty
        if (p.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Connection Error", "You must enter a port number");
            return;
        }

        int port;

        try {
            port = Integer.parseInt(p); // convert input to number
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Connection Error", "Port must be a number");
            return;
        }

        // Validate port range (only 1024–65535 are allowed)
        if (port < 1024 || port > 65535) {
            showAlert(Alert.AlertType.WARNING, "Connection Error", "Port must be between 1024 and 65535");
            return;
        }

        // Launch the server and store its instance
        serverInstance = app.runServer(String.valueOf(port));

        // Update UI to reflect that the server is now running
        lblConnection.setText("Server is running on port " + port);
        txtPort.setDisable(true);
        btnSend.setDisable(true);

        // Start automatic refresh of connected clients list
        startAutoClientListUpdater();
    }

    /**
     * Shows the list of currently connected clients in the text area.
     */
    @FXML
    public void showConnectedClients() {
        // Don't proceed if server hasn't started
        if (serverInstance == null) {
            showAlert(Alert.AlertType.WARNING, "Server Not Running", "Please start the server first.");
            return;
        }

        // Get list of connected clients
        ArrayList<String> clients = serverInstance.getConnectedClientInfoList();

        // Display client list or message if empty
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
     * Shuts down the server and closes the application.
     * Notifies connected clients before exiting.
     */
    @FXML
    public void exitApplication() {
        try {
            if (serverInstance != null) {
                // Notify all clients before closing the server
                serverInstance.sendToAllClients("server_shutdown");

                // Close the server
                serverInstance.close();
                System.out.println("Server stopped successfully.");
            }
        } catch (Exception e) {
            System.err.println("Failed to stop server: " + e.getMessage());
        }

        // Exit the entire application
        System.exit(0);
    }

    /**
     * Displays a JavaFX alert with a given message.
     *
     * @param type    the type of alert (INFO, WARNING, etc.)
     * @param title   the title of the alert window
     * @param message the message to show
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Starts a timeline that refreshes the client list every 5 seconds.
     */
    private void startAutoClientListUpdater() {
        clientUpdateTimeline = new Timeline(new KeyFrame(Duration.seconds(5), e -> showConnectedClients()));
        clientUpdateTimeline.setCycleCount(Timeline.INDEFINITE);
        clientUpdateTimeline.play();
    }

    /**
     * JavaFX initialize method.
     * Sets a default port value in the text field on load,
     * and attaches mouse listeners for dragging the undecorated window.
     */
    @FXML
    public void initialize() {
        txtPort.setText("5555");

        // Drag event: record initial offset when mouse is pressed
        dragArea.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        // Drag event: move the stage when mouse is dragged
        dragArea.setOnMouseDragged(event -> {
            if (stageRef != null) {
                stageRef.setX(event.getScreenX() - xOffset);
                stageRef.setY(event.getScreenY() - yOffset);
            }
        });
    }
    
    
}
