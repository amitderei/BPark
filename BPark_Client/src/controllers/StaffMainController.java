package controllers;

import client.ClientController;
import common.User;
import common.UserRole;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import ui.UiUtils;

/**
 * Controller for the shared staff main screen (Attendant & Manager).
 * Controls button visibility, navigation, and actions based on the logged-in user's role.
 */
public class StaffMainController implements ClientAware {

    @FXML private Label welcomeLabel;
    @FXML private Button logoutButton;

    // Manager-only buttons
    @FXML private Button parkingReportButton;
    @FXML private Button subscriberReportButton;

    /** Reference to the logged-in user */
    private User currentUser;

    /** Reference to the client controller for server communication */
    private ClientController client;

    /**
     * Injects the client instance into this controller.
     *
     * @param client the ClientController instance
     */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
    }

    /**
     * Sets the logged-in user and adjusts the screen based on their role.
     *
     * @param user the logged-in User object
     */
    public void setUser(User user) {
        this.currentUser = user;
        welcomeLabel.setText("Welcome, " + user.getUsername() + "!");

        // Hide manager-only buttons for attendants
        if (user.getRole() == UserRole.Attendant) {
            parkingReportButton.setVisible(false);
            subscriberReportButton.setVisible(false);
        }
    }

    /**
     * Handles logout action and navigates back to the login screen.
     */
    @FXML
    private void handleLogoutClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/LoginScreen.fxml"));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof ClientAware aware) {
                aware.setClient(client);
            }

            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("BPARK - Login");
            stage.show();

        } catch (Exception e) {
            UiUtils.showAlert("BPARK - Error",
                    "Failed to load login screen: " + e.getMessage(),
                    Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Handles home button click.
     */
    @FXML
    private void handleHomeClick() {
        UiUtils.showAlert("Home", "You are already on the home screen.", Alert.AlertType.INFORMATION);
    }

    /**
     * Handles the "Register Users" button action.
     */
    @FXML
    private void handleRegisterUsers() {
        UiUtils.showAlert("Register", "Register users functionality.", Alert.AlertType.INFORMATION);
    }

    /**
     * Handles the "View Subscriber Info" button action.
     */
    @FXML
    private void handleViewSubscriberInfo() {
        UiUtils.showAlert("Subscribers", "Viewing subscriber information.", Alert.AlertType.INFORMATION);
    }

    /**
     * Handles the "View Active Parkings" button action.
     */
    @FXML
    private void handleViewActiveParkings() {
        UiUtils.showAlert("Active Parkings", "Viewing active parkings.", Alert.AlertType.INFORMATION);
    }

    /**
     * Handles the "View Parking Time Report" button action (Manager only).
     */
    @FXML
    private void handleViewParkingReport() {
        UiUtils.showAlert("Report", "Viewing parking time report.", Alert.AlertType.INFORMATION);
    }

    /**
     * Handles the "View Subscriber Status Report" button action (Manager only).
     */
    @FXML
    private void handleViewSubscriberReport() {
        UiUtils.showAlert("Report", "Viewing subscriber status report.", Alert.AlertType.INFORMATION);
    }
}
