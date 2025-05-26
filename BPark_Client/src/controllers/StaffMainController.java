package controllers;

import client.ClientController;
import common.User;
import common.UserRole;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import ui.UiUtils;

/**
 * Controller for the shared staff main screen (Attendant & Manager).
 * Sets button visibility and navigation according to the logged-in user's role.
 */
public class StaffMainController implements ClientAware {

    /* -------------------------------------------------------------
     * FXML-injected controls
     * ------------------------------------------------------------- */
    @FXML private Label  welcomeLabel;
    @FXML private Button btnLogout;               // renamed for consistency

    // Manager-only buttons
    @FXML private Button parkingReportButton;
    @FXML private Button subscriberReportButton;

    /* -------------------------------------------------------------
     * Runtime fields
     * ------------------------------------------------------------- */
    private ClientController client;

    /* -------------------------------------------------------------
     * ClientAware implementation
     * ------------------------------------------------------------- */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
    }

    /* -------------------------------------------------------------
     * Public API
     * ------------------------------------------------------------- */

    /**
     * Populates the screen with user-specific information and hides
     * manager-only actions when an attendant is logged in.
     *
     * @param user the logged-in user object
     */
    public void setUser(User user) {
        welcomeLabel.setText("Welcome, " + user.getUsername() + "!");

        if (user.getRole() == UserRole.Attendant) {
            parkingReportButton.setVisible(false);
            subscriberReportButton.setVisible(false);
        }
    }

    /* -------------------------------------------------------------
     * Toolbar actions
     * ------------------------------------------------------------- */

    /** Logs out and returns to the login screen. */
    @FXML
    private void handleLogoutClick() {
        UiUtils.loadScreen(btnLogout,
                           "/client/LoginScreen.fxml",
                           "BPARK – Login",
                           client);
    }

    /** Home button – already on home screen. */
    @FXML
    private void handleHomeClick() {
        UiUtils.showAlert("Home",
                          "You are already on the home screen.",
                          Alert.AlertType.INFORMATION);
    }

    /* -------------------------------------------------------------
     * Navigation buttons (placeholders)
     * ------------------------------------------------------------- */

    @FXML private void handleRegisterUsers()        { UiUtils.showAlert("Register",      "Register users functionality.",        Alert.AlertType.INFORMATION); }
    @FXML private void handleViewSubscriberInfo()   { UiUtils.showAlert("Subscribers",   "Viewing subscriber information.",      Alert.AlertType.INFORMATION); }
    @FXML private void handleViewActiveParkings()   { UiUtils.showAlert("Active",        "Viewing active parkings.",             Alert.AlertType.INFORMATION); }
    @FXML private void handleViewParkingReport()    { UiUtils.showAlert("Report",        "Viewing parking time report.",         Alert.AlertType.INFORMATION); }
    @FXML private void handleViewSubscriberReport() { UiUtils.showAlert("Report",        "Viewing subscriber status report.",    Alert.AlertType.INFORMATION); }
}
