package controllers;

import client.ClientController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import ui.UiUtils;

/**
 * Controller for BPARK's entry screen.
 * Lets the user choose between Guest mode and Login.
 */
public class MainController implements ClientAware {

	
	@FXML private Button guestBtn;   // “Enter as Guest”
	@FXML private Button loginBtn;   // “Login”
	@FXML private Button btnExit;
	@FXML private Button btnBack;

	private ClientController client;

	/** Injects the active client. */
	@Override
	public void setClient(ClientController client) {
		this.client = client;
	}



	/** Opens the guest interface. */
	@FXML
	public void handleGuest() {
		UiUtils.loadScreen(guestBtn,
				"/client/GuestMainLayout.fxml",
				"BPARK – Guest",
				client);               
	}

	/** Opens the login screen. */
	@FXML
	public void handleLogin() {
		UiUtils.loadScreen(loginBtn,
				"/client/LoginScreen.fxml",
				"BPARK – Login",
				client);
	}

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

	@FXML
	private void handleBackClick() {
		UiUtils.loadScreen(btnBack,
				"/client/SelectionScreen.fxml",
				"Select User Type",
				client); 
	}
}

