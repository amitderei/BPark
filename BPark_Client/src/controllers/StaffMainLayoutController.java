package controllers;

import client.ClientController;
import common.Operation;
import common.User;
import common.UserRole;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import ui.UiUtils;

/**
 * Main layout for staff users (Attendant / Manager).
 * Displays a fixed top bar and side menu.
 * Swaps the center pane using loadScreen().
 */
public class StaffMainLayoutController implements ClientAware {

    /** "Home" button in the top bar */
    @FXML private Button btnHome;

    /** "Logout" button in the top bar */
    @FXML private Button btnLogout;

    /** "Exit" button in the top bar */
    @FXML private Button btnExit;

    /** Side-menu button to register new users */
    @FXML private Button btnRegisterUsers;

    /** Side-menu button to view subscriber information */
    @FXML private Button btnViewSubscriberInfo;

    /** Side-menu button to view currently active parking events */
    @FXML private Button btnViewActiveParkings;

    /** Side-menu button to view parking report (visible to managers only) */
    @FXML private Button btnViewParkingReport;

    /** Side-menu button to view subscriber status report (visible to managers only) */
    @FXML private Button btnViewSubscriberReport;

    /** Placeholder pane that swaps child screens */
    @FXML private AnchorPane center;

    /** Active client instance used for server communication */
    private ClientController client;

    /** Logged-in staff user (Manager or Attendant) */
    private User user;

    /**
     * Sets the shared client controller and registers this controller.
     *
     * @param client active client instance
     */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
        client.setStaffMainLayoutController(this);
    }

    /**
     * Sets the logged-in staff user and adjusts UI visibility
     * based on the user's role.
     *
     * @param user staff user object (Manager or Attendant)
     */
    public void setUser(User user) {
        this.user = user;

        if (user.getRole() == UserRole.Attendant) {
            btnViewSubscriberReport.setVisible(false);
            btnViewParkingReport.setVisible(false);
        }
    }

    /**
     * Automatically called after FXML is loaded.
     * Loads the default screen into the center pane.
     */
    @FXML
    private void initialize() {
        loadScreen("/client/StaffMainScreen.fxml");
    }

    /** Handles the Home button press — loads the main staff screen. */
    @FXML
    private void handleHomeClick() {
        loadScreen("/client/StaffMainScreen.fxml");
    }

    /** Handles the Logout button press — navigates back to the login screen. */
    @FXML
    private void handleLogoutClick() {
        UiUtils.loadScreen(btnLogout, "/client/LoginScreen.fxml", "BPARK – Login", client);
    }

    /** Loads the Register Subscriber screen. */
    @FXML
    private void handleRegisterUsers() {
        loadScreen("/client/RegisterSubscriberScreen.fxml");
    }

    /** Loads the screen showing all subscribers and their details. */
    @FXML
    private void handleViewSubscriberInfo() {
        loadScreen("/client/ViewSubscribersInfoScreen.fxml");
    }

    /** Loads the screen showing currently parked vehicles. */
    @FXML
    private void handleViewActiveParkings() {
        loadScreen("/client/ViewActiveParkingsScreen.fxml");
    }

    /** Loads the Parking Report screen for monthly stats. */
    @FXML
    private void handleViewParkingReport() {
        loadScreen("/client/ParkingReportScreen.fxml");
    }

    /** Displays a basic alert with a placeholder message for subscriber report. */
    @FXML
    private void handleViewSubscriberReport() {
        loadScreen("/client/SubscriberStatusScreen.fxml");
    }

    /**
     * Loads a child screen into the center pane and injects the client.
     * Also performs role-specific or controller-specific initial setup.
     *
     * @param fxml path to the FXML resource
     */
    public void loadScreen(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent content = loader.load();
            Object ctrl = loader.getController();

            if (ctrl instanceof ClientAware aware)
                aware.setClient(client);

            if (ctrl instanceof ViewSubscribersInfoController vsic) {
                client.setViewSubscribersInfoController(vsic);
                vsic.requestSubscribers();
            }

            if (ctrl instanceof ViewActiveParkingsController vapc) {
                client.setViewActiveParkingsController(vapc);
                vapc.requestActiveParkingEvents();
            }

            if (ctrl instanceof RegisterSubscriberController rsc) {
                client.setRegisterSubscriberController(rsc);
            }

            if (ctrl instanceof ParkingReportController prc) {
                client.setParkingReportController(prc);
                prc.getDatesOfReportsInDB();
            }
            
            if (ctrl instanceof SubscriberStatusController ssc) {
                client.setSubscriberStatusController(ssc);
                ssc.sendRequest();     
            }


            center.getChildren().setAll(content);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the Exit button press — disconnects from the server (if needed)
     * and closes the application.
     */
    @FXML
    private void handleExitClick() {
        UiUtils.exitFromSystem();
    }
}
