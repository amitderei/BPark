package controllers;

import java.io.IOException;

import client.ClientController;
import common.Operation;
import common.User;
import common.UserRole;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import ui.DragUtilServer;
import ui.StageAware;
import ui.UiUtils;

/**
 * Controller for handling the login workflow.
 * 
 * This screen allows the user to enter credentials, send a login request,
 * and get redirected to the appropriate screen based on their role
 * (Subscriber, Attendant, or Manager).
 */
public class LoginController implements ClientAware, StageAware {
	
    /** Text field for entering username */
    @FXML 
    private TextField username;

    /** Password field for entering subscriber/staff code */
    @FXML 
    private PasswordField code;

    /** Button to submit the login form */
    @FXML 
    private Button submit;

    /** Label to show login errors or validation messages */
    @FXML private Label lblError;

    /** Button to return to the previous screen */
    @FXML 
    private Button backButton;

    /** Button to exit the application */
    @FXML 
    private Button btnExit;
    
    /**
     * The toolbar used to drag the undecorated login window.
     */
    @FXML
    private ToolBar dragArea;

    /** Active client controller, used to communicate with the server */
    private ClientController client;

    /** Cached password for future use (e.g., profile editing) */
    private String password;

    /**
     * Enables drag-to-move behavior using the top toolbar.
     *
     * @param stage the primary JavaFX stage
     */
    @Override
    public void setStage(Stage stage) {
        DragUtilServer.enableDrag(dragArea, stage);
    }
    
    /**
     * Injects the client controller and registers this controller
     * as the login callback handler.
     *
     * @param client active client instance
     */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
        client.setLoginController(this);
    }

    /**
     * Called when the login button is clicked.
     * Validates the fields and sends a login request to the server.
     */
    @FXML
    private void handleLoginClick() {
        // Clear previous status message
        lblError.setText("");

        String user = username.getText().trim();
        password    = code.getText().trim();

        if (user.isEmpty() || password.isEmpty()) {
            lblError.setText("Please enter both username and password.");
            return;
        }

        /* ---------- Make sure the socket is open ---------- */
        try {
            if (!client.isConnected()) {          // Socket is closed - open a new one
                client.openConnection();          // May throw IOException
            }
        } catch (IOException ex) {
            lblError.setText("Unable to connect to server.");
            ex.printStackTrace();
            return;                               // Cannot continue without a connection
        }

        submit.setDisable(true);                  // Prevent double-clicks

        /* ---------- Send the login request ---------- */
        try {
            client.getRequestSender()
                  .requestLogin(user, password);  // All network calls go through ClientRequestSender
        } catch (IOException ex) {                // Network problem while sending
            lblError.setText("Failed to send login request.");
            submit.setDisable(false);
            ex.printStackTrace();
        }
    }



    /**
     * Called when login is successful.
     * Navigates the user to the appropriate main layout based on their role.
     *
     * @param user authenticated user object
     */
    public void handleLoginSuccess(User user) {
        submit.setDisable(false);
        System.out.println("[DEBUG] Login successful, role = " + user.getRole());
        navigateToHome(user);
    }

    /**
     * Called when login fails.
     * Displays an error message to the user.
     *
     * @param msg error message to show
     */
    public void handleLoginFailure(String msg) {
        submit.setDisable(false);
        lblError.setText(msg);
        code.clear();            // clear password field
        System.err.println("[DEBUG] Login failed: " + msg);
    }


    /**
     * Disconnects from the server and exits the application.
     * Triggered by the Exit button.
     */
    @FXML
    private void handleExitClick() {
        try {
            if (client != null && client.isConnected()) {
                client.sendToServer(new Object[] { Operation.EXIT});
                client.closeConnection();
                client.clearSession();

            }
        } catch (Exception ignored) { }
        Platform.exit();
        System.exit(0);
    }

    /**
     * Navigates back to the welcome screen (Guest/Login choice).
     */
    @FXML
    private void handleBack() {
        UiUtils.loadScreen(backButton,
                "/client/MainScreen.fxml",
                "BPARK – Welcome",
                client);
    }

    /**
     * Loads the appropriate main layout screen based on the user's role.
     * Passes required references to the next controller.
     *
     * @param user authenticated user returned by the server
     */
    private void navigateToHome(User user) {
    	client.clearSession();
        String fxml;
        UserRole role = user.getRole();

        // Determine target FXML based on role
        switch (role) {
            case Subscriber -> {
                fxml = "/client/SubscriberMainLayout.fxml";
                client.getRequestSender().subscriberDetails(user); // Fetch full subscriber info from server
            }
            case Attendant, Manager -> fxml = "/client/StaffMainLayout.fxml";
            default -> {
                UiUtils.showAlert("BPARK – Error",
                        "Unknown role: " + role,
                        Alert.AlertType.ERROR);
                return;
            }
        }

        try {
            // Load the selected layout
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Object ctrl = loader.getController();

            // If it's a subscriber, configure main layout and preload home screen
            if (ctrl instanceof SubscriberMainLayoutController sub) {
                client.setMainLayoutController(sub);
                sub.setClient(client);
                sub.setSubscriberName(username.getText().trim());
                sub.loadScreen("/client/SubscriberMainScreen.fxml");
                client.setPassword(password); // Save password for reuse
            }

            // If it's a staff member (attendant or manager), pass user info
            if (ctrl instanceof StaffMainLayoutController staff) {
                staff.setClient(client);
                staff.setUser(user);
            }
            
            // for allow draging
            if (ctrl instanceof StageAware stageAware) {
                Stage stage = (Stage) submit.getScene().getWindow();
                stageAware.setStage(stage);
            }

            // Replace current scene
            Stage stage = (Stage) submit.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("BPARK – " + role);
            stage.show();

        } catch (Exception ex) {
            UiUtils.showAlert("BPARK – Error",
                    "Failed to load " + role + " screen.",
                    Alert.AlertType.ERROR);
            ex.printStackTrace();
        }
    }
}
