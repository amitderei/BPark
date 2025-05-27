package controllers;

import client.ClientController;
import common.User;
import common.UserRole;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import ui.UiUtils;

/**
 * Controller for the login screen of the BPARK system.
 * Handles input validation, login request, and post-login navigation.
 */
public class LoginController implements ClientAware {

    /* ------------------------------------------------------------------
     *  FXML controls
     * ------------------------------------------------------------------ */
    @FXML private TextField      username;
    @FXML private PasswordField  code;
    @FXML private Button         submit;
    @FXML private Label          lblError;
    @FXML private Button         backButton;

    /* ------------------------------------------------------------------
     *  Runtime
     * ------------------------------------------------------------------ */
    private ClientController client;

    /** Injects the active client and registers callback for login result. */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
        client.setLoginController(this);
    }

    /* ------------------------------------------------------------------
     *  Login flow
     * ------------------------------------------------------------------ */

    /** Triggered when the user clicks “Login”. */
    @FXML
    private void handleLoginClick() {
        String user = username.getText();
        String pass = code.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            lblError.setText("Please enter both username and password.");
            return;
        }
        try {
            client.requestLogin(user, pass);
        } catch (Exception ex) {
            lblError.setText("Failed to send login request.");
            ex.printStackTrace();
        }
    }

    /** Called by ClientController on successful authentication. */
    public void handleLoginSuccess(User user) {
        System.out.println("[DEBUG] Login successful, role = " + user.getRole());
        navigateToHome(user);
    }

    /** Called by ClientController on failed authentication. */
    public void handleLoginFailure(String msg) {
        lblError.setText(msg);
        System.err.println("[DEBUG] Login failed: " + msg);
    }

    /* ------------------------------------------------------------------
     *  Navigation helpers
     * ------------------------------------------------------------------ */

    /**
     * Loads the appropriate main screen according to the user's role,
     * injecting both ClientController and User where needed.
     */
    private void navigateToHome(User user) {
        String fxml;
        UserRole role = user.getRole();

        switch (role) {
            case Subscriber         -> fxml = "/client/SubscriberMainScreen.fxml";
            case Attendant, Manager -> fxml = "/client/StaffMainScreen.fxml";
            default -> {
                UiUtils.showAlert("BPARK – Error",
                                  "Unknown role: " + role,
                                  Alert.AlertType.ERROR);
                return;
            }
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Object ctrl = loader.getController();

            // Inject client
            if (ctrl instanceof ClientAware aware) {
                aware.setClient(client);
            }
            // Extra data per role
            if (ctrl instanceof StaffMainController staff) {
                staff.setUser(user);
            }
            if (ctrl instanceof SubscriberMainController sub) {
                sub.setSubscriberName(username.getText().trim());
            }

            Stage stage = (Stage) submit.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("BPARK – " + role);
            stage.show();

        } catch (Exception ex) {
            UiUtils.showAlert("BPARK – Error",
                              "Failed to load " + role + " screen.",
                              Alert.AlertType.ERROR);
            ex.printStackTrace();
        }
    }

    /** Returns to the entry screen (Guest / Login choice). */
    @FXML
    private void handleBack() {
        UiUtils.loadScreen(backButton,
                           "/client/MainScreen.fxml",
                           "BPARK – Welcome",
                           client);
    }
}

