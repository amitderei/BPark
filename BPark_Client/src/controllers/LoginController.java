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
 * Controller for the shared login screen.
 * Handles login logic for Subscriber, Attendant, and Manager roles.
 * Allows navigating back to user type selection screen and forwards to
 * the relevant home screen upon successful login.
 */
public class LoginController implements ClientAware {

    /** Text field for user ID input */
    @FXML private TextField username;

    /** Password field for password input */
    @FXML private PasswordField code;

    /** Button to trigger login */
    @FXML private Button submit;

    /** Label for displaying login error messages */
    @FXML private Label lblError;

    /** Button to navigate back to the user type screen */
    @FXML private Button backButton;

    /** Reference to the active client connection */
    private ClientController client;

    /** The user role selected before login (e.g., Subscriber, Manager) */
    private UserRole userRole;

    /**
     * Sets the connected ClientController instance.
     *
     * @param client the active client connection
     */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
    }

    /**
     * Sets the user role selected before login.
     *
     * @param role the role to set (e.g., Subscriber, Manager)
     */
    public void setUserRole(UserRole role) {
        this.userRole = role;
    }

    /**
     * Handles the Back button action.
     * Navigates back to the user type selection screen.
     */
    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/UserTypeSelectionScreen.fxml"));
            Parent root = loader.load();

            // Set client on the next controller
            Object controller = loader.getController();
            if (controller instanceof ClientAware) {
                ((ClientAware) controller).setClient(client);
            }

            // Replace current scene with user type selection
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
     * Validates credentials and navigates to the corresponding user home screen.
     */
    @FXML
    private void handleLoginClick() {
        String userId = username.getText().trim();
        String password = code.getText().trim();

        if (userId.isEmpty() || password.isEmpty()) {
            lblError.setText("Please enter both ID and password.");
            return;
        }



    }

    /**
     * Navigates to the appropriate home screen based on the user's role.
     *
     * @param role the user's role (Subscriber, Attendant, or Manager)
     */
    private void navigateToHome(UserRole role) {
        String fxmlPath;

        switch (role) {
            case Subscriber -> fxmlPath = "/client/SubscriberHome.fxml";
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
            if (controller instanceof ClientAware) {
                ((ClientAware) controller).setClient(client);
            }

            // Replace current scene with the relevant home screen
            Stage stage = (Stage) submit.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            UiUtils.showAlert("BPARK - Error", "Failed to load " + role + " screen.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }


}
