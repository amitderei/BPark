package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import ui.UiUtils;

/**
 * Controller for the guest main screen.
 * Allows basic guest functionality like viewing availability,
 * and returning to the entry screen.
 */
public class GuestMainController {

    /** Button to return to the main welcome screen */
    @FXML
    private Button btnBack;

    /**
     * Triggered when the "Home" button is clicked.
     * This is already the home screen for guests.
     */
    @FXML
    private void handleHomeClick() {
        System.out.println("Home button clicked (guest already on home screen).");
    }

    /**
     * Triggered when the "Back" button is clicked.
     * Returns to the user type selection screen.
     */
    @FXML
    private void handleBackClick() {
        UiUtils.loadScreen(btnBack,
                           "/client/MainScreen.fxml",
                           "Select User Type",
                           null); // no client needed for guest
    }

    /**
     * Triggered when the "Check Parking Availability" button is clicked.
     */
    @FXML
    private void handleCheckAvailability() {
        System.out.println("Checking parking availability...");
    }
}
