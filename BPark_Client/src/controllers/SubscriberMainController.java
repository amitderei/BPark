package controllers;

import client.ClientController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import ui.UiUtils;

/**
 * Controller for the Subscriber's main screen in the BPARK system.
 * Displays a personalized greeting and provides access to subscriber actions.
 */
public class SubscriberMainController implements ClientAware {

    /** Label displaying the subscriber's name */
    @FXML
    private Label welcomeLabel;

    /** Logout button that returns to the main entry screen */
    @FXML
    private Button logoutButton;

    /** The name of the subscriber, injected from login */
    private String subscriberName;

    /** Client controller used to communicate with the server */
    private ClientController client;

    /**
     * Injects the client controller after screen is loaded.
     *
     * @param client the active client instance
     */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
    }

    /**
     * Called from the login controller to display a personalized welcome message.
     * If the FXML field was not yet injected, the label will not be updated.
     *
     * @param name the subscriber's name
     */
    public void setSubscriberName(String name) {
        this.subscriberName = name;

        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + name + "!");
        }
    }

    /**
     * Handles "Home" button click. Since this is the home screen, does nothing.
     */
    @FXML
    private void handleHomeClick() {
        System.out.println("Home button clicked (already on home screen)");
    }

    /**
     * Handles logout by returning the user to the main screen (Login/Guest).
     */
    @FXML
    private void handleLogoutClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/MainScreen.fxml"));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof ClientAware aware) {
                aware.setClient(client);
            }

            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("BPARK - Welcome");
            stage.show();

        } catch (Exception e) {
            UiUtils.showAlert("BPARK - Error",
                    "Failed to load main screen: " + e.getMessage(),
                    javafx.scene.control.Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // -------------------------
    // Navigation button handlers
    // -------------------------

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


