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
 * Main layout for staff users (attendant / manager). Fixed parts: top bar and
 * side-menu. The centre pane swaps screens with loadScreen().
 */
public class StaffMainLayoutController implements ClientAware {

	/* ---------- top bar buttons ---------- */
	@FXML
	private Button btnHome;
	@FXML
	private Button btnLogout;
	@FXML
	private Button btnExit;

	/* ---------- side-menu buttons ---------- */
	@FXML
	private Button btnRegisterUsers;
	@FXML
	private Button btnViewSubscriberInfo;
	@FXML
	private Button btnViewActiveParkings;
	@FXML
	private Button btnViewParkingReport;
	@FXML
	private Button btnViewSubscriberReport;

	/* ---------- placeholder for child screens ---------- */
	@FXML
	private AnchorPane center;

	/* ---------- runtime state ---------- */
	private ClientController client; // shared socket handler
	private User user; // logged-in staff account

	/**
	 * Injects the ClientController so child screens can talk to the server.
	 *
	 * @param client active client instance
	 */
	@Override
	public void setClient(ClientController client) {
		this.client = client;
		client.setStaffMainLayoutController(this); // let the client call back
	}

	/**
	 * Stores the staff user and hides manager-only buttons when the role is
	 * Attendant.
	 *
	 * @param user authenticated staff account
	 */
	public void setUser(User user) {
		this.user = user;

		// attendant has no access to the two report buttons
		if (user.getRole() == UserRole.Attendant) {
			btnViewSubscriberReport.setVisible(false);
			btnViewParkingReport.setVisible(false);
		}
	}

	/* ---------- first load ---------- */

	@FXML
	private void initialize() {
		loadScreen("/client/StaffMainScreen.fxml");
	}

	/* ---------- top-bar handlers ---------- */

	@FXML
	private void handleHomeClick() {
		loadScreen("/client/StaffMainScreen.fxml");
	}

	@FXML
	private void handleLogoutClick() {
		UiUtils.loadScreen(btnLogout, "/client/LoginScreen.fxml", "BPARK – Login", client);
	}

	/* ---------- side-menu handlers ---------- */

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
		loadScreen("/client/ParkingReportScreen.fxml");
	}

	@FXML
	private void handleViewSubscriberReport() {
		UiUtils.showAlert("Report", "Viewing subscriber status report.", Alert.AlertType.INFORMATION);
	}

	/*
	 * ===================================================== Screen loader
	 * =====================================================
	 */

	/**
	 * Replaces the center pane with the given FXML and passes the ClientController
	 * to its controller when needed.
	 *
	 * @param fxml resource path of the child screen
	 */
	public void loadScreen(String fxml) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
			Parent content = loader.load();

			Object ctrl = loader.getController();

			// generic injection for any ClientAware screen
			if (ctrl instanceof ClientAware aware)
				aware.setClient(client);

			// register specific controllers for push-callbacks
			if (ctrl instanceof ViewSubscribersInfoController vsic) {
				client.setViewSubscribersInfoController(vsic);
				vsic.requestSubscribers(); // pull data immediately
			}
			if (ctrl instanceof ViewActiveParkingsController vapc) {
				client.setViewActiveParkingsController(vapc);
				vapc.requestActiveParkingEvents();
			}
			if (ctrl instanceof RegisterSubscriberController rsc) {
				client.setRegisterSubscriberController(rsc);
			}
			if(ctrl instanceof ParkingReportController prc) {
				client.setParkingReportController(prc);
				prc.getParkingReportFromServer();
				
			}

			center.getChildren().setAll(content);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Disconnects from the server (if connected) and quits. Called by the Exit
	 * button.
	 */
	@FXML
	private void handleExitClick() {
		try {
			if (client != null && client.isConnected()) {
				client.sendToServer(new Object[] { "disconnect" });
				client.closeConnection();
			}
		} catch (Exception ignored) {
		}
		javafx.application.Platform.exit();
		System.exit(0);
	}
}
