package controllers;

import java.io.IOException;

import client.ClientController;
import common.Order;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import ui.UiUtils;

public class SubscriberMainLayoutController implements ClientAware{
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
	private Button btnMyReservations;
	@FXML
	private Button btnExit;

	
	
	
	@FXML
	private AnchorPane center;
	
	private ClientController client;

	
	private String subscriberName;

	public void setSubscriberName(String name) {
	    this.subscriberName = name;
	}
	
	/**
	 * load the home page in the center
	 */
	@FXML
	private void handleHomeClick() {
		loadScreen("/client/SubscriberMainScreen.fxml");
	}

	/** Logs out and returns to the entry screen. */
	@FXML
	private void handleLogoutClick() {
		UiUtils.loadScreen(btnLogout, "/client/MainScreen.fxml", "BPARK – Welcome", client);
	}

	

	@FXML
	public void handleViewPersonalInfo() {
		loadScreen("/client/ViewSubscriberDetailsScreen.fxml");
	}

	@FXML
	private void handleViewParkingHistory() {
		loadScreen("/client/ViewSubscriberHistoryScreen.fxml");
	}

	@FXML
	private void handleViewActiveParkingInfo() {
		loadScreen("/client/ViewActiveParkingInfoScreen.fxml");
	}

	@FXML
	private void handleExtendParkingTime() {
		loadScreen("/client/ExtendParkingScreen.fxml");
	}

	@FXML
	private void handleSubmitVehicle() {
		System.out.println("Submitting vehicle…");
	}
	
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
	private void handleMyReservations() {
		loadScreen("/client/WatchAndCancelOrdersScreen.fxml");
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
			if(ctrl instanceof SubscriberMainController controller) {
				controller.setSubscriberName(subscriberName);
			}
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
			if(ctrl instanceof ViewSubscriberDetailsController controller) {
				client.setViewSubscriberDetailsController(controller);
				controller.setClient(client);
				controller.setSubscriberAndPassword();
				controller.setLabels();
			}
			if(ctrl instanceof EditSubscriberDetailsController controller) {
				client.setEditSubscriberDetailsController(controller);
				controller.setClient(client);
				controller.setSubscriberAndPassword();
				controller.setTextOnField();
			}
			if (ctrl instanceof ViewParkingHistoryController controller) {
				client.setViewParkingHistoryController(controller);
				controller.setClient(client);
				controller.setTable();
				
			}
			if (ctrl instanceof ViewActiveParkingInfoController controller) {
				client.setViewActiveParkingInfoController(controller);
				controller.setClient(client);
				controller.getDetailsOfActiveInfo();
			}
			
			if (ctrl instanceof VehiclePickupController controller) {
				controller.setClient(client);
				client.setPickupController(controller);
			}
			
			if (ctrl instanceof VehicleDeliveryController controller) {
				controller.setClient(client);
				client.setDeliveryController(controller);
			}
			
			if (ctrl instanceof ExtendParkingController controller) {
			    controller.setClient(client);
			    client.setExtendParkingController(controller);
			}
			
			
			center.getChildren().clear();
			center.getChildren().add(content);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * load the summary screen with order details on the center of borderPane
	 * @param fxml (String of fxml)
	 * @param order
	 */
	public void loadScreen(String fxml, Order order) {
		try {
			FXMLLoader loader= new FXMLLoader(getClass().getResource(fxml));
			Parent content=loader.load();
			Object ctrl=loader.getController();
			
			if (ctrl instanceof ParkingReservationSummaryController controller) {
				client.setSummaryController(controller);
				controller.setClient(client);
				controller.setLabels(order);
			}
			
			center.getChildren().clear();
			center.getChildren().add(content);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}

	public void handleGoToCreateOrder() {
		try {
			loadScreen("/client/PlacingAnOrderView.fxml"); // load the Placing_an_order_view.fxml after search on resources
			
		} catch (Exception e) {
			System.out.println("Error:" + e.getMessage());
			  e.printStackTrace();
		}
	}

	
	public void setClient(ClientController client) {
		this.client=client;
	}
	
	@FXML
	private void handleExitClick() {
		try {
			if (client != null && client.isConnected()) {
				client.sendToServer(new Object[] { "disconnect" });
				client.closeConnection();
			}
		} catch (Exception ignored) {
			// Not critical – we're quitting anyway
		}
		javafx.application.Platform.exit();
		System.exit(0);
	}

}
