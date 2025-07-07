package client;

import common.*;
import controllers.*;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import ocsf.client.AbstractClient;
import ui.UiUtils;

import java.io.IOException;
import java.sql.Date;
import java.sql.Time;
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

	// UI controllers (setters are used from JavaFX side)
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
	private StaffMainLayoutController staffMainLayoutController;
	private ExtendParkingController extendParkingController;
	private RegisterSubscriberController registerSubscriberController;
	private AvailabilityController availabilityController;
	private ParkingReportController parkingReportController;
	private SubscriberStatusController subscriberStatusController;

	/**
	 * Stores the logged-in subscriber object during session (null if not logged in)
	 */
	private Subscriber subscriber;

	/** Stores the user's password during the current session */
	private String password;

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

	public void setLoginController(LoginController c) {
		this.loginController = c;
	}

	public void setPickupController(VehiclePickupController c) {
		this.pickupController = c;
	}

	public void setGuestMainController(GuestMainController c) {
		this.guestMainController = c;
	}

	public void setNewOrderController(CreateNewOrderViewController c) {
		this.newOrderController = c;
	}

	public void setSummaryController(ParkingReservationSummaryController c) {
		this.summaryController = c;
	}

	public void setSubscriberMainController(SubscriberMainController c) {
		this.subscriberMainController = c;
	}

	public void setDeliveryController(VehicleDeliveryController c) {
		this.newDeliveryController = c;
	}

	public void setMainLayoutController(SubscriberMainLayoutController c) {
		this.mainLayoutController = c;
	}

	public SubscriberMainLayoutController getMainLayoutController() {
		return mainLayoutController;
	}

	public void setWatchAndCancelOrdersController(WatchAndCancelOrdersController c) {
		this.watchAndCancelOrdersController = c;
	}

	public WatchAndCancelOrdersController getWatchAndCancelOrdersController() {
		return watchAndCancelOrdersController;
	}

	public void setViewSubscriberDetailsController(ViewSubscriberDetailsController c) {
		this.viewSubscriberDetailsController = c;
	}

	public void setEditSubscriberDetailsController(EditSubscriberDetailsController c) {
		this.editSubscriberDetailsController = c;
	}

	public EditSubscriberDetailsController getEditSubscriberDetailsController() {
		return editSubscriberDetailsController;
	}

	public void setViewParkingHistoryController(ViewParkingHistoryController c) {
		this.viewParkingHistoryController = c;
	}

	public ViewParkingHistoryController getViewParkingHistoryController() {
		return viewParkingHistoryController;
	}

	public void setViewSubscribersInfoController(ViewSubscribersInfoController c) {
		this.viewSubscribersInfoController = c;
	}

	public ViewSubscribersInfoController getViewSubscribersInfoController() {
		return viewSubscribersInfoController;
	}

	public void setViewActiveParkingsController(ViewActiveParkingsController c) {
		this.viewActiveParkingsController = c;
	}

	public ViewActiveParkingsController getViewActiveParkingsController() {
		return viewActiveParkingsController;
	}

	public void setViewActiveParkingInfoController(ViewActiveParkingInfoController c) {
		this.viewActiveParkingInfoController = c;
	}

	public ViewActiveParkingInfoController getViewActiveParkingInfoController() {
		return viewActiveParkingInfoController;
	}

	public void setMainController(MainController c) {
		this.mainController = c;
	}

	public void setTerminalController(TerminalMainLayoutController c) {
		this.terminalController = c;
	}

	public void setStaffMainLayoutController(StaffMainLayoutController c) {
		this.staffMainLayoutController = c;
	}

	public void setExtendParkingController(ExtendParkingController c) {
		this.extendParkingController = c;
	}

	public ExtendParkingController getExtendParkingController() {
		return extendParkingController;
	}

	public void setRegisterSubscriberController(RegisterSubscriberController c) {
		this.registerSubscriberController = c;
	}

	public RegisterSubscriberController getRegisterSubscriberController() {
		return registerSubscriberController;
	}

	public void setAvailabilityController(AvailabilityController c) {
		this.availabilityController = c;
	}

	public AvailabilityController getAvailabilityController() {
		return availabilityController;
	}

	public void setParkingReportController(ParkingReportController c) {
		this.parkingReportController = c;
	}

	public ParkingReportController getParkingReportController() {
		return parkingReportController;
	}

	public void setSubscriberStatusController(SubscriberStatusController c) {
		this.subscriberStatusController = c;
	}

	public SubscriberStatusController getSubscriberStatusController() {
		return subscriberStatusController;
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

			// Handle based on the type of response
			switch ((ResponseType) response.getType()) {
			case LOGIN_FAILED:
				if (!response.isSucceed() && loginController != null) {
					loginController.handleLoginFailure(response.getMsg());
				}
				break;
			case PARKING_REPORT_LOADED:
				if (response.isSucceed() && parkingReportController != null) {
					parkingReportController.setParkingReport((ParkingReport) response.getData());
					parkingReportController.setChart();
				}
				break;
			case LOGIN_SUCCESSFULL:
				if (response.isSucceed() && loginController != null) {
					loginController.handleLoginSuccess((User) response.getData());
				}
				break;
			case ORDER_DELETED:
				if (response.isSucceed() && watchAndCancelOrdersController != null) {
					getRequestSender().askForReservations();
				}
				break;
			case CONFLICT_CHECKED:
				if (newOrderController != null) {
					boolean hasConflict = (boolean) response.getData();
					newOrderController.onReservationConflictCheck(hasConflict);
					break;
				}

			case PARKING_HISTORY_LOADED:
				if (response.isSucceed() && viewParkingHistoryController != null) {
					viewParkingHistoryController.displayHistory((ArrayList<ParkingEvent>) response.getData());
				}
				break;
			case ORDERS_DISPLAY:
				if (response.isSucceed() && response.getData() instanceof ArrayList<?> dataList
						&& watchAndCancelOrdersController != null) {
					ArrayList<Order> orders = (ArrayList<Order>) dataList;
					watchAndCancelOrdersController.displayOrders(orders);
				}
				break;
			case NO_ORDERS:
				if (response.isSucceed() && watchAndCancelOrdersController != null) {
					watchAndCancelOrdersController.displayOrders(new ArrayList<Order>());
					UiUtils.showAlert(response.getMsg(), response.getMsg(), Alert.AlertType.INFORMATION);
				}
				break;
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
			case PICKUP_VEHICLE:
				if (pickupController != null) {
					UiUtils.setStatus(pickupController.getStatusLabel(), response.getMsg(), response.isSucceed());
					pickupController.disableAfterPickup();
				}
				break;
			case REPOSTS_DATE_LOADED:
				if (response.isSucceed() && parkingReportController != null) {
					parkingReportController.setDates((ArrayList<Date>) (response.getData()));
				}
				break;
			case ORDER_ADDED:
				if (response.isSucceed() && newOrderController != null) {
					newOrderController.setOrderAndGoToNextPage((Order) response.getData());
				}
				break;
			case ORDER_NOT_EXISTS:
				if (response.isSucceed() && newOrderController != null) {
					newOrderController.orderExistFuture.complete(false);
				}
				break;
			case ORDER_ALREADY_EXISTS:
				if (response.isSucceed() && newOrderController != null) {
					newOrderController.orderExistFuture.complete(true);
					
				}
				break;
			case SUBSCRIBER_DETAILS:
				if (response.isSucceed()) {
					setSubscriber((Subscriber) response.getData());
				}
				break;
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
				break; // handled
			case PARKING_INFO_LOADED:
				if (response.isSucceed() && viewActiveParkingInfoController != null) {
					viewActiveParkingInfoController.setParkingEvent((ParkingEvent) response.getData());
					viewActiveParkingInfoController.setTexts();	
				}
				break;
			case ACTIVE_PARKINGS:
				if (response.isSucceed() && viewActiveParkingsController != null) {
					ArrayList<ParkingEvent> events = (ArrayList<ParkingEvent>) response.getData();
					viewActiveParkingsController.onActiveParkingsReceived(events);
					
				}
				break;
			case PARKING_SESSION_EXTENDED:
				if (response.getMsg() != null && extendParkingController != null) {
					extendParkingController.onExtensionResponse(response.isSucceed(), response.getMsg());
					
				}
				break;
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
					// Just in case the controller wasn't loaded properly (fallback for unexpected
					// situations)
					UiUtils.showAlert("Subscriber Registration", response.getMsg(),
							response.isSucceed() ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
					
				}
				break;
			case PARKING_AVALIABILITY:
				if (response.isSucceed() && response.getData() instanceof Object[] stats
						&& availabilityController != null) {
					availabilityController.updateAvailability(stats);
					
				}
				break;
			case RESERVATION_VALID:
				if (newOrderController != null) {
					newOrderController.makingReservation(true);
					
				}
				break;
			case RESERVATION_INVALID:
				if (newOrderController != null) {
					newOrderController.makingReservation(false);
					
				}
				break;
			case SUBSCRIBER_STATUS:
				if (subscriberStatusController != null && response.getData() instanceof List<?> listRaw) {
					if (response.isSucceed()) {
						@SuppressWarnings("unchecked")
						List<SubscriberStatusReport> list = (List<SubscriberStatusReport>) listRaw;
						subscriberStatusController.onReportReceived(list);
						break;
					} else {
						/* ----- fail: no snapshot for that month ----- */
						UiUtils.showAlert("Subscriber Report", response.getMsg(), // e.g. "No snapshot available for
																					// 4/2025"
								Alert.AlertType.INFORMATION);
						break;
					}
				}

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

			case RESERVATION_EXISTS:
				if (newDeliveryController != null) {
					newDeliveryController.hasReservation();
					
				}
				break;
			case RESERVATION_NOT_EXISTS:
				if (newDeliveryController != null) {
					newDeliveryController.NoReservation();
					
				}
				break;
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
			case VEHICLE_ID:
				if (newDeliveryController != null) {
					String vehicleID = (String) response.getData();
					newDeliveryController.vehicleIdFuture.complete(vehicleID);
				}
				break;
			case DELIVER_VEHICLE:
				if (newDeliveryController != null) {
					newDeliveryController.successfulDelivery();
				}
				break;
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
			case MATCHED_SUBSCRIBER_TO_TAG:
				if (newDeliveryController != null) {
					int subCode = (int) response.getData();
					newDeliveryController.subCodeFuture.complete(subCode);
				}
				break;
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
