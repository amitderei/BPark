package controllers;

import java.io.IOException;

import client.ClientController;
import common.Operation;
import common.Order;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import ui.UiUtils;

/**
 * Main layout for the physical terminal (kiosk) used by subscribers at the parking lot.
 * The layout includes a top bar and a side menu with quick actions:
 * - Submit vehicle (enter the parking lot)
 * - Retrieve vehicle (collect the car)
 * - Extend parking session
 * - Check availability
 *
 * All other screens are loaded into the center panel.
 */
public class TerminalMainLayoutController implements ClientAware {

	@FXML
	private Button btnExit;

	@FXML
	private Button btnBack;

	/** Button to return to the welcome panel of the terminal */
	@FXML
	private Button btnHome;

	/** Button to begin the car delivery flow */
	@FXML
	private Button btnSubmitVehicle;

	/** Button to begin the car retrieval flow */
	@FXML
	private Button btnRetrieveVehicle;
	
	@FXML
	private Button btnExtendParking;

	/** Button to check current parking availability */
	@FXML
	private Button btnCheckAvailability;

	/** The dynamic container into which screens are loaded */
	@FXML
	private AnchorPane center;

	/** Shared controller for server communication */
	private ClientController client;

	/**
	 * Injects the ClientController reference for use in this layout and
	 * sub-controllers.
	 *
	 * @param client active client instance
	 */
	@Override
	public void setClient(ClientController client) {
		this.client = client;
	}

	/**
	 * Initializes the layout after FXML is loaded by displaying the home panel.
	 */
	@FXML
	public void initialize() {
		handleHomeClick();
	}

	/**
	 * Loads the terminal’s default home panel. Triggered when the user clicks the
	 * "Home" button.
	 */
	@FXML
	private void handleHomeClick() {
		loadScreen("/client/TerminalMainScreen.fxml");
	}

	/**
	 * Returns to the initial user-selection screen. Triggered when the user clicks
	 * the "Back" button.
	 */
	@FXML
	private void handleBackClick() {
		UiUtils.loadScreen(btnBack, "/client/SelectionScreen.fxml", "BPARK – Welcome", client);
	}

	/**
	 * Disconnects from the server (if needed) and shuts down the application.
	 * Triggered when the user clicks the "Exit" button.
	 */
	@FXML
	private void handleExitClick() {
		UiUtils.exitFromSystem();
	}

	/**
	 * Loads the screen that begins the car delivery (submission) process. Triggered
	 * when the user clicks "Submit Vehicle".
	 */
	@FXML
	public void handleGoToDelivery() {
		loadScreen("/client/VehicleDeliveryScreen.fxml");
	}

	/**
	 * Loads the screen for retrieving a vehicle by parking code. Triggered when the
	 * user clicks "Retrieve Vehicle".
	 */
	@FXML
	private void handleRetrieveVehicle() {
		loadScreen("/client/VehiclePickupScreen.fxml");
	}
	
    /**
     * Opens the screen to extend an active parking session.
     */
	@FXML
	private void handleExtendParking() {
		loadScreen("/client/ExtendParkingScreen.fxml");
	}


	/**
	 * Loads the screen that shows current spot availability. Triggered when the
	 * user clicks "Check Availability".
	 */
	@FXML
	private void handleCheckAvailability() {
		loadScreen("/client/AvailabilityScreen.fxml");
	}

	/**
	 * Replaces the content in the center pane with the given screen. Also wires
	 * controllers that implement ClientAware with the shared client.
	 *
	 * @param fxml path to the child screen’s FXML file
	 */
	public void loadScreen(String fxml) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
			Parent content = loader.load();
			Object ctrl = loader.getController();

			// Inject the client to any ClientAware controller
			if (ctrl instanceof ClientAware aware)
				aware.setClient(client);

			// Perform additional setup for specific controllers
			if (ctrl instanceof CreateNewOrderViewController c) {
				client.setNewOrderController(c);
				c.initializeCombo();
			} 
			else if (ctrl instanceof WatchAndCancelOrdersController c) {
				client.setWatchAndCancelOrdersController(c);
				c.defineTable();
			} 
			else if (ctrl instanceof VehiclePickupController c) {
				client.setPickupController(c);
			} 
			else if (ctrl instanceof VehicleDeliveryController c) {
				client.setDeliveryController(c);
			} 
			else if (ctrl instanceof AvailabilityController c) {
				client.setAvailabilityController(c);
				client.getRequestSender().requestParkingAvailability();
			}
			else if (ctrl instanceof ExtendParkingController c) {
				client.setExtendParkingController(c);
			}


			// Display the loaded screen
			center.getChildren().setAll(content);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
