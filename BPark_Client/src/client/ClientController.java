package client;

import common.Order;
import common.ParkingEvent;
import common.ServerResponse;
import common.Subscriber;
import common.User;
import controllers.CreateNewOrderViewController;
import controllers.EditSubscriberDetailsController;
import controllers.GuestMainController;
import controllers.LoginController;
import controllers.MainController;
import controllers.SubscriberMainLayoutController;
import controllers.ParkingReservationSummaryController;
import controllers.SubscriberMainController;
import controllers.TerminalMainLayoutController;
import controllers.VehicleDeliveryController;
import controllers.VehiclePickupController;
import controllers.ViewActiveParkingInfoController;
import controllers.ViewParkingHistoryController;
import controllers.ViewSubscriberDetailsController;
import controllers.WatchAndCancelOrdersController;
import controllers.ViewSubscribersInfoController;
import controllers.ViewActiveParkingsController;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import ocsf.client.AbstractClient;
import ui.UiUtils;

import java.io.IOException;
import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles network communication between the BPARK client and server. Wraps
 * client requests and processes server responses.
 */
public class ClientController extends AbstractClient {

	private LoginController loginController;
	private VehiclePickupController pickupController;
	private GuestMainController guestMainController;
	private CreateNewOrderViewController newOrderController;
	private ParkingReservationSummaryController summaryController;
	private SubscriberMainController subscriberMainController;
	private VehicleDeliveryController newDeliveryController;
	private SubscriberMainLayoutController mainLayoutController;
	private WatchAndCancelOrdersController watchAndCancelOrdersController;
	private ViewSubscriberDetailsController viewSubscriberDetailsController;
	private EditSubscriberDetailsController editSubscriberDetailsController;
	private ViewParkingHistoryController viewParkingHistoryController;
	private ViewSubscribersInfoController viewSubscribersInfoController;
	private ViewActiveParkingsController viewActiveParkingsController;
	private ViewActiveParkingInfoController viewActiveParkingInfoController;
	private TerminalMainLayoutController terminalController;
	private MainController mainController;

	private Subscriber subscriber;

	private String password;
	

	/**
	 * Constructs a new ClientController instance.
	 *
	 * @param host the server's IP or hostname
	 * @param port the server's listening port
	 */
	public ClientController(String host, int port) {
		super(host, port);
	}

	/**
	 * set the password in order to reduce I/O
	 * 
	 * @param password
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	public WatchAndCancelOrdersController getWatchAndCancelOrdersController() {
		return watchAndCancelOrdersController;
	}

	public void setWatchAndCancelOrdersController(WatchAndCancelOrdersController watchAndCancelOrdersController) {
		this.watchAndCancelOrdersController = watchAndCancelOrdersController;
	}

	public void setViewParkingHistoryController(ViewParkingHistoryController viewParkingHistoryController) {
		this.viewParkingHistoryController = viewParkingHistoryController;
	}
	
	public void setMainController(MainController mainController) {
		this.mainController=mainController;
	}
	
	public void setTerminalController(TerminalMainLayoutController terminalController) {
		this.terminalController=terminalController;
	}

	/**
	 * Sets the login screen controller.
	 *
	 * @param loginController the login screen controller
	 */
	public void setLoginController(LoginController loginController) {
		this.loginController = loginController;
	}

	public void setGuestController(GuestMainController controller) {
		this.guestMainController = controller;
	}

	public void setSubscriberMainController(SubscriberMainController controller) {
		this.subscriberMainController = controller;
	}
	
	public void setViewActiveParkingsController(ViewActiveParkingsController controller) {
	    this.viewActiveParkingsController = controller;
	}
	
	public void setViewSubscribersInfoController(ViewSubscribersInfoController c) {
	    this.viewSubscribersInfoController = c;
	}

	/**
	 * Sets the ViewSubscriberDetails screen controller.
	 *
	 * @param loginController the login screen controller
	 */
	public void setViewSubscriberDetailsController(ViewSubscriberDetailsController viewSubscriberDetailsController) {
		this.viewSubscriberDetailsController = viewSubscriberDetailsController;
	}

	/**
	 * Sets the vehicle pickup screen controller.
	 *
	 * @param pickupController the vehicle pickup controller
	 */
	public void setPickupController(VehiclePickupController pickupController) {
		this.pickupController = pickupController;
	}

	public void setGuestMainController(GuestMainController guestMainController) {
		this.guestMainController = guestMainController;
	}

	public void setEditSubscriberDetailsController(EditSubscriberDetailsController editSubscriberDetailsController) {
		this.editSubscriberDetailsController = editSubscriberDetailsController;
	}

	/**
	 * set new order controller
	 * 
	 * @param controller
	 */
	public void setNewOrderController(CreateNewOrderViewController controller) {
		this.newOrderController = controller;
	}

	public void setMainLayoutController(SubscriberMainLayoutController controller) {
		this.mainLayoutController = controller;

	}

	public SubscriberMainLayoutController getMainLayoutController() {
		return mainLayoutController;
	}

	public void setSummaryController(ParkingReservationSummaryController controller) {
		this.summaryController = controller;

	}

	public void setDeliveryController(VehicleDeliveryController controller) {
		this.newDeliveryController = controller;

	}
	
	public void setViewActiveParkingInfoController(ViewActiveParkingInfoController viewActiveParkingInfoController) {
		this.viewActiveParkingInfoController = viewActiveParkingInfoController;
	}

	public void setSubscriber(Subscriber subscriber) {
		this.subscriber = subscriber;
	}

	public Subscriber getSubscriber() {
		return subscriber;
	}

	/**
	 * Processes messages received from the server. Handles login results, order
	 * data, error messages, vehicle pickup responses, and other system messages.
	 *
	 * @param msg the message sent from the server (expected to be ServerResponse or
	 *            String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void handleMessageFromServer(Object msg) {
		// Handle server shutdown as plain String
		if (msg instanceof String str && str.equals("server_shutdown")) {
			Platform.runLater(() -> {
				UiUtils.showAlert("Server Shutdown", "The server is shutting down. The application will now close.",
						Alert.AlertType.INFORMATION);
				System.exit(0);
			});
			return;
		}

		// Validate expected type (ServerResponse)
		if (!(msg instanceof ServerResponse response)) {
			System.err.println("Unexpected message type: " + msg.getClass());
			return;
		}

		Platform.runLater(() -> {

			if (!response.isSucceed() && loginController != null
					&& response.getMsg().toLowerCase().contains("invalid")) {
				loginController.handleLoginFailure(response.getMsg());
				return;
			}
			
			
			
			// General error message popup (only if not handled before)
			if (!response.isSucceed()) {
				UiUtils.showAlert("System Message", response.getMsg(), Alert.AlertType.ERROR);
			}
			
			else if (response.isSucceed() && response.getData() instanceof User user) {
				if (loginController != null) {
					loginController.handleLoginSuccess(user);
				}
				return;
			} 

			// update the table after deleting order
			else if (response.isSucceed() && response.getMsg().equals("order deleted successfully.")) {
				if (watchAndCancelOrdersController != null) {
					askForReservations();
				}
			}

			else if (response.isSucceed() && response.getMsg().equals("Parking history data loaded successfully.")) {
				if (viewParkingHistoryController != null) {
					viewParkingHistoryController.displayHistory((ArrayList<ParkingEvent>) response.getData());
				}
			}
			// display orders of subscriber in table
			else if (response.isSucceed() && response.getData() instanceof ArrayList<?> dataList
					&& response.getMsg().equals("Orders of subscriber displayed successfully.")) {
				ArrayList<Order> orders = (ArrayList<Order>) dataList;
				if (watchAndCancelOrdersController != null) {
					watchAndCancelOrdersController.displayOrders(orders);
				}
				return;
			}
			
			else if(response.isSucceed()&&response.getMsg().equals("No orders.")) {
				if (watchAndCancelOrdersController != null) {
					watchAndCancelOrdersController.displayOrders(new ArrayList<Order>());
					UiUtils.showAlert(response.getMsg(), response.getMsg(), Alert.AlertType.INFORMATION);
				}
				return;
			}

			else if (response.isSucceed() && response.getMsg().equals("Details updated successfully.")) {
				ArrayList<Object> newDetails = (ArrayList<Object>) response.getData();
				if (newDetails.get(0) instanceof Subscriber) {
					setSubscriber((Subscriber) newDetails.get(0));
					newDetails.remove(0);
				}
				if (!newDetails.isEmpty()) {
					setPassword(((User) newDetails.get(0)).getPassword());
				}
				editSubscriberDetailsController.handleGoToView();
			}

			else if (pickupController != null) {
				UiUtils.setStatus(pickupController.getStatusLabel(), response.getMsg(), response.isSucceed());

				if (response.isSucceed()) {
					String lowerMsg = response.getMsg().toLowerCase(); // changed from 'msg' to 'lowerMsg'

					// Handle subscriber validation (by code or by tag)
					if (lowerMsg.contains("subscriber verified")) {
						if (response.getData() instanceof Integer subscriberCode) {
							// Subscriber was validated using tag ID
							pickupController.onSubscriberValidated(subscriberCode);
						} else {
							// Subscriber was validated using numeric code input
							pickupController.onSubscriberValidated();
						}
					}

					// Handle successful car collection
					else if (lowerMsg.contains("pickup successful")) {
						pickupController.disableAfterPickup();
					}
				}
			}

			// get response from server and back to GuestMainController with
			// updateAvailableSpots

			// get response from server and back to CreateNeworder
			if (response.getData() instanceof Integer && guestMainController != null) {
				int count = (int) response.getData();
				System.out.println(((Integer) count).toString());
				guestMainController.updateAvailableSpots(count); // call method to update
			}

			if (response.isSucceed() && response.getData() instanceof Order) {
				if (newOrderController != null) {
					newOrderController.setOrderAndGoToNextPage((Order) response.getData());

				}
			}

			if (response.isSucceed() && response.getData() instanceof Subscriber) {
				setSubscriber((Subscriber) response.getData());
			}
			
			// "all_subscribers"  rows = List<Object[]> { [0]=Subscriber , [1]=Integer lateCount }
			if (response.isSucceed() && "all_subscribers".equals(response.getMsg())) {

			    ArrayList<Object[]> rows = (ArrayList<Object[]>) response.getData();

			    System.out.println("[DEBUG] Received all_subscribers. Total rows = " + rows.size());

			    List<Subscriber> subs = new ArrayList<>();
			    Map<Subscriber, Integer> lateMap = new HashMap<>();

			    for (Object[] r : rows) {
			        Subscriber s = (Subscriber) r[0];
			        int late = (Integer) r[1];
			        subs.add(s);
			        lateMap.put(s, late);
			        System.out.println(" -> " + s.getUsername() + ", late: " + late);
			    }

			    if (viewSubscribersInfoController != null)
			        viewSubscribersInfoController.onSubscribersReceived(subs, lateMap);

			    return; // handled
			}
			
			else if (response.isSucceed()&&response.getMsg().equals("Active parking info loaded successfully.")) {
				if(viewActiveParkingInfoController!=null) {
					viewActiveParkingInfoController.setParkingEvent((ParkingEvent) response.getData());
					viewActiveParkingInfoController.setTexts();
				}
				return;
			}
			
			// staff -view current active parking events
			if (response.isSucceed() && "active_parkings".equals(response.getMsg())) {
			    ArrayList<ParkingEvent> events = (ArrayList<ParkingEvent>) response.getData();

			    if (viewActiveParkingsController != null)
			        viewActiveParkingsController.onActiveParkingsReceived(events);

			    return; // handled
			}

			// Vehicle delivery screen updates
			if (newDeliveryController != null) {
				System.out.println(">> newDeliveryController connected!");
				// 1 – handle subscriber code not found
				if (response.getMsg().toLowerCase().contains("does not")) {
					newDeliveryController.subscriberCodeDoesntExist();
				}

				// 2 - handle subscriber validation successfully
				if (response.getMsg().toLowerCase().contains("is valid!")) {
					newDeliveryController.subscriberCodeIsValid();
				}

				// 3 - handle subscriber has a reservation
				if (response.getMsg().toLowerCase().contains("has a reservation.")) {
					newDeliveryController.hasReservation();
				}

				// 4 - handle subscriber doesn't have a reservation
				if (response.getMsg().toLowerCase().contains("doesn't have a reseravtion.")) {
					newDeliveryController.NoReservation();
				}

				// 5 - handle subscriber entered subscriber code successfully
				if (response.getMsg().toLowerCase().contains("confirmation code has entered")) {
					newDeliveryController.confirmationCodeIsValid();
				}

				// 6 - handle subscriber entered subscriber code unsuccessfully
				if (response.getMsg().toLowerCase().contains("confirmation code isn't")) {
					newDeliveryController.confirmationCodeNotValid();
				}

				// 7 - handle no free parking spaces
				if (response.getMsg().toLowerCase().contains("the Parking Lot is Full")) {
					newDeliveryController.setParkingLotStatus(false);
				}

				// 8 - handle there's free parking space
				if (response.getMsg().toLowerCase().contains("there is free parking space")) {
					newDeliveryController.setParkingLotStatus(true);
				}

				// 9 - handle a parking space that is found
				if (response.getMsg().toLowerCase().contains("found free parking space")) {
					// gathering the available parking space
					int parkingSpace = (int) response.getData();
					newDeliveryController.parkingSpaceFuture.complete(parkingSpace);
				}

				// 10 - handle a matched vehicleID to the asked subscriber
				if (response.getMsg().toLowerCase().contains("found matched vehicle")) {
					// gathering the matched vehicle
					String vehicleID = (String) response.getData();
					newDeliveryController.vehicleIdFuture.complete(vehicleID);
				}

				// 11 - handle successful addition of adding a parking event into the DB
				if (response.getMsg().toLowerCase().contains("added parking event successfully")) {
					newDeliveryController.successfulDelivery();
				}

				// 12 - handle existing tag
				if (response.getMsg().toLowerCase().contains("tag exists")) {
					newDeliveryController.tagFound();
				}

				// 13 - handle tag doesn't exists
				if (response.getMsg().toLowerCase().contains("tag does not exists")) {
					newDeliveryController.tagNotFound();
				}

				// 14 - handle subscriber found by a tag, deliver the vehicle with a parking
				// event creation
				if (response.getMsg().toLowerCase().contains("subscriber with matching tag has been found")) {
					int subCode = (int) response.getData();
					newDeliveryController.subCodeFuture.complete(subCode);
				}

				// 15 - handle subscriber isn't inside the parking lot
				if (response.getMsg().toLowerCase().contains("the subscriber didn't entered his vehicle yet")) {
					newDeliveryController.checkIfTheresReservation();
				}

				// 16 - handle vehicle with the matched tagId isn't inside the parking lot
				if (response.getMsg().toLowerCase().contains("the tag isn't inside")) {
					newDeliveryController.findMatchedSubToTheTag();
				}

				// 17 - handle subscriber is already inside the parking lot
				if (response.getMsg().toLowerCase().contains("the vehicle is already inside the parking lot")) {
					newDeliveryController.vehicleIsAlreadyInside();
				}

			}
		});
	}

	/**
	 * add new order to order table
	 * 
	 * @param newOrder
	 */
	public void addNewOrder(Order newOrder) {
		try {
			sendToServer(new Object[] { "addNewOrder", newOrder });
		} catch (IOException e) {
			System.err.println("Failed to send 'addNewOrder' request to server: " + e.getMessage());
		}
	}

	public void checkAvailability(Date date, Time time) {
		try {
			sendToServer(new Object[] { "checkAvailability", date, time });
		} catch (IOException e) {
			System.err.println("Failed to send 'checkAvailability' request to server: " + e.getMessage());
		}
	}

	/**
	 * Sends a login request to the server.
	 *
	 * @param username user's input username
	 * @param password user's input password
	 */
	public void requestLogin(String username, String password) {
		try {
			sendToServer(new Object[] { "login", username, password });
		} catch (IOException e) {
			System.err.println("[ERROR] Failed to send login request: " + e.getMessage());
		}
	}

	/**
	 * Validates whether a subscriber exists using their code.
	 *
	 * @param subscriberCode the code to validate
	 */
	public void validateSubscriber(int subscriberCode) {
		try {
			sendToServer(new Object[] { "validateSubscriber", subscriberCode });
		} catch (IOException e) {
			System.err.println("Failed to send 'validateSubscriber' request: " + e.getMessage());
		}
	}

	/**
	 * Validates a subscriber by their RFID tag ID.
	 *
	 * @param tagId the unique tag identifier (e.g., "TAG_001")
	 */
	public void validateSubscriberByTag(String tagId) {
		try {
			sendToServer(new Object[] { "validateSubscriberByTag", tagId });
		} catch (IOException e) {
			System.err.println("Failed to send 'validateSubscriberByTag' request: " + e.getMessage());
		}
	}

	/**
	 * Sends a vehicle pickup request with subscriber code and parking code.
	 *
	 * @param subscriberCode the subscriber code
	 * @param parkingCode    the assigned parking code
	 */
	public void collectCar(int subscriberCode, int parkingCode) {
		try {
			sendToServer(new Object[] { "collectCar", subscriberCode, parkingCode });
		} catch (IOException e) {
			System.err.println("Failed to send 'collectCar' request: " + e.getMessage());
		}
	}

	/**
	 * Sends a request to extend the current parking session.
	 *
	 * @param subscriberCode the subscriber code
	 */
	public void requestExtension(int subscriberCode) {
		try {
			sendToServer(new Object[] { "extendParking", subscriberCode });
		} catch (IOException e) {
			System.err.println("Failed to send 'extendParking' request: " + e.getMessage());
		}
	}


	/**
	 * Sends a request to the server to check how many parking spots are currently
	 * available. This request is used in guest mode or before placing an order.
	 */
	public void requestAvailableSpots() {
		try {
			sendToServer(new Object[] { "CheckParkingAvailability" });
		} catch (IOException e) {
			System.err.println("Failed to request available spots: " + e.getMessage());
		}
	}

	/**
	 * send to server the user to get the subscriber details.
	 * 
	 * @param user
	 */
	public void subscriberDetails(User user) {
		try {
			sendToServer(new Object[] { "subscriberDetails", user });
		} catch (IOException e) {
			System.err.println("Failed to send 'sendLostCode' request: " + e.getMessage());
		}
	}

	/**
	 * send to server the subscriber that connect to client to get all his
	 * reservations
	 */
	public void askForReservations() {
		try {
			sendToServer(new Object[] { "askForReservations", subscriber });
		} catch (IOException e) {
			System.err.println("Failed to send 'askForReservations' request: " + e.getMessage());
		}
	}

	/**
	 * the function send to server the command and the order number
	 * 
	 * @param orderNumberToDelete (this is the primary key of order)
	 */
	public void deleteOrder(int orderNumberToDelete) {
		try {
			sendToServer(new Object[] { "deleteOrder", orderNumberToDelete });
		} catch (IOException e) {
			System.err.println("Failed to send 'deleteOrder' request: " + e.getMessage());
		}
	}

	/**
	 * the function send to server the command, subscriber details and user details
	 * 
	 * @param subscriber- the new details of subscriber
	 * @param user-       the new details of user
	 */
	public void updateDetailsOfSubscriber(Subscriber subscriber, User user) {
		try {
			sendToServer(new Object[] { "updateDetailsOfSubscriber", subscriber, user });
		} catch (IOException e) {
			System.err.println("Failed to send 'updateDetailsOfSubscriber' request: " + e.getMessage());
		}
	}

	/**
	 * the function send to server the command, subscriber details
	 */
	public void updateParkingHistoryOfSubscriber() {
		try {
			sendToServer(new Object[] { "updateParkingHistoryOfSubscriber", subscriber });
		} catch (IOException e) {
			System.err.println("Failed to send 'updateParkingHistoryOfSubscriber' request: " + e.getMessage());
		}
	}
	
	public void forgotMyParkingCode(int validatedSubscriberCode) {
		try {
			sendToServer(new Object[] { "forgotMyParkingCode", validatedSubscriberCode });
		} catch (IOException e) {
			System.err.println("Failed to send 'updateParkingHistoryOfSubscriber' request: " + e.getMessage());
		}
	}
	
    /**
     * Requests a list of all subscribers and their late pickup counts.
     * Used in staff views for monitoring subscriber activity.
     */
    public void requestAllSubscribers() {

	    try {
	        System.out.println("[DEBUG] Sending 'get_all_subscribers' to server");
	        sendToServer(new Object[] { "get_all_subscribers" });
	    } catch (IOException e) {
	        System.err.println("Failed to send 'get_all_subscribers' request: " + e.getMessage());
	    }
	}
	
    /**
     * Requests a list of all active parking events (vehicles currently inside).
     * Used by staff to monitor current parking activity.
     */
    public void requestActiveParkingEvents() {
	    try {
	        sendToServer(new Object[] { "get_active_parkings" });
	    } catch (IOException e) {
	        System.err.println("Failed to send request for active parking events.");
	        e.printStackTrace();
	    }
	}
    
    /**
     * Requests all active parking events of subscriber
     * Used by subscriber
     */
    public void getDetailsOfActiveInfo() {
		try {
			sendToServer(new Object[] { "getDetailsOfActiveInfo", subscriber });
		} catch (IOException e) {
			System.err.println("Failed to send 'getDetailsOfActiveInfo' request: " + e.getMessage());
		}
    }
}
