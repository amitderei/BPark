package controllers;

import client.ClientController;
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
 * Controller for the staff main layout.
 * Loads center screens and handles role-based visibility.
 */
public class StaffMainLayoutController implements ClientAware {

    @FXML private Button btnHome;
    @FXML private Button btnLogout;
    @FXML private Button btnRegisterUsers;
    @FXML private Button btnViewSubscriberInfo;
    @FXML private Button btnViewActiveParkings;
    @FXML private Button btnViewParkingReport;
    @FXML private Button btnViewSubscriberReport;
    @FXML private Button btnExit;

    @FXML private AnchorPane center;

    private ClientController client;
    private User user;

    @Override
    public void setClient(ClientController client) {
        this.client = client;
        client.setStaffMainLayoutController(this);
    }

    public void setUser(User user) {
        this.user = user;

        if (btnViewSubscriberReport != null && btnViewParkingReport != null) {
            if (user.getRole() == UserRole.Attendant) {
                btnViewSubscriberReport.setVisible(false);
                btnViewParkingReport.setVisible(false);
            }
        }
    }

    @FXML
    private void initialize() {
        loadScreen("/client/StaffMainScreen.fxml");
    }

    @FXML
    private void handleHomeClick() {
        loadScreen("/client/StaffMainScreen.fxml");
    }

    @FXML
    private void handleLogoutClick() {
        UiUtils.loadScreen(btnLogout, "/client/LoginScreen.fxml", "BPARK – Login", client);
    }

    @FXML
    private void handleRegisterUsers() {
    	loadScreen("/client/RegisterSubscriberScreen.fxml");
    }

    @FXML
    private void handleViewSubscriberInfo() {
        loadScreen("/client/ViewSubscribersInfoScreen.fxml");
    }

    @FXML
    private void handleViewActiveParkings() {
        loadScreen("/client/ViewActiveParkingsScreen.fxml");
    }

    @FXML
    private void handleViewParkingReport() {
        UiUtils.showAlert("Report", "Viewing parking time report.", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleViewSubscriberReport() {
        UiUtils.showAlert("Report", "Viewing subscriber status report.", Alert.AlertType.INFORMATION);
    }

    /**
     * Loads a screen into the center of the layout and injects the client.
     */
    public void loadScreen(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent content = loader.load();

            Object ctrl = loader.getController();

            if (client != null) {
                if (ctrl instanceof ClientAware aware)
                    aware.setClient(client);

                if (ctrl instanceof ViewSubscribersInfoController controller) {
                    client.setViewSubscribersInfoController(controller);
                    controller.requestSubscribers();
                }

                if (ctrl instanceof ViewActiveParkingsController controller) {
                    client.setViewActiveParkingsController(controller);
                    controller.requestActiveParkingEvents();
                }
                
                if (ctrl instanceof RegisterSubscriberController controller) {
                    client.setRegisterSubscriberController(controller);
                }
            }

            center.getChildren().clear();
            center.getChildren().add(content);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Terminates the application gracefully when 'Exit' is clicked.
     */
    @FXML
    private void handleExitClick() {
        try {
            if (client != null && client.isConnected()) {
                client.sendToServer(new Object[]{"disconnect"});
                client.closeConnection();
            }
        } catch (Exception e) {
            // Optional: log error
        }
        javafx.application.Platform.exit();
        System.exit(0);
    }
}

