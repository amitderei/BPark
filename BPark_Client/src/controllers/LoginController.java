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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/UserTypeSelectionScreen.fxml"));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof ClientAware aware) {
                aware.setClient(client);
            }

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            UiUtils.showAlert("BPARK - Error", "Failed to return to user type screen: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Triggered when the login button is clicked.
     * Validates input fields and sends a login request to the server.
     */
    @FXML
    private void handleLoginClick() {
        String userId = username.getText().trim();
        String password = code.getText().trim();

        if (userId.isEmpty() || password.isEmpty()) {
            lblError.setText("Please enter both ID and password.");
            return;
        }

        try {
            client.sendToServer(new Object[] { "login", userId, password });
        } catch (Exception e) {
            lblError.setText("Failed to send login request.");
            System.err.println("[ERROR] Failed to send login request: " + e.getMessage());
        }
    }

    /**
     * Called by ClientController when the login is successful.
     * Redirects the user to the appropriate home screen.
     *
     * @param user the authenticated user returned by the server
     */
    public void handleLoginSuccess(User user) {
        System.out.println("[DEBUG] Login successful on client. Navigating to home...");
        navigateToHome(user.getRole());
    }

    /**
     * Navigates the user to their corresponding home screen based on role.
     *
     * @param role the user's role
     */
    private void navigateToHome(UserRole role) {
        String fxmlPath;

        switch (role) {
            case Subscriber -> fxmlPath = "/client/SubscriberHomeScreen.fxml";
            case Attendant  -> fxmlPath = "/client/AttendantHome.fxml";
            case Manager    -> fxmlPath = "/client/ManagerHome.fxml";
            default -> {
                UiUtils.showAlert("BPARK - Error", "Unknown role: " + role, Alert.AlertType.ERROR);
                return;
            }
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof ClientAware aware) {
                aware.setClient(client);
            }

            Stage stage = (Stage) submit.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            UiUtils.showAlert("BPARK - Error", "Failed to load " + role + " screen.", Alert.AlertType.ERROR);
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

