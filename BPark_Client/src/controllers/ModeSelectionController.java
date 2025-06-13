package controllers;


import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javafx.fxml.FXMLLoader;

import java.net.InetAddress;
import java.net.UnknownHostException;

import client.ClientApp;
import client.ClientController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import ui.UiUtils;

/**
 * Controller for BPARK's entry screen.
 * Lets the user choose between Application and Termial.
 */
public class ModeSelectionController implements ClientAware {

	@FXML 
	private Button btnApp;   // will open app fxml
	@FXML 
	private Button btnTerminal;   // will open terminal fxml
	@FXML
	private Button btnExit;

	private ClientController client;

	/** Injects the active client and registers callback for login result. */
	@Override
	public void setClient(ClientController client) {
		this.client = client;
	}


	/** Opens the Application screen. */
	@FXML
	public void handleApp() {
		UiUtils.loadScreen(btnApp,
				"/client/MainScreen.fxml",
				"BPARK – Guest",
				client);          
	}

	/** Opens the Terminal screen. */
	@FXML
	public void handleTerminal() {
		UiUtils.loadScreen(btnTerminal,
				"/client/TerminalMainLayout.fxml",
				"BPARK – Terminal",
				client);  // No client needed for terminal mode
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

}
