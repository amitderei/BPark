package controllers;

import java.io.IOException;

import client.ClientController;
import common.Operation;
import common.Order;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import ui.UiUtils;

/**
 * Main container for every subscriber screen. Keeps the same top bar and
 * side-menu while swapping the centre pane.
 */
public class SubscriberMainLayoutController implements ClientAware {
	/** Button to return to subscriber home screen */
	@FXML
	private Button btnHome;

	/** Button to log out and return to login screen */
	@FXML
	private Button btnLogout;

	/** Button to exit the application */
	@FXML
	private Button btnExit;

	/** Button to view subscriber's personal information */
	@FXML
	private Button btnViewPersonalInfo;

	/** Button to view full parking history */
	@FXML
	private Button btnViewParkingHistory;

	/** Button to view the current active parking session */
	@FXML
	private Button btnViewActiveParkingInfo;

	/** Button to request parking extension */
	@FXML
	private Button btnExtendParkingTime;

	/** Button to place a new parking reservation */
	@FXML
	private Button btnParkingReservation;

	/** Button to view and cancel existing reservations */
	@FXML
	private Button btnMyReservations;

	/** Center pane where child screens are dynamically loaded */
	@FXML
	private AnchorPane center;

	/** Shared client controller for server communication */
	private ClientController client;

	/** Subscriber's first name, used in greeting text */
	private String subscriberName;

	/**
	 * Injects the shared ClientController instance.
	 * 
	 * @param client active client instance
	 */
	@Override
	public void setClient(ClientController client) {
		this.client = client;
	}

	/**
	 * Stores the subscriber’s first name to be used for greetings.
	 * 
	 * @param name subscriber's first name
	 */
	public void setSubscriberName(String name) {
		this.subscriberName = name;
	}

	/** Navigates to the subscriber's home screen. */
	@FXML
	private void handleHomeClick() {
		loadScreen("/client/SubscriberMainScreen.fxml");
	}

	/** Logs the user out and returns to the main screen. */
	@FXML
	private void handleLogoutClick() {
		UiUtils.loadScreen(btnLogout, "/client/MainScreen.fxml", "BPARK – Welcome", client);
	}

	/** Gracefully disconnects and exits the application. */
	@FXML
	private void handleExitClick() {
		UiUtils.exitFromSystem();
	}

	/** Loads the personal details view. */
	@FXML
	private void handleViewPersonalInfo() {
		loadScreen("/client/ViewSubscriberDetailsScreen.fxml");
	}

	/** Loads the parking history view. */
	@FXML
	private void handleViewParkingHistory() {
		loadScreen("/client/ViewSubscriberHistoryScreen.fxml");
	}

	/** Loads the current active parking session view. */
	@FXML
	private void handleViewActiveParkingInfo() {
		loadScreen("/client/ViewActiveParkingInfoScreen.fxml");
	}

	/** Loads the extend parking request screen. */
	@FXML
	private void handleExtendParkingTime() {
		loadScreen("/client/ExtendParkingScreen.fxml");
	}

	/** Loads the user's current reservations screen. */
	@FXML
	private void handleMyReservations() {
		loadScreen("/client/WatchAndCancelOrdersScreen.fxml");
	}

	/**
	 * Loads the new reservation creation flow screen. Typically triggered from
	 * inside the reservation panel.
	 */
	public void handleGoToCreateOrder() {
		loadScreen("/client/PlacingAnOrderView.fxml");
	}

	/**
	 * Loads a standard screen into the center pane.
	 *
	 * @param fxml the path to the FXML file in the resource directory
	 */
	public void loadScreen(String fxml) {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxml));
			Parent content = fxmlLoader.load();
			Object ctrl = fxmlLoader.getController();

			// Inject the subscriber's name if it’s the home screen
			if (ctrl instanceof SubscriberMainController c) {
				c.setSubscriberName(subscriberName);
			}

			// Inject shared client controller
			if (ctrl instanceof ClientAware aware) {
				aware.setClient(client);
			}

			// Register callbacks and init logic for specific screens
			if (ctrl instanceof CreateNewOrderViewController c) {
				client.setNewOrderController(c);
				c.initializeCombo();
			}
			if (ctrl instanceof WatchAndCancelOrdersController c) {
				client.setWatchAndCancelOrdersController(c);
				c.defineTable();
			}
			if (ctrl instanceof ViewSubscriberDetailsController c) {
				client.setViewSubscriberDetailsController(c);
				c.setSubscriberAndPassword();
				c.setLabels();
			}
			if (ctrl instanceof EditSubscriberDetailsController c) {
				client.setEditSubscriberDetailsController(c);
				c.setSubscriberAndPassword();
				c.setTextOnField();
			}
			if (ctrl instanceof ViewParkingHistoryController c) {
				client.setViewParkingHistoryController(c);
				c.setTable();
			}
			if (ctrl instanceof ViewActiveParkingInfoController c) {
				client.setViewActiveParkingInfoController(c);
				c.getDetailsOfActiveInfo();
			}
			if (ctrl instanceof ExtendParkingController c) {
				client.setExtendParkingController(c);
			}

			center.getChildren().setAll(content);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Loads a screen that needs a specific {@link Order} passed to it, such as a
	 * reservation summary page.
	 *
	 * @param fxml  FXML path of the target screen
	 * @param order the order to show in the summary view
	 */
	public void loadScreen(String fxml, Order order) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
			Parent content = loader.load();

			if (loader.getController() instanceof ParkingReservationSummaryController c) {
				client.setSummaryController(c);
				c.setLabels(order);
			}

			center.getChildren().setAll(content);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
