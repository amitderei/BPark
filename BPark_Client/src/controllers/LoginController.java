package controllers;

import client.ClientController;
import common.User;
import common.UserRole;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import ui.UiUtils;

/**
 * Controller for handling the login workflow.
 * 
 * This screen allows the user to enter credentials, send a login request,
 * and get redirected to the appropriate screen based on their role
 * (Subscriber, Attendant, or Manager).
 */
public class LoginController implements ClientAware {

    // ==============================
    // FXML Fields
    // ==============================

    /** Text field for entering username */
    @FXML private TextField username;

    /** Password field for entering subscriber/staff code */
    @FXML private PasswordField code;

    /** Button to submit the login form */
    @FXML private Button submit;

    /** Label to show login errors or validation messages */
    @FXML private Label lblError;

    /** Button to return to the previous screen */
    @FXML private Button backButton;

    /** Button to exit the application */
    @FXML private Button btnExit;

    // ==============================
    // Runtime Fields
    // ==============================

    /** Active client controller, used to communicate with the server */
    private ClientController client;

    /** Cached password for future use (e.g., profile editing) */
    private String password;

    // ==============================
    // Dependency Injection
    // ==============================

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

    // ==============================
    // FXML Event Handlers
    // ==============================

    /**
     * Called when the login button is clicked.
     * Validates the fields and sends a login request to the server.
     */
    @FXML
    private void handleLoginClick() {
        // Read input values
        String user = username.getText();
        password    = code.getText();

        // Validate input
        if (user.isEmpty() || password.isEmpty()) {
            lblError.setText("Please enter both username and password.");
            return;
        }

        // Send login request to server
        try {
            client.requestLogin(user, password);
        } catch (Exception ex) {
            lblError.setText("Failed to send login request.");
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
        lblError.setText(msg);
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
                client.sendToServer(new Object[] { "disconnect" });
                client.closeConnection();
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

    // ==============================
    // Internal Helpers
    // ==============================

    /**
     * Loads the appropriate main layout screen based on the user's role.
     * Passes required references to the next controller.
     *
     * @param user authenticated user returned by the server
     */
    private void navigateToHome(User user) {
        String fxml;
        UserRole role = user.getRole();

        // Determine target FXML based on role
        switch (role) {
            case Subscriber -> {
                fxml = "/client/SubscriberMainLayout.fxml";
                client.subscriberDetails(user); // Fetch full subscriber info from server
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
