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

	/*
	 * ------------------------------------------------------------- FXML-injected
	 * controls -------------------------------------------------------------
	 */
	@FXML
	private Label welcomeLabel;

	@FXML
	private Button btnHome;
	@FXML
	private Button btnLogout;

	@FXML
	private Button btnViewPersonalInfo;
	@FXML
	private Button btnViewParkingHistory;
	@FXML
	private Button btnViewActiveParkingInfo;
	@FXML
	private Button btnExtendParkingTime;
	@FXML
	private Button btnSubmitVehicle;
	@FXML
	private Button btnRetrieveVehicle;
	@FXML
	private Button btnParkingReservation;
	@FXML
	private Button btnParkingCodeConfirmation;

	/*
	 * ------------------------------------------------------------- Runtime fields
	 * -------------------------------------------------------------
	 */
	private ClientController client;

	/*
	 * ------------------------------------------------------------- ClientAware
	 * implementation -------------------------------------------------------------
	 */

	/** Injects the active client once the screen is loaded. */
	@Override
	public void setClient(ClientController client) {
		this.client = client;
	}

	/*
	 * ------------------------------------------------------------- Public API
	 * -------------------------------------------------------------
	 */

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

	/*
	 * ------------------------------------------------------------- Top-toolbar
	 * handlers -------------------------------------------------------------
	 */

	/** “Home” button – already on home, so nothing to do. */
	@FXML
	private void handleHomeClick() {
		System.out.println("Home button clicked (already on home screen).");
	}

	/** Logs out and returns to the entry screen. */
	@FXML
	private void handleLogoutClick() {
		UiUtils.loadScreen(btnLogout, "/client/MainScreen.fxml", "BPARK – Welcome", client);
	}

	/*
	 * ------------------------------------------------------------- Navigation
	 * button handlers (placeholders for now)
	 * -------------------------------------------------------------
	 */

	@FXML
	private void handleViewPersonalInfo() {
		System.out.println("Viewing personal info…");
	}

	@FXML
	private void handleViewParkingHistory() {
		System.out.println("Viewing parking history…");
	}

	@FXML
	private void handleViewActiveParkingInfo() {
		System.out.println("Viewing active parking info…");
	}

	@FXML
	private void handleExtendParkingTime() {
		System.out.println("Extending parking time…");
	}

	@FXML
	private void handleSubmitVehicle() {
		System.out.println("Submitting vehicle…");
	}

	/**
	 * Opens the vehicle-pickup screen when the user clicks “Retrieve Vehicle”.
	 */
	@FXML
	private void handleRetrieveVehicle() {
		UiUtils.loadScreen(btnRetrieveVehicle, "/client/VehiclePickupScreen.fxml", "BPARK – Vehicle Pickup", client);
	}

	@FXML
	private void handleParkingReservation() {
		System.out.println("Reserving parking…");
	}

	@FXML
	private void handleParkingCodeConfirmation() {
		System.out.println("Confirming parking code…");
	}

	public void handleGoToCreateOrder() {
	    try {
	    	FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/Placing_an_order_view.fxml")); //load the Placing_an_order_view.fxml after search on resources
	        Parent root = loader.load();

	        
	        CreateNewOrderViewController controller = loader.getController(); //after loading the fxml- get the controller
	        controller.setClient(client);// move the client to the new controller
	        client.setNewOrderController(controller); //for act functions
	        
	        controller.initializeComboBoxesAndDatePickers();
	        
	        
	        Stage stage = (Stage) btnParkingReservation.getScene().getWindow(); //get the stage
	        Scene scene = new Scene(root); //create new scene
	        stage.setScene(scene);
	        stage.show();
	    } catch (Exception e) {
	        System.out.println("Error:"+ e.getMessage());
	    }
	}
	
	
	public void handleGoToDelivery() {
	    try {
	    	FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/Vehicle_delivery_screen.fxml")); //load the Placing_an_order_view.fxml after search on resources
	        Parent root = loader.load();

	        
	        VehicleDeliveryController controller = loader.getController(); //after loading the fxml- get the controller
	        controller.setClient(client);// move the client to the new controller
	        client.setDeliveryController(controller); //for act functions
	        
	        
	        
	        
	        Stage stage = (Stage) btnParkingReservation.getScene().getWindow(); //get the stage
	        Scene scene = new Scene(root); //create new scene
	        stage.setScene(scene);
	        stage.show();
	    } catch (Exception e) {
	        System.out.println("Error:"+ e.getMessage());
	    }
	}
}