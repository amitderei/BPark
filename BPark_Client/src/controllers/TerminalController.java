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

public class TerminalController {

	@FXML 
	private Button btnExit;
	@FXML 
	private Button btnBack;
	@FXML 
	private Button btnHome;
	@FXML
	private Button btnSubmitVehicle;
	@FXML
	private Button btnRetrieveVehicle;
	@FXML
	private AnchorPane center;

	private ClientController client;

	public void setClient(ClientController client) {
		this.client=client;
	}

	/**
	 * load the home page in the center
	 */
	@FXML
	private void handleHomeClick() {
		loadScreen("/client/TerminalMainScreen.fxml");
	}
	@FXML
	private void handleBackClick() {
		UiUtils.loadScreen(btnBack,
				"/client/SelectionScreen.fxml",
				"Select User Type",
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

	public void handleGoToDelivery() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/Vehicle_delivery_screen.fxml")); // load
			// the
			// Placing_an_order_view.fxml
			// after
			// search
			// on
			// resources
			Parent root = loader.load();

			VehicleDeliveryController controller = loader.getController(); // after loading the fxml- get the controller
			controller.setClient(client);// move the client to the new controller
			client.setDeliveryController(controller); // for act functions

			Stage stage = (Stage).getScene().getWindow(); // get the stage
			Scene scene = new Scene(root); // create new scene
			stage.setScene(scene);
			stage.show();
		} catch (Exception e) {
			System.out.println("Error:" + e.getMessage());
		}
	}
	
	/**
	 * Opens the vehicle-pickup screen when the user clicks “Retrieve Vehicle”.
	 */
	@FXML
	private void handleRetrieveVehicle() {
		UiUtils.loadScreen(btnRetrieveVehicle, "/client/VehiclePickupScreen.fxml", "BPARK – Vehicle Pickup", client);
	}

	/**
	 * load screen in the center of borderPane
	 * @param fxml
	 */
	public void loadScreen(String fxml) {
		try {
			FXMLLoader loader= new FXMLLoader(getClass().getResource(fxml));
			Parent content=loader.load();

			Object ctrl=loader.getController();
			
			if(ctrl instanceof CreateNewOrderViewController controller) {
				client.setNewOrderController(controller); // for act functions
				controller.setClient(client);
				controller.initializeCombo();
			}
			if(ctrl instanceof WatchAndCancelOrdersController controller) {
				client.setWatchAndCancelOrdersController(controller);
				controller.setClient(client);
				controller.defineTable();
			}


			center.getChildren().clear();
			center.getChildren().add(content);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

}
