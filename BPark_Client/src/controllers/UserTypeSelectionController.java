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
 * Navigates to the guest screen or shared login screen depending on user type.
 */
public class UserTypeSelectionController implements ClientAware {

    private ClientController client;

    /** Button used to trigger guest login. Used to access the active stage safely. */
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
     * Sets the connected client instance.
     *
     * @param client the active client connection
     */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
    }

    /**
     * Handles Guest selection — navigates to the Guest screen.
     */
    @FXML
    public void handleGuest() {
        loadScreen("/client/GuestScreen.fxml", null);
    }

    /**
     * Handles Subscriber selection — navigates to the shared login screen with role.
     */
    @FXML
    public void handleSubscriber() {
        loadScreen("/client/LoginScreen.fxml", UserRole.Subscriber);
    }

    /**
     * Handles Attendant selection — navigates to the shared login screen with role.
     */
    @FXML
    public void handleAttendant() {
        loadScreen("/client/LoginScreen.fxml", UserRole.Attendant);
    }

    /**
     * Handles Manager selection — navigates to the shared login screen with role.
     */
    @FXML
    public void handleManager() {
        loadScreen("/client/LoginScreen.fxml", UserRole.Manager);
    }

    /**
     * Loads a new FXML screen and passes the client and user role if applicable.
     * Uses guestBtn to retrieve the active window stage reliably.
     *
     * @param path the FXML path to load
     * @param role the user role (null if Guest)
     */
    private void loadScreen(String path, UserRole role) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof ClientAware) {
                ((ClientAware) controller).setClient(client);
            }
            if (controller instanceof LoginController && role != null) {
                ((LoginController) controller).setUserRole(role);
            }

            // Use the scene from an existing button to retrieve the current Stage
            Stage stage = (Stage) guestBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            System.err.println("Failed to load screen: " + path);
            e.printStackTrace();
        }
    }
}

