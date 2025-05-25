package controllers;

import client.ClientController;
import common.User;
import common.UserRole;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import ui.UiUtils;

/**
 * Controller for the login screen of the BPARK system.
 * Handles login input, validation, request sending, and post-login redirection.
 */
public class LoginController implements ClientAware {

    /** Text field for entering username */
    @FXML private TextField username;

    /** Password field for entering user's code (password) */
    @FXML private PasswordField code;

    /** Submit button to trigger login */
    @FXML private Button submit;

    /** Label to display error messages to the user */
    @FXML private Label lblError;

    /** Button to go back to the previous screen */
    @FXML private Button backButton;

    /** Client controller instance used for server communication */
    private ClientController client;

    /**
     * Sets the client instance for communication with the server.
     * Also registers this controller to receive login results.
     *
     * @param client the connected client instance
     */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
        client.setLoginController(this); // so client can call back upon login response
    }

    /**
     * Handles the login button click.
     * Sends a login request to the server using the entered credentials.
     * Shows an error label if fields are empty.
     */
    @FXML
    private void handleLoginClick() {
        // Get user input
        String username = this.username.getText().trim();   
        String password = code.getText().trim();       

        // Validate input
        if (username.isEmpty() || password.isEmpty()) {
            lblError.setText("Please enter both username and password.");
            return;
        }

        // Send login request to server
        try {
            client.requestLogin(username, password); // Role is no longer sent
        } catch (Exception e) {
            lblError.setText("Failed to send login request.");
            System.err.println("[ERROR] Failed to send login request: " + e.getMessage());
        }
    }

    /**
     * Called by the client controller when login is successful.
     * Navigates the user to their designated home screen based on role.
     *
     * @param user the authenticated User object returned from the server
     */
    public void handleLoginSuccess(User user) {
        System.out.println("[DEBUG] Login successful. User role: " + user.getRole());

        // Navigate to home screen based on role
        navigateToHome(user.getRole());
    }

    /**
     * Navigates the user to the corresponding home screen based on their role.
     *
     * @param role the user's assigned role
     */
    private void navigateToHome(UserRole role) {
        String fxmlPath;

        // Choose FXML path based on user role
        switch (role) {
            case Subscriber -> fxmlPath = "/client/SubscriberMainScreen.fxml";
            case Attendant  -> fxmlPath = "/client/AttendantMainScreen.fxml";
            case Manager    -> fxmlPath = "/client/ManagerMainScreen.fxml";
            default -> {
                UiUtils.showAlert("BPARK - Error", "Unknown role: " + role, Alert.AlertType.ERROR);
                return;
            }
        }

        try {
            // Load the appropriate screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();

            // Inject the client into the new controller
            if (controller instanceof ClientAware aware) {
                aware.setClient(client);
            }

            // Pass username to subscriber screen (optional enhancement)
            if (role == UserRole.Subscriber && controller instanceof SubscriberMainController subController) {
                subController.setSubscriberName(username.getText().trim());
            }

            // Switch to the new screen
            Stage stage = (Stage) submit.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            UiUtils.showAlert("BPARK - Error",
                    "Failed to load " + role + " screen.",
                    Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Displays a login error message in the UI.
     * Typically triggered after failed authentication by the server.
     *
     * @param message the error message to display
     */
    public void handleLoginFailure(String message) {
        lblError.setText(message);
        System.err.println("[DEBUG] Login failed: " + message);
    }

    /**
     * Handles the "Back" button click.
     * Returns the user to the main entry screen with login/guest options.
     */
    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/MainScreen.fxml"));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof ClientAware aware) {
                aware.setClient(client);
            }

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("BPARK - Welcome");
            stage.show();

        } catch (Exception e) {
            UiUtils.showAlert("BPARK - Error",
                    "Failed to return to main screen: " + e.getMessage(),
                    Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
}

