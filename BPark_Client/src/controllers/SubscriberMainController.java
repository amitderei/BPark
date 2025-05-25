package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import java.io.IOException;

public class SubscriberMainController {

    @FXML
    private Label welcomeLabel; // Label to show subscriber's name

    @FXML
    private Button logoutButton; // Button for logout (linked in FXML)

    private String subscriberName; // Holds subscriber name passed from login

    /**
     * This method is called from Login after successful login
     */
    public void setSubscriberName(String name) {
        this.subscriberName = name;
        welcomeLabel.setText("Welcome, " + name + "!");
    }

    @FXML
    private void handleHomeClick() {
        System.out.println("Home button clicked (subscriber already on home screen)");
    }

    @FXML
    private void handleLogoutClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/UserTypeSelectionScreen.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Select User Type");
            stage.setWidth(800);
            stage.setHeight(500);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Each of these will be replaced with real functionality later
    @FXML private void handleViewPersonalInfo() {
        System.out.println("Viewing personal info...");
    }

    @FXML private void handleViewParkingHistory() {
        System.out.println("Viewing parking history...");
    }

    @FXML private void handleViewActiveParkingInfo() {
        System.out.println("Viewing active parking info...");
    }

    @FXML private void handleExtendParkingTime() {
        System.out.println("Extending parking time...");
    }

    @FXML private void handleSubmitVehicle() {
        System.out.println("Submitting vehicle...");
    }

    @FXML private void handleRetrieveVehicle() {
        System.out.println("Retrieving vehicle...");
    }

    @FXML private void handleParkingReservation() {
        System.out.println("Reserving parking...");
    }

    @FXML private void handleParkingCodeConfirmation() {
        System.out.println("Confirming parking code...");
    }
}

