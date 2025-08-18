package client;

import common.*;
import controllers.*;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import ocsf.client.AbstractClient;
import ui.UiUtils;

import java.sql.Date;
import java.util.*;

/**
 * This is the main client-side controller responsible for managing the
 * connection to the server.
 * 
 * It listens for messages from the server and updates the UI accordingly. Any
 * outgoing request to the server is sent via the ClientRequestSender helper
 * class.
 *
 * This class also holds the current session state (e.g., subscriber and
 * password), and references to all major UI controllers used in the
 * application.
 */
public class ClientController extends AbstractClient {
	/** Helper object responsible for sending requests to the server. */
	private final ClientRequestSender requestSender;

	/**
	 * Stores the logged-in subscriber object during session (null if not logged in)
	 */
	private Subscriber subscriber;

	/** Stores the user's password during the current session */
	private String password;

	// Controllers – each one is set via a corresponding setter from the UI

	/** Controller for the login screen. */
	private LoginController loginController;

	/** Controller for the vehicle pickup screen. */
	private VehiclePickupController pickupController;

	/** Controller for the guest main screen. */
	private GuestMainController guestMainController;

	/** Controller for creating a new parking order. */
	private CreateNewOrderViewController newOrderController;

	/** Controller for displaying the parking reservation summary. */
	private ParkingReservationSummaryController summaryController;

	/** Controller for the subscriber’s main screen. */
	private SubscriberMainController subscriberMainController;

	/** Controller for vehicle delivery at the exit. */
	private VehicleDeliveryController newDeliveryController;

	/** Controller for the main layout used by subscribers. */
	private SubscriberMainLayoutController mainLayoutController;

	/** Controller for managing and cancelling reservations. */
	private WatchAndCancelOrdersController watchAndCancelOrdersController;

	/** Controller for viewing subscriber details. */
	private ViewSubscriberDetailsController viewSubscriberDetailsController;

	/** Controller for editing subscriber details. */
	private EditSubscriberDetailsController editSubscriberDetailsController;

	/** Controller for viewing the subscriber’s parking history. */
	private ViewParkingHistoryController viewParkingHistoryController;

	/** Controller for staff viewing subscriber information and statistics. */
	private ViewSubscribersInfoController viewSubscribersInfoController;

	/** Controller for viewing active parking sessions. */
	private ViewActiveParkingsController viewActiveParkingsController;

	/**
	 * Controller for viewing detailed information about a specific parking session.
	 */
	private ViewActiveParkingInfoController viewActiveParkingInfoController;

	/** Controller for the terminal (kiosk) main layout. */
	private TerminalMainLayoutController terminalController;

	/** Controller for the main entry point screen. */
	private MainController mainController;

	/** Controller for the staff’s main layout screen. */
	private StaffMainLayoutController staffMainLayoutController;

	/** Controller for extending parking sessions. */
	private ExtendParkingController extendParkingController;

	/** Controller for registering new subscribers. */
	private RegisterSubscriberController registerSubscriberController;

	/** Controller for showing current parking availability. */
	private AvailabilityController availabilityController;

	/** Controller for generating and displaying parking reports. */
	private ParkingReportController parkingReportController;

	/** Controller for viewing monthly subscriber status reports. */
	private SubscriberStatusController subscriberStatusController;

	/**
	 * Constructs a new {@code ClientController} instance with the specified host
	 * and port.
	 * 
	 * @param host the server host address
	 * @param port the server port number
	 */
	public ClientController(String host, int port) {
		super(host, port);
		this.requestSender = new ClientRequestSender(this);
	}

	/** @return a reference to the helper class that sends requests */
	public ClientRequestSender getRequestSender() {
		return requestSender;
	}

	/**
	 * Sets the password for the current client session.
	 * 
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Gets the password for the current client session.
	 * 
	 * @return the current password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Sets the login controller.
	 * 
	 * @param c the login controller to set
	 */
	public void setLoginController(LoginController c) {
		this.loginController = c;
	}

	/**
	 * Sets the vehicle pickup controller.
	 * 
	 * @param c the vehicle pickup controller to set
	 */
	public void setPickupController(VehiclePickupController c) {
		this.pickupController = c;
	}

	/**
	 * Sets the guest main controller.
	 * 
	 * @param c the guest main controller to set
	 */
	public void setGuestMainController(GuestMainController c) {
		this.guestMainController = c;
	}

	/**
	 * Sets the new order controller.
	 * 
	 * @param c the new order controller to set
	 */
	public void setNewOrderController(CreateNewOrderViewController c) {
		this.newOrderController = c;
	}

	/**
	 * Sets the reservation summary controller.
	 * 
	 * @param c the summary controller to set
	 */
	public void setSummaryController(ParkingReservationSummaryController c) {
		this.summaryController = c;
	}

	/**
	 * Sets the subscriber main controller.
	 * 
	 * @param c the subscriber main controller to set
	 */
	public void setSubscriberMainController(SubscriberMainController c) {
		this.subscriberMainController = c;
	}

	/**
	 * Sets the vehicle delivery controller.
	 * 
	 * @param c the vehicle delivery controller to set
	 */
	public void setDeliveryController(VehicleDeliveryController c) {
		this.newDeliveryController = c;
	}

	/**
	 * Sets the main layout controller for subscribers.
	 * 
	 * @param c the main layout controller to set
	 */
	public void setMainLayoutController(SubscriberMainLayoutController c) {
		this.mainLayoutController = c;
	}

	/**
	 * Returns the main layout controller for subscribers.
	 * 
	 * @return the main layout controller
	 */
	public SubscriberMainLayoutController getMainLayoutController() {
		return mainLayoutController;
	}

	/**
	 * Sets the watch and cancel orders controller.
	 * 
	 * @param c the watch and cancel orders controller to set
	 */
	public void setWatchAndCancelOrdersController(WatchAndCancelOrdersController c) {
		this.watchAndCancelOrdersController = c;
	}

	/**
	 * Returns the watch and cancel orders controller.
	 * 
	 * @return the watch and cancel orders controller
	 */
	public WatchAndCancelOrdersController getWatchAndCancelOrdersController() {
		return watchAndCancelOrdersController;
	}

	/**
	 * Sets the view subscriber details controller.
	 * 
	 * @param c the view subscriber details controller to set
	 */
	public void setViewSubscriberDetailsController(ViewSubscriberDetailsController c) {
		this.viewSubscriberDetailsController = c;
	}

	/**
	 * Sets the edit subscriber details controller.
	 * 
	 * @param c the edit subscriber details controller to set
	 */
	public void setEditSubscriberDetailsController(EditSubscriberDetailsController c) {
		this.editSubscriberDetailsController = c;
	}

	/**
	 * Returns the edit subscriber details controller.
	 * 
	 * @return the edit subscriber details controller
	 */
	public EditSubscriberDetailsController getEditSubscriberDetailsController() {
		return editSubscriberDetailsController;
	}

	/**
	 * Sets the view parking history controller.
	 * 
	 * @param c the view parking history controller to set
	 */
	public void setViewParkingHistoryController(ViewParkingHistoryController c) {
		this.viewParkingHistoryController = c;
	}

	/**
	 * Returns the view parking history controller.
	 * 
	 * @return the view parking history controller
	 */
	public ViewParkingHistoryController getViewParkingHistoryController() {
		return viewParkingHistoryController;
	}

	/**
	 * Sets the view subscribers info controller.
	 * 
	 * @param c the view subscribers info controller to set
	 */
	public void setViewSubscribersInfoController(ViewSubscribersInfoController c) {
		this.viewSubscribersInfoController = c;
	}

	/**
	 * Returns the view subscribers info controller.
	 * 
	 * @return the view subscribers info controller
	 */
	public ViewSubscribersInfoController getViewSubscribersInfoController() {
		return viewSubscribersInfoController;
	}

	/**
	 * Sets the view active parkings controller.
	 * 
	 * @param c the view active parkings controller to set
	 */
	public void setViewActiveParkingsController(ViewActiveParkingsController c) {
		this.viewActiveParkingsController = c;
	}

	/**
	 * Returns the view active parkings controller.
	 * 
	 * @return the view active parkings controller
	 */
	public ViewActiveParkingsController getViewActiveParkingsController() {
		return viewActiveParkingsController;
	}

	/**
	 * Sets the view active parking info controller.
	 * 
	 * @param c the view active parking info controller to set
	 */
	public void setViewActiveParkingInfoController(ViewActiveParkingInfoController c) {
		this.viewActiveParkingInfoController = c;
	}

	/**
	 * Returns the view active parking info controller.
	 * 
	 * @return the view active parking info controller
	 */
	public ViewActiveParkingInfoController getViewActiveParkingInfoController() {
		return viewActiveParkingInfoController;
	}

	/**
	 * Sets the main controller.
	 * 
	 * @param c the main controller to set
	 */
	public void setMainController(MainController c) {
		this.mainController = c;
	}

	/**
	 * Sets the terminal controller.
	 * 
	 * @param c the terminal controller to set
	 */
	public void setTerminalController(TerminalMainLayoutController c) {
		this.terminalController = c;
	}

	/**
	 * Sets the staff main layout controller.
	 * 
	 * @param c the staff main layout controller to set
	 */
	public void setStaffMainLayoutController(StaffMainLayoutController c) {
		this.staffMainLayoutController = c;
	}

	/**
	 * Sets the extend parking controller.
	 * 
	 * @param c the extend parking controller to set
	 */
	public void setExtendParkingController(ExtendParkingController c) {
		this.extendParkingController = c;
	}

	/**
	 * Returns the extend parking controller.
	 * 
	 * @return the extend parking controller
	 */
	public ExtendParkingController getExtendParkingController() {
		return extendParkingController;
	}

	/**
	 * Sets the register subscriber controller.
	 * 
	 * @param c the register subscriber controller to set
	 */
	public void setRegisterSubscriberController(RegisterSubscriberController c) {
		this.registerSubscriberController = c;
	}

	/**
	 * Returns the register subscriber controller.
	 * 
	 * @return the register subscriber controller
	 */
	public RegisterSubscriberController getRegisterSubscriberController() {
		return registerSubscriberController;
	}

	/**
	 * Sets the availability controller.
	 * 
	 * @param c the availability controller to set
	 */
	public void setAvailabilityController(AvailabilityController c) {
		this.availabilityController = c;
	}

	/**
	 * Returns the availability controller.
	 * 
	 * @return the availability controller
	 */
	public AvailabilityController getAvailabilityController() {
		return availabilityController;
	}

	/**
	 * Sets the parking report controller.
	 * 
	 * @param c the parking report controller to set
	 */
	public void setParkingReportController(ParkingReportController c) {
		this.parkingReportController = c;
	}

	/**
	 * Returns the parking report controller.
	 * 
	 * @return the parking report controller
	 */
	public ParkingReportController getParkingReportController() {
		return parkingReportController;
	}

	/**
	 * Sets the subscriber status controller.
	 * 
	 * @param c the subscriber status controller to set
	 */
	public void setSubscriberStatusController(SubscriberStatusController c) {
		this.subscriberStatusController = c;
	}

	/**
	 * Returns the subscriber status controller.
	 * 
	 * @return the subscriber status controller
	 */
	public SubscriberStatusController getSubscriberStatusController() {
		return subscriberStatusController;
	}

	/**
	 * Sets the subscriber associated with the current session.
	 * 
	 * @param subscriber the subscriber to set
	 */
	public void setSubscriber(Subscriber subscriber) {
		this.subscriber = subscriber;
	}

	/**
	 * Returns the subscriber associated with the current session.
	 * 
	 * @return the current subscriber
	 */
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

			// If the server response has no type, treat it as a general-purpose message
			if (response.getType() == null) {
				System.out.println("Server response msg: " + response.getMsg());
				System.out.println("Success? " + response.isSucceed());

				boolean handled = false;

				// Show message in pickup screen label if available
				if (pickupController != null) {
					UiUtils.setStatus(pickupController.getStatusLabel(), response.getMsg(), response.isSucceed());
					handled = true;
				}

				// If not handled and it's an error, show fallback alert
				if (!handled && !response.isSucceed()) {
					UiUtils.showAlert("System Message", response.getMsg(), Alert.AlertType.ERROR);
				}
				return;
			}

			// Handle based on the type of response - case for any response (from Enam class
			// ResponseType in common)
			switch ((ResponseType) response.getType()) {

			// Failed login attempt. expected type: {LOGIN_FAILED}
			case LOGIN_FAILED:
				if (!response.isSucceed() && loginController != null) {
					loginController.handleLoginFailure(response.getMsg());
				}
				break;

			// Parking report data received.
			case PARKING_REPORT_LOADED:
				if (response.isSucceed() && parkingReportController != null) {
					parkingReportController.setParkingReport((ParkingReport) response.getData());
					parkingReportController.setChart();
				}
				break;

			// Successful login.
			case LOGIN_SUCCESSFULL:
				if (response.isSucceed() && loginController != null) {
					loginController.handleLoginSuccess((User) response.getData());
				}
				break;

			// Order deleted – refresh the reservations list.
			case ORDER_DELETED:
				if (response.isSucceed() && watchAndCancelOrdersController != null) {
					getRequestSender().askForReservations();
				}
				break;

			// Check if reservation conflicts exist.
			case CONFLICT_CHECKED:
				if (newOrderController != null) {
					boolean hasConflict = (boolean) response.getData();
					newOrderController.onReservationConflictCheck(hasConflict);
					break;
				}

				// Load parking history for subscriber.
			case PARKING_HISTORY_LOADED:
				if (response.isSucceed() && viewParkingHistoryController != null) {
					viewParkingHistoryController.displayHistory((ArrayList<ParkingEvent>) response.getData());
				}
				break;

			// Load and display list of existing orders.
			case ORDERS_DISPLAY:
				if (response.isSucceed() && response.getData() instanceof ArrayList<?> dataList
						&& watchAndCancelOrdersController != null) {
					ArrayList<Order> orders = (ArrayList<Order>) dataList;
					watchAndCancelOrdersController.displayOrders(orders);
				}
				break;

			// No existing orders found.
			case NO_ORDERS:
				if (response.isSucceed() && watchAndCancelOrdersController != null) {
					watchAndCancelOrdersController.displayOrders(new ArrayList<Order>());
				}
				break;

			// Subscriber details updated.
			case DETAILS_UPDATED:
				if (response.isSucceed()) {
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
				break;

			// Verify subscriber during pickup.
			case SUBSCRIBER_VERIFIED:
				if (pickupController != null) {
					// Always show status in the label - success or failure
					UiUtils.setStatus(pickupController.getStatusLabel(), response.getMsg(), response.isSucceed());

					if (response.isSucceed()) {
						if (response.getData() instanceof Integer subscriberCode) {
							pickupController.onSubscriberValidated(subscriberCode);
						} else {
							pickupController.onSubscriberValidated();
						}
					}
				}
				break;

			// Complete vehicle pickup.
			case PICKUP_VEHICLE:
				if (pickupController != null) {
					// Always show the server's message (success or error) in the status label
					UiUtils.setStatus(pickupController.getStatusLabel(), response.getMsg(), response.isSucceed());

					if (response.isSucceed()) {
						// If the pickup was successful - lock everything so user can't do it again
						pickupController.disableAfterPickup();
					} else {
						// If pickup failed (probably wrong parking code) - clear the field so they can
						// try again
						pickupController.resetParkingCodeField();
					}
				}
				break;

			// Load available report dates.
			case REPOSTS_DATE_LOADED:
				if (response.isSucceed() && parkingReportController != null) {
					parkingReportController.setDates((ArrayList<Date>) (response.getData()));
				}
				break;

			// Order was successfully added.
			case ORDER_ADDED:
				if (response.isSucceed() && newOrderController != null) {
					newOrderController.setOrderAndGoToNextPage((Order) response.getData());
				}
				break;

			// Checked whether an order exists and it does NOT.
			case ORDER_NOT_EXISTS:
				if (response.isSucceed() && newOrderController != null) {
					newOrderController.orderExistFuture.complete(false);
				}
				break;

			// Checked whether an order exists and it does.
			case ORDER_ALREADY_EXISTS:
				if (response.isSucceed() && newOrderController != null) {
					newOrderController.orderExistFuture.complete(true);
				}
				break;

			// Subscriber details fetched successfully.
			case SUBSCRIBER_DETAILS:
				if (response.isSucceed()) {
					setSubscriber((Subscriber) response.getData());
				}
				break;

			// Report about subscribers with late pickups.
			case LATE_PICKUP_COUNTS:
				if (response.isSucceed()) {
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
				}
				break;

			// Load details of specific parking event.
			case PARKING_INFO_LOADED:
				if (viewActiveParkingInfoController != null) {
					if (response.isSucceed()) {
						viewActiveParkingInfoController.setParkingEvent((ParkingEvent) response.getData());
						viewActiveParkingInfoController.setTexts();
					} else {
						viewActiveParkingInfoController.noActiveParking();
					}
				}
				break;

			// Load list of active parkings.
			case ACTIVE_PARKINGS:
				if (response.isSucceed() && viewActiveParkingsController != null) {
					ArrayList<ParkingEvent> events = (ArrayList<ParkingEvent>) response.getData();
					viewActiveParkingsController.onActiveParkingsReceived(events);
				}
				break;

			// Parking session was extended.
			case PARKING_SESSION_EXTENDED:
				if (response.getMsg() != null && extendParkingController != null) {
					extendParkingController.onExtensionResponse(response.isSucceed(), response.getMsg());
				}
				break;
			// action of register new subscriber ended.
			case SUBSCRIBER_INSERTED:
				// If the registration failed and the server sent a list of duplicate fields
				// (like "username", "email", etc.)
				if (registerSubscriberController != null) {
					if (!response.isSucceed() && response.getData() instanceof List<?> list) {
						for (Object fieldObj : list) {
							if (fieldObj instanceof String field)
								// Show the appropriate error next to the corresponding input field
								registerSubscriberController.showDuplicateError(field);
						}
					} else {
						// Otherwise (either success or general failure), show the status at the bottom
						// label
						registerSubscriberController.showStatusFromServer(response.getMsg(), response.isSucceed());
					}
					break;
				} else {
					// Just in case the controller wasn't loaded properly (fallback for unexpected)
					// situations)
					UiUtils.showAlert("Subscriber Registration", response.getMsg(),
							response.isSucceed() ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);

				}
				break;
			// return the parking availability to the subscriber
			case PARKING_AVALIABILITY:
				if (response.isSucceed() && response.getData() instanceof Object[] stats
						&& availabilityController != null) {
					availabilityController.updateAvailability(stats);

				}
				break;
			// return the order is valid
			case RESERVATION_VALID:
				if (newOrderController != null) {
					newOrderController.makingReservation(true);

				}
				break;
			// return the order is invalid
			case RESERVATION_INVALID:
				if (newOrderController != null) {
					newOrderController.makingReservation(false);

				}
				break;
			// return subscriber status report
			case SUBSCRIBER_STATUS:
				if (subscriberStatusController != null && response.getData() instanceof List<?> listRaw) {
					if (response.isSucceed()) {
						List<SubscriberStatusReport> list = (List<SubscriberStatusReport>) listRaw;
						subscriberStatusController.onReportReceived(list);
						break;
					} else {
						// fail: no snapshot for that month
						UiUtils.showAlert("Subscriber Report", response.getMsg(), // e.g. "No snapshot available for
																					// 4/2025"
								Alert.AlertType.INFORMATION);
						break;
					}
				}
				// return if subscriber code is valid
			case SUBSCRIBER_CODE:
				if (newDeliveryController != null) {
					if (response.isSucceed()) {
						newDeliveryController.subscriberCodeIsValid();
						break;
					} else {
						newDeliveryController.subscriberCodeDoesntExist();
						break;
					}
				}
				// return- there is a reservation in delivery
			case RESERVATION_EXISTS:
				if (newDeliveryController != null) {
					newDeliveryController.hasReservation();

				}
				break;
			// return there isn't a reservation in delivery
			case RESERVATION_NOT_EXISTS:
				if (newDeliveryController != null) {
					newDeliveryController.NoReservation();
				}
				break;
			// return if confirmation code is valid
			case CONFIRMATION_CODE_VALIDATION:
				if (newDeliveryController != null) {
					if (response.isSucceed()) {
						newDeliveryController.confirmationCodeIsValid();
						break;
					} else {
						newDeliveryController.confirmationCodeNotValid();
						break;
					}
				}
				break;
			// return if there is empty space
			case PARKING_SPACE_AVAILABILITY:
				if (newDeliveryController != null) {
					if (response.isSucceed()) {
						// gathering the available parking space
						int parkingSpace = (int) response.getData();
						// setting the gathered parking space in the delivery controller
						newDeliveryController.setParkingSpace(parkingSpace);
						// setting the status of the parking lot to not be full
						newDeliveryController.setParkingLotStatus(true);
						break;
					} else {
						newDeliveryController.setParkingLotStatus(false);
						break;
					}
				}
				break;
			// return vehicle id
			case VEHICLE_ID:
				if (newDeliveryController != null) {
					String vehicleID = (String) response.getData();
					newDeliveryController.vehicleIdFuture.complete(vehicleID);
				}
				break;
			// deliver the vehicle into lot
			case DELIVER_VEHICLE:
				if (newDeliveryController != null) {
					newDeliveryController.successfulDelivery();
				}
				break;
			// check if the tag is exists in the system
			case TAG_EXISTS:
				if (newDeliveryController != null) {
					if (response.isSucceed()) {
						newDeliveryController.tagFound();
						break;
					} else {
						newDeliveryController.tagNotFound();
						break;
					}
				}
				break;
			// return subscriber code
			case MATCHED_SUBSCRIBER_TO_TAG:
				if (newDeliveryController != null) {
					int subCode = (int) response.getData();
					newDeliveryController.subCodeFuture.complete(subCode);
				}
				break;
			// return subscriber vehicle is inside lot by the tag
			case SUBSCRIBER_VEHICLE_ISNT_INSIDE:
				if (newDeliveryController != null) {
					if (response.isSucceed()) {
						newDeliveryController.checkIfTheresReservation();
						break;
					} else {
						newDeliveryController.vehicleIsAlreadyInside();
						break;
					}
				}
				// return subscriber vehicle not inside lot by the tag
			case SUBSCRIBER_VEHICLE_ISNT_INSIDE_BY_TAG:
				if (newDeliveryController != null) {
					newDeliveryController.findMatchedSubToTheTag();
				}
				break;
			default:
				System.out.println("Server response msg: " + response.getMsg());
				System.out.println("Success? " + response.isSucceed());
				// General error message pop up (only if not handled before)
				if (!response.isSucceed()) {
					UiUtils.showAlert("System Message", response.getMsg(), Alert.AlertType.ERROR);
					break;
				}

			}

		});
	}

	/**
	 * Clears all user-specific session data from memory. This is called when
	 * logging out or switching accounts.
	 */
	public void clearSession() {
		this.subscriber = null;
		this.password = null;

		// General UI
		this.mainController = null;
		this.mainLayoutController = null;
		this.staffMainLayoutController = null;
		this.terminalController = null;

		// Subscriber-related
		this.subscriberMainController = null;
		this.watchAndCancelOrdersController = null;
		this.viewSubscriberDetailsController = null;
		this.editSubscriberDetailsController = null;
		this.viewParkingHistoryController = null;
		this.viewActiveParkingInfoController = null;
		this.viewActiveParkingsController = null;
		this.newOrderController = null;
		this.summaryController = null;
		this.subscriberStatusController = null;

		// Guest & Registration
		this.guestMainController = null;
		this.registerSubscriberController = null;

		// Terminal operations
		this.pickupController = null;
		this.newDeliveryController = null;

		// Staff views
		this.viewSubscribersInfoController = null;
		this.availabilityController = null;
		this.parkingReportController = null;

		System.out.println("[DEBUG] Client session cleared.");
	}

}
