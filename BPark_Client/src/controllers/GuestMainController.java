package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import java.io.IOException;

public class GuestMainController {
	@FXML
	private Button logoutButton;

    // This method is triggered when the "Home" button is clicked
    @FXML
    private void handleHomeClick() {
        // For a guest, home is the current screen — no action needed
        System.out.println("Home button clicked (guest already on home screen)");
    }

    // This method is triggered when the "Logout" button is clicked
    @FXML
    private void handleLogoutClick() {
        try {
            // Load the user type selection screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/UserTypeSelectionScreen.fxml"));
            Parent root = loader.load();

            // Get the current stage from the logout button
            Stage stage = (Stage) logoutButton.getScene().getWindow();

            // Set the new scene
            stage.setScene(new Scene(root));
            stage.setTitle("Select User Type");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // This method is triggered when the "Check Parking Availability" button is clicked
    @FXML
    private void handleCheckAvailability() {
        // For now, just print a placeholder message
        System.out.println("Checking parking availability...");
        // Later we'll load another screen or display results
    }
}

