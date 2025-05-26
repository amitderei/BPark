package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import ui.UiUtils;

/**
 * Controller for the guest's main screen in the BPARK system.
 * Handles guest-level navigation and actions.
 */
public class GuestMainController {

    /** Button used to log out and return to the entry screen. */
    @FXML
    private Button logoutButton;

    /**
     * Triggered when the "Home" button is clicked.
     * For a guest, this is already the home screen.
     */
    @FXML
    private void handleHomeClick() {
        System.out.println("Home button clicked (guest already on home screen).");
    }

    /**
     * Triggered when the "Logout" button is clicked.
     * Returns the user to the guest/login selection screen.
     */
    @FXML
    private void handleLogoutClick() {
        UiUtils.loadScreen(logoutButton,
                           "/client/UserTypeSelectionScreen.fxml",
                           "Select User Type",
                           null); // guest does not require ClientController
    }

    /**
     * Triggered when the "Check Parking Availability" button is clicked.
     * Placeholder logic for future parking availability feature.
     */
    @FXML
    private void handleCheckAvailability() {
        System.out.println("Checking parking availability...");
        // Future: load a screen or display actual availability
    }
}

