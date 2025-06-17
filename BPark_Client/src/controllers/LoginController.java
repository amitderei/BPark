package controllers;

import client.ClientController;
import common.User;
import common.UserRole;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import ui.UiUtils;

/**
 * Handles the login workflow:
 *  • Validates username / password fields  
 *  • Sends a login request to the server  
 *  • Reacts to success or failure callbacks from ClientController  
 *  • Routes the user to the correct main layout according to role
 */
public class LoginController implements ClientAware {

    /* ---------- FXML controls ---------- */
    @FXML private TextField username;
    @FXML private PasswordField code;
    @FXML private Button submit;
    @FXML private Label lblError;
    @FXML private Button backButton;
    @FXML private Button btnExit;

    /* ---------- runtime ---------- */
    private ClientController client;
    private String password;   // cached so we can forward to SubscriberMain

    /**
     * Saves the ClientController reference and
     * registers this object as the callback target for login events.
     *
     * @param client active client instance, injected by the parent screen
     */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
        client.setLoginController(this);
    }

    /* =====================================================
     *  UI event handlers
     * ===================================================== */

    /** Reads the fields, validates, and fires a login request. */
    @FXML
    private void handleLoginClick() {
        String user = username.getText();
        password    = code.getText();

        if (user.isEmpty() || password.isEmpty()) {
            lblError.setText("Please enter both username and password.");
            return;
        }
        try {
            client.requestLogin(user, password);
        } catch (Exception ex) {
            lblError.setText("Failed to send login request.");
            ex.printStackTrace();
        }
    }

    /** Server confirmed credentials – route to the proper main screen. */
    public void handleLoginSuccess(User user) {
        System.out.println("[DEBUG] Login successful, role = " + user.getRole());
        navigateToHome(user);
    }

    /** Server rejected credentials – show the reason. */
    public void handleLoginFailure(String msg) {
        lblError.setText(msg);
        System.err.println("[DEBUG] Login failed: " + msg);
    }

    /** Disconnects and quits the application. */
    @FXML
    private void handleExitClick() {
        try {
            if (client != null && client.isConnected()) {
                client.sendToServer(new Object[] { "disconnect" });
                client.closeConnection();
            }
        } catch (Exception ignored) { }
        Platform.exit();
        System.exit(0);
    }

    /** Returns to the entry screen (Guest / Login choice). */
    @FXML
    private void handleBack() {
        UiUtils.loadScreen(backButton,
                           "/client/MainScreen.fxml",
                           "BPARK – Welcome",
                           client);
    }

    /* =====================================================
     *  Private helpers
     * ===================================================== */

    /**
     * Loads the main layout that matches the user’s role
     * and transfers required objects to that controller.
     *
     * @param user authenticated user returned by the server
     */
    private void navigateToHome(User user) {

        String fxml;
        UserRole role = user.getRole();

        switch (role) {
            case Subscriber -> {
                fxml = "/client/SubscriberMainLayout.fxml";
                client.subscriberDetails(user);   // fetch full subscriber info
            }
            case Attendant, Manager -> fxml = "/client/StaffMainLayout.fxml";
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

            /* --- role-specific wiring --- */
            if (ctrl instanceof SubscriberMainLayoutController sub) {
                client.setMainLayoutController(sub);
                sub.setClient(client);
                sub.setSubscriberName(username.getText().trim());
                sub.loadScreen("/client/SubscriberMainScreen.fxml");
                client.setPassword(password);   // cache for later edits
            }

            if (ctrl instanceof StaffMainLayoutController staff) {
                staff.setClient(client);
                staff.setUser(user);
            }

            /* --- swap scene --- */
            Stage stage = (Stage) submit.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("BPARK – " + role);
            stage.show();

        } catch (Exception ex) {
            UiUtils.showAlert("BPARK – Error",
                              "Failed to load " + role + " screen.",
                              Alert.AlertType.ERROR);
            ex.printStackTrace();
        }
    }
}
