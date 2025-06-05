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

public class TerminalController implements ClientAware {

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
	
	/**
	 * Sets the client instance used for communication with the server.
	 */
	public void setClient(ClientController client) {
		this.client=client;
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
		UiUtils.loadScreen(btnBack,"/client/SelectionScreen.fxml", "Select User Type",client); 
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
	
	    try {
	    	FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/Vehicle_delivery_screen.fxml"));
			Parent root = loader.load();
			VehicleDeliveryController controller = loader.getController();
			controller.setClient(client); 
			client.setDeliveryController(controller);

	        Stage stage = (Stage) btnSubmitVehicle.getScene().getWindow();
	        Scene scene = new Scene(root);
	        
	        System.out.println("✔ Vehicle_delivery_screen.fxml loaded successfully");

	        stage.setScene(scene);
	        stage.show();
	    } catch (Exception e) {
	        System.out.println("Error: " + e.getMessage());
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
