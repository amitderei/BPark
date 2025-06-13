package controllers;

import client.ClientController;
import common.User;
import common.Subscriber;
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
 * Controller for the login screen of the BPARK system. Handles input
 * validation, login request, and post-login navigation.
 */
public class LoginController implements ClientAware {


	@FXML
	private TextField username;
	@FXML
	private PasswordField code;
	@FXML
	private Button submit;
	@FXML
	private Label lblError;
	@FXML
	private Button backButton;
	@FXML
	private Button btnExit;

	private ClientController client;
	private String password;

	/** Injects the active client and registers callback for login result. */
	@Override
	public void setClient(ClientController client) {
		this.client = client;
		client.setLoginController(this);
	}



	/** Triggered when the user clicks “Login”. */
	@FXML
	private void handleLoginClick() {
		String user = username.getText();
		password = code.getText();

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

	/**
	 * Handles the "Exit" button click. Gracefully disconnects from the server and
	 * terminates the application.
	 */
	
	@FXML
	private void handleExitClick() {

			try {
				if (client != null && client.isConnected()) {
					client.sendToServer(new Object[] { "disconnect" });
					client.closeConnection();
					System.out.println("Client disconnected successfully.");
				}
			} catch (Exception e) {
				System.err.println("Failed to disconnect client: " + e.getMessage());
			}
			Platform.exit();
			System.exit(0);
		}

	/**
	 * Loads the appropriate main screen according to the user's role, injecting
	 * both ClientController and User where needed.
	 */
	private void navigateToHome(User user) {
		String fxml;
		UserRole role = user.getRole();

		switch (role) {
		case Subscriber:
			fxml = "/client/SubscriberMainLayout.fxml";
			client.subscriberDetails(user);
			break;
		case Attendant, Manager:
			fxml = "/client/StaffMainLayout.fxml";
			break;
		default:
			UiUtils.showAlert("BPARK – Error", "Unknown role: " + role, Alert.AlertType.ERROR);
			return;
		}
		

		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
			Parent root = loader.load();
			Object ctrl = loader.getController();

			if(ctrl instanceof SubscriberMainLayoutController) {
				client.setMainLayoutController((SubscriberMainLayoutController)ctrl);
				((SubscriberMainLayoutController)ctrl).setClient(client);
				((SubscriberMainLayoutController)ctrl).setSubscriberName(username.getText().trim());
				((SubscriberMainLayoutController)ctrl).loadScreen("/client/SubscriberMainScreen.fxml");
				client.setPassword(password);
			}
			// Extra data per role
			if (ctrl instanceof StaffMainLayoutController staff) {
				staff.setUser(user);
			}
		
			
			

			Stage stage = (Stage) submit.getScene().getWindow();
			stage.setScene(new Scene(root));
			stage.setTitle("BPARK – " + role);
			stage.show();

		} catch (Exception ex) {
			UiUtils.showAlert("BPARK – Error", "Failed to load " + role + " screen.", Alert.AlertType.ERROR);
			ex.printStackTrace();
		}
	}

	/**
	 * Returns to the entry screen (Guest / Login choice)
	 */
	@FXML
	private void handleBack() {
		UiUtils.loadScreen(backButton, "/client/MainScreen.fxml", "BPARK – Welcome", client);
	}
}
