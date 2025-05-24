package controllers;

import client.ClientController;
import common.UserRole;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

/**
 * Controller for the user type selection screen.
 * Navigates to the login or guest screen based on the selected user type.
 */
public class UserTypeSelectionController implements ClientAware {

    private ClientController client;

    /** Button used to retrieve the current window (stage) */
    @FXML
    private Button guestBtn;

    /** Button for subscriber login */
    @FXML
    private Button subscriberBtn;

    /** Button for attendant login */
    @FXML
    private Button attendantBtn;

    /** Button for manager login */
    @FXML
    private Button managerBtn;

    /**
     * Injects the active ClientController for communication with the server.
     *
     * @param client the active client instance
     */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
    }

    /**
     * Handles selection of guest user type and navigates to the guest screen.
     */
    @FXML
    public void handleGuest() {
        loadScreen("/client/GuestScreen.fxml", null);
    }

    /**
     * Handles selection of subscriber user type and navigates to login screen.
     */
    @FXML
    public void handleSubscriber() {
        loadScreen("/client/LoginScreen.fxml", UserRole.Subscriber);
    }

    /**
     * Handles selection of attendant user type and navigates to login screen.
     */
    @FXML
    public void handleAttendant() {
        loadScreen("/client/LoginScreen.fxml", UserRole.Attendant);
    }

    /**
     * Handles selection of manager user type and navigates to login screen.
     */
    @FXML
    public void handleManager() {
        loadScreen("/client/LoginScreen.fxml", UserRole.Manager);
    }

    /**
     * Loads a new screen and passes both the client and expected user role (if applicable).
     *
     * @param fxmlPath the relative path to the FXML screen
     * @param role     the expected role for login (null if guest)
     */
    private void loadScreen(String fxmlPath, UserRole role) {
        try {
            /* 1. Load FXML + controller */
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent     root   = loader.load();

            /* 2. Inject ClientController into the new controller (if it wants one) */
            Object ctrl = loader.getController();
            if (ctrl instanceof ClientAware aware) {
                aware.setClient(client);
            }

            /* 3. If this is the login screen, tell it which role to expect */
            if (ctrl instanceof LoginController loginCtrl && role != null) {
                loginCtrl.setUserRole(role);
            }

            /* 4. Swap current scene with the newly loaded one */
            Stage stage = (Stage) guestBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            System.err.println("Failed to load screen: " + fxmlPath);
            e.printStackTrace();
        }
    }
}


