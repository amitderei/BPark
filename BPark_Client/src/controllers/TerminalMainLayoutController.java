package controllers;

import java.io.IOException;

import client.ClientController;
import common.Order;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import ui.UiUtils;

public class TerminalMainLayoutController implements ClientAware {

	@FXML private Button btnExit;
	@FXML private Button btnBack;
	@FXML private Button btnHome;
	@FXML private Button btnSubmitVehicle;
	@FXML private Button btnRetrieveVehicle;
	@FXML private Button btnCheckAvailability;
	@FXML private AnchorPane center;

	private ClientController client;



	
	/**
	 * Sets the client instance used for communication with the server.
	 */
	public void setClient(ClientController client) {
		this.client=client;
	}
	
	@FXML
	public void initialize() {
		handleHomeClick(); // Automatically load the terminal home screen
	}


	/**
	 * Loads the main terminal screen into the center pane.
	 */
	@FXML
	private void handleHomeClick() {
		loadScreen("/client/TerminalMainScreen.fxml");
	}
	
	/**
	 * Loads the user type selection screen (back button).
	 */
	@FXML
	private void handleBackClick() {
		loadScreen("/client/SelectionScreen.fxml");
	}
	
	/**
	 * Disconnects from the server and exits the application.
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
	 * Navigates to the delivery screen and sets up the controller.
	 */
	@FXML
	public void handleGoToDelivery() {
		loadScreen("/client//VehicleDeliveryScreen.fxml");
	}

	/**
	 * Opens the vehicle-pickup screen when the user clicks “Retrieve Vehicle”.
	 */
	@FXML
	private void handleRetrieveVehicle() {
		loadScreen("/client/VehiclePickupScreen.fxml");
	}
	
	@FXML
	private void handleCheckAvailability() {
		//צריך לממש
		//לדעתי יש ליצור מסך עבור סטטוס חניות זמינות
	}


	/**
	 * Loads a screen into the center of the terminal layout.
	 * Skips client injection for simple terminal screens like TerminalMainScreen.
	 *
	 * @param fxml the path to the FXML file to load
	 */
	public void loadScreen(String fxml) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
			Parent content = loader.load();

			Object ctrl = loader.getController();

			if (ctrl instanceof CreateNewOrderViewController controller) {
				client.setNewOrderController(controller);
				controller.setClient(client);
				controller.initializeCombo();
			}
			else if (ctrl instanceof WatchAndCancelOrdersController controller) {
				client.setWatchAndCancelOrdersController(controller);
				controller.setClient(client);
				controller.defineTable();
			}
			else if (ctrl instanceof VehiclePickupController controller) {
				controller.setClient(client);
				client.setPickupController(controller);
			}
			else if (ctrl instanceof VehicleDeliveryController controller) {
				controller.setClient(client);
				client.setDeliveryController(controller);
			}
		


			center.getChildren().clear();
			center.getChildren().add(content);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}
