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
 * Controller for the login screen of BPARK.
 * Sends login requests to the server and handles navigation based on user role.
 */
public class LoginController implements ClientAware {

    @FXML private TextField username;
    @FXML private PasswordField code;
    @FXML private Button submit;
    @FXML private Label lblError;
    @FXML private Button backButton;

    private ClientController client;
    private UserRole userRole;

    /**
     * Sets the ClientController used to communicate with the server.
     *
     * @param client the client instance to assign
     */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
        client.setLoginController(this);
    }

    /**
     * Sets the user role that was selected prior to login.
     *
     * @param role the role (Subscriber, Attendant, Manager)
     */
    public void setUserRole(UserRole role) {
        this.userRole = role;
    }

    /**
     * Navigates back to the user type selection screen when the "Back" button is clicked.
     */
    @FXML
    private void handleBack() {
        try {
            // 1. Load the FXML for the user-type selection screen
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/client/UserTypeSelectionScreen.fxml"));
            Parent root = loader.load();

            // 2. Propagate the active ClientController to the next screen (if it needs it)
            Object controller = loader.getController();
            if (controller instanceof ClientAware aware) {
                aware.setClient(client); // keep the existing server connection alive
            }

            // 3. Switch the current window to the newly loaded scene
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            // 4. Show a user-friendly error dialog and print the stack trace for debugging
            UiUtils.showAlert("BPARK - Error",
                    "Failed to return to user type screen: " + e.getMessage(),
                    Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }


    /**
     * Triggered when the login button is clicked.
     * Validates input fields and sends a login request to the server.
     */
    @FXML
    private void handleLoginClick() {
        // --- 1. Read user input from the text-fields ---
        String userId   = username.getText();   
        String password = code.getText();       

        // --- 2. Basic client-side validation ---
        if (userId.isEmpty() || password.isEmpty()) {
            lblError.setText("Please enter both ID and password.");
            return; // don’t bother the server with invalid data
        }

        try {
            // --- 3. Send a login request to the server (OCSF protocol) ---
            // Format: ["login", userId, password]
            client.sendToServer(new Object[]{"login", userId, password});
        } catch (Exception e) {
            // --- 4. Communication failure: inform the user & log details ---
            lblError.setText("Failed to send login request.");
            System.err.println("[ERROR] Failed to send login request: " + e.getMessage());
        }
    }


    /**
     * Called by the client when login is successful.
     * Navigates to the appropriate screen based on user role.
     * Validates that the logged-in user's role matches the expected one.
     *
     * @param user the authenticated user returned by the server
     */
    public void handleLoginSuccess(User user) {
        System.out.println("[DEBUG] Login successful on client. Validating role...");

        // --- 1. Verify that the role returned by the server matches the one the user selected ---
        if (user.getRole() != userRole) {
            String expected = userRole.toString();
            String actual   = user.getRole().toString();

            // Show an error dialog and log the mismatch for debugging
            UiUtils.showAlert(
                    "Login Error",
                    "You attempted to log in as a " + expected +
                    ", but the user is actually a " + actual + ".",
                    Alert.AlertType.ERROR);

            System.err.println("[DEBUG] Role mismatch: expected " + expected + ", but got " + actual);
            return; // abort navigation
        }

        // --- 2. Role verified – proceed to the relevant home screen ---
        System.out.println("[DEBUG] Role validated. Navigating to home...");
        navigateToHome(user.getRole());
    }



    /**
     * Navigates the user to their corresponding home screen based on role.
     *
     * @param role the user's role
     */
    private void navigateToHome(UserRole role) {
        String fxmlPath;

        // --- 1. Map each role to its dedicated FXML screen ---
        switch (role) {
            case Subscriber -> fxmlPath = "/client/SubscriberHomeScreen.fxml";
            case Attendant  -> fxmlPath = "/client/AttendantHome.fxml";
            case Manager    -> fxmlPath = "/client/ManagerHome.fxml";
            default -> { // safety-net: unknown role
                UiUtils.showAlert("BPARK - Error",
                        "Unknown role: " + role,
                        Alert.AlertType.ERROR);
                return;
            }
        }

        try {
            // --- 2. Load the selected FXML and obtain its controller ---
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // --- 3. Pass the active ClientController to the new screen (if it needs it) ---
            Object controller = loader.getController();
            if (controller instanceof ClientAware aware) {
                aware.setClient(client); // preserve existing server connection
            }

            // --- 4. Replace the current scene with the newly loaded one ---
            Stage stage = (Stage) submit.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            // --- 5. Display a user-friendly error dialog and log the stack trace ---
            UiUtils.showAlert("BPARK - Error",
                    "Failed to load " + role + " screen.",
                    Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }


    /**
     * Displays a login error message (e.g., invalid credentials).
     *
     * @param message the error message to display to the user
     */
    public void handleLoginFailure(String message) {
        lblError.setText(message);
        System.err.println("[DEBUG] Login failed: " + message);
    }
    
    
}

