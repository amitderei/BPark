package controllers;

import client.ClientController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import ui.UiUtils;

/**
 * Controller for the subscriber's main screen in the BPARK system. Provides
 * navigation to all subscriber-level actions.
 */
public class SubscriberMainController implements ClientAware {

	
	
	@FXML
	private Label welcomeLabel;

	
	
	private ClientController client;

		

	/** Injects the active client once the screen is loaded. */
	@Override
	public void setClient(ClientController client) {
		this.client = client;
	}

	

	/**
	 * Displays a personalized welcome message.
	 *
	 * @param name subscriber's first name
	 */
	public void setSubscriberName(String name) {
		if (welcomeLabel != null) {
			welcomeLabel.setText("Welcome, " + name + "!");
		}
	}

	

	
}