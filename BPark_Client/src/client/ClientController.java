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
 * This class handles communication with the server.
 * It receives messages from the server and updates the UI accordingly.
 * Any request-sending logic was moved to {@link ClientRequestSender}.
 */
public class ClientController extends AbstractClient {

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

    private Subscriber subscriber;
    private String password;

    /**
     * Initializes a new network client.
     */
    public ClientController(String host, int port) {
        super(host, port);
        this.requestSender = new ClientRequestSender(this);
    }

    /** @return a reference to the helper class that sends requests */
    public ClientRequestSender getRequestSender() {
        return requestSender;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setLoginController(LoginController c) { this.loginController = c; }
    public void setPickupController(VehiclePickupController c) { this.pickupController = c; }
    public void setGuestMainController(GuestMainController c) { this.guestMainController = c; }
    public void setNewOrderController(CreateNewOrderViewController c) { this.newOrderController = c; }
    public void setSummaryController(ParkingReservationSummaryController c) { this.summaryController = c; }
    public void setSubscriberMainController(SubscriberMainController c) { this.subscriberMainController = c; }
    public void setDeliveryController(VehicleDeliveryController c) { this.newDeliveryController = c; }
    public void setMainLayoutController(SubscriberMainLayoutController c) { this.mainLayoutController = c; }
    public SubscriberMainLayoutController getMainLayoutController() { return mainLayoutController; }
    public void setWatchAndCancelOrdersController(WatchAndCancelOrdersController c) { this.watchAndCancelOrdersController = c; }
    public WatchAndCancelOrdersController getWatchAndCancelOrdersController() { return watchAndCancelOrdersController; }
    public void setViewSubscriberDetailsController(ViewSubscriberDetailsController c) { this.viewSubscriberDetailsController = c; }
    public void setEditSubscriberDetailsController(EditSubscriberDetailsController c) { this.editSubscriberDetailsController = c; }
    public EditSubscriberDetailsController getEditSubscriberDetailsController() { return editSubscriberDetailsController; }
    public void setViewParkingHistoryController(ViewParkingHistoryController c) { this.viewParkingHistoryController = c; }
    public ViewParkingHistoryController getViewParkingHistoryController() { return viewParkingHistoryController; }
    public void setViewSubscribersInfoController(ViewSubscribersInfoController c) { this.viewSubscribersInfoController = c; }
    public ViewSubscribersInfoController getViewSubscribersInfoController() { return viewSubscribersInfoController; }
    public void setViewActiveParkingsController(ViewActiveParkingsController c) { this.viewActiveParkingsController = c; }
    public ViewActiveParkingsController getViewActiveParkingsController() { return viewActiveParkingsController; }
    public void setViewActiveParkingInfoController(ViewActiveParkingInfoController c) { this.viewActiveParkingInfoController = c; }
    public ViewActiveParkingInfoController getViewActiveParkingInfoController() { return viewActiveParkingInfoController; }
    public void setMainController(MainController c) { this.mainController = c; }
    public void setTerminalController(TerminalMainLayoutController c) { this.terminalController = c; }
    public void setStaffMainLayoutController(StaffMainLayoutController c) { this.staffMainLayoutController = c; }
    public void setExtendParkingController(ExtendParkingController c) { this.extendParkingController = c; }
    public ExtendParkingController getExtendParkingController() { return extendParkingController; }
    public void setRegisterSubscriberController(RegisterSubscriberController c) { this.registerSubscriberController = c; }
    public RegisterSubscriberController getRegisterSubscriberController() { return registerSubscriberController; }
    public void setAvailabilityController(AvailabilityController c) { this.availabilityController = c; }
    public AvailabilityController getAvailabilityController() { return availabilityController; }
    public void setParkingReportController(ParkingReportController c) { this.parkingReportController = c; }
    public ParkingReportController getParkingReportController() { return parkingReportController; }
    public void setSubscriberStatusController(SubscriberStatusController c) { this.subscriberStatusController = c; }
    public SubscriberStatusController getSubscriberStatusController() { return subscriberStatusController; }

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

			if(response.getType() == null) {
				System.out.println("Server response msg: " + response.getMsg());
				System.out.println("Success? " + response.isSucceed());
				// General error message pop up (only if not handled before)
				if (!response.isSucceed()) {
					UiUtils.showAlert("System Message", response.getMsg(), Alert.AlertType.ERROR);
				}
				return;
			}
			
			
			switch((ResponseType)response.getType()) {
			case LOGIN_FAILED :
				if (!response.isSucceed() && loginController != null) {
					loginController.handleLoginFailure(response.getMsg());
					break;
				}

			case PARKING_REPORT_LOADED:
				if (response.isSucceed() && parkingReportController!=null) {
					parkingReportController.setParkingReport((ParkingReport)response.getData());
					parkingReportController.setChart();
					break;
				}

			case LOGIN_SUCCESSFULL :
				if(response.isSucceed() && loginController != null) {
					loginController.handleLoginSuccess((User)response.getData());
					break;
				}

			case ORDER_DELETED :
				if(response.isSucceed() && watchAndCancelOrdersController != null) {
					askForReservations();
				}

			case PARKING_HISTORY_LOADED :
				if (response.isSucceed() && viewParkingHistoryController != null) {
					viewParkingHistoryController.displayHistory((ArrayList<ParkingEvent>) response.getData());
					break;
				}

			case ORDERS_DISPLAY :
				if (response.isSucceed() && response.getData() instanceof ArrayList<?> dataList && watchAndCancelOrdersController != null) {
					ArrayList<Order> orders = (ArrayList<Order>) dataList;
					watchAndCancelOrdersController.displayOrders(orders);
					break;
				}

			case NO_ORDERS :
				if (response.isSucceed() && watchAndCancelOrdersController != null) {
					watchAndCancelOrdersController.displayOrders(new ArrayList<Order>());
					UiUtils.showAlert(response.getMsg(), response.getMsg(), Alert.AlertType.INFORMATION);
					break;
				}

			case DETAILS_UPDATED :
				if(response.isSucceed()) {
					ArrayList<Object> newDetails = (ArrayList<Object>) response.getData();
					if (newDetails.get(0) instanceof Subscriber) {
						setSubscriber((Subscriber) newDetails.get(0));
						newDetails.remove(0);
					}
					if (!newDetails.isEmpty()) {
						setPassword(((User) newDetails.get(0)).getPassword());
					}
					editSubscriberDetailsController.handleGoToView();
					break;
				}

			case SUBSCRIBER_VERIFIED :
				if (pickupController != null && response.isSucceed()) {
					UiUtils.setStatus(pickupController.getStatusLabel(), response.getMsg(), response.isSucceed());

					if (response.getData() instanceof Integer subscriberCode) {
						// Subscriber was validated using tag ID
						pickupController.onSubscriberValidated(subscriberCode);
						break;
					} else {
						// Subscriber was validated using numeric code input
						pickupController.onSubscriberValidated();
						break;
					}
				}

			case PICKUP_VEHICLE :
				if (pickupController != null) {
					UiUtils.setStatus(pickupController.getStatusLabel(), response.getMsg(), response.isSucceed());
					pickupController.disableAfterPickup();
					break;
				}

			case REPOSTS_DATE_LOADED :
				if (response.isSucceed() && parkingReportController != null) {
					parkingReportController.setDates((ArrayList<Date>)(response.getData()));
					break;
				}

			case ORDER_ADDED :
				if (response.isSucceed() && newOrderController != null) {
					newOrderController.setOrderAndGoToNextPage((Order) response.getData());
					break;
				}

			case ORDER_NOT_EXISTS : 
				if (response.isSucceed() && newOrderController != null) {
					newOrderController.orderExistFuture.complete(false);
					break;
				}

			case ORDER_ALREADY_EXISTS :
				if (response.isSucceed() && newOrderController != null) {
					newOrderController.orderExistFuture.complete(true);
					break;
				}

			case SUBSCRIBER_DETAILS :
				if (response.isSucceed()) {
					setSubscriber((Subscriber) response.getData());
					break;
				}

			case LATE_PICKUP_COUNTS :
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

					break; // handled

				}

			case PARKING_INFO_LOADED :
				if(response.isSucceed() && viewActiveParkingInfoController!=null) {
					viewActiveParkingInfoController.setParkingEvent((ParkingEvent) response.getData());
					viewActiveParkingInfoController.setTexts();
					break;
				}

			case ACTIVE_PARKINGS :
				if(response.isSucceed() && viewActiveParkingsController != null) {
					ArrayList<ParkingEvent> events = (ArrayList<ParkingEvent>) response.getData();
					viewActiveParkingsController.onActiveParkingsReceived(events);
					break;
				}

			case PARKING_SESSION_EXTENDED :
				if(response.getMsg() != null && extendParkingController != null) {
					extendParkingController.onExtensionResponse(response.isSucceed(), response.getMsg());
					break;
				}

			case SUBSCRIBER_INSERTED :
				if (registerSubscriberController != null) {
					registerSubscriberController.showStatusFromServer(response.getMsg(), response.isSucceed());
					break;
				} else {
					UiUtils.showAlert("Subscriber Registration", response.getMsg(),
							response.isSucceed() ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
					break;
				}

			case PARKING_AVALIABILITY :
				if(response.isSucceed() && response.getData() instanceof Object[] stats &&
						availabilityController != null) {
					availabilityController.updateAvailability(stats);
					break;
				}

			case RESERVATION_VALID :
				if(newOrderController != null) {
					newOrderController.makingReservation(true);
					break;
				}

			case RESERVATION_INVALID :
				if(newOrderController != null) {
					newOrderController.makingReservation(false);
					break;
				}

			case SUBSCRIBER_STATUS :
				if(subscriberStatusController != null &&
				response.getData() instanceof List<?> listRaw) {
					if(response.isSucceed()) {
						@SuppressWarnings("unchecked")
						List<SubscriberStatusReport> list = (List<SubscriberStatusReport>) listRaw;
						subscriberStatusController.onReportReceived(list);
						break;
					}
					else {
						/* ----- fail: no snapshot for that month ----- */
						UiUtils.showAlert(
								"Subscriber Report",
								response.getMsg(),              // e.g. "No snapshot available for 4/2025"
								Alert.AlertType.INFORMATION);
						break;
					}
				}

			case SUBSCRIBER_CODE :
				if (newDeliveryController != null) {
					if(response.isSucceed()) {
						newDeliveryController.subscriberCodeIsValid();
						break;
					}
					else {
						newDeliveryController.subscriberCodeDoesntExist();
						break;
					}
				}

			case RESERVATION_EXISTS :
				if (newDeliveryController != null) {
					newDeliveryController.hasReservation();
					break;
				}

			case RESERVATION_NOT_EXISTS : 
				if (newDeliveryController != null) {
					newDeliveryController.NoReservation();
					break;
				}

			case CONFIRMATION_CODE_VALIDATION :
				if (newDeliveryController != null) {
					if(response.isSucceed()) {
						newDeliveryController.confirmationCodeIsValid();
						break;
					}
					else {
						newDeliveryController.confirmationCodeNotValid();
						break;
					}
				}

			case PARKING_SPACE_AVAILABILITY :
				if (newDeliveryController != null) {
					if(response.isSucceed()) {
						// gathering the available parking space
						int parkingSpace = (int) response.getData();
						// setting the gathered parking space in the delivery controller
						newDeliveryController.setParkingSpace(parkingSpace);
						// setting the status of the parking lot to not be full
						newDeliveryController.setParkingLotStatus(true);
						break;
					}
					else {
						newDeliveryController.setParkingLotStatus(false);
						break;
					}
				}
				
			case VEHICLE_ID :
				if (newDeliveryController != null) {
					String vehicleID = (String) response.getData();
					newDeliveryController.vehicleIdFuture.complete(vehicleID);
					break;
				}
				
			case DELIVER_VEHICLE :
				if (newDeliveryController != null) {
					newDeliveryController.successfulDelivery();
					break;
				}
				
			case TAG_EXISTS :
				if (newDeliveryController != null) {
					if(response.isSucceed()) {
						newDeliveryController.tagFound();
						break;
					}
					else {
						newDeliveryController.tagNotFound();
						break;
					}
				}
				
			case MATCHED_SUBSCRIBER_TO_TAG :
				if (newDeliveryController != null) {
					int subCode = (int) response.getData();
					newDeliveryController.subCodeFuture.complete(subCode);
					break;
				}
				
			case SUBSCRIBER_VEHICLE_ISNT_INSIDE :
				if (newDeliveryController != null) {
					if(response.isSucceed()) {
						newDeliveryController.checkIfTheresReservation();
						break;
					}
					else {
						newDeliveryController.vehicleIsAlreadyInside();
						break;
					}
				}
				
			case SUBSCRIBER_VEHICLE_ISNT_INSIDE_BY_TAG :
				if (newDeliveryController != null) {
					newDeliveryController.findMatchedSubToTheTag();
					break;
				}

			default :
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
	 * add new order to order table
	 * 
	 * @param newOrder
	 */
	public void addNewOrder(Order newOrder) {
		try {
			sendToServer(new Object[] { Operation.ADD_NEW_ORDER, newOrder });
		} catch (IOException e) {
			System.err.println("Failed to send 'addNewOrder' request to server: " + e.getMessage());
		}
	}

	public void checkAvailability(Date date, Time time) {
		try {
			sendToServer(new Object[] { Operation.CHECK_AVAILABILITY_FOR_ORDER, date, time });
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
			sendToServer(new Object[] { Operation.LOGIN, username, password });
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
			sendToServer(new Object[] { Operation.VALIDATE_SUBSCRIBER_BY_SUBSCRIBER_CODE, subscriberCode });
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
			sendToServer(new Object[] { Operation.VALIDATE_SUBSCRIBER_BY_TAG, tagId });
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
			sendToServer(new Object[] { Operation.COLLECT_CAR, subscriberCode, parkingCode });
		} catch (IOException e) {
			System.err.println("Failed to send 'collectCar' request: " + e.getMessage());
		}
	}




	/**
	 * send to server the user to get the subscriber details.
	 * 
	 * @param user
	 */
	public void subscriberDetails(User user) {
		try {
			sendToServer(new Object[] { Operation.SUBSCRIBER_DETAILS, user });
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
			sendToServer(new Object[] { Operation.ASK_FOR_RESERVATIONS, subscriber });
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
			sendToServer(new Object[] { Operation.DELETE_ORDER, orderNumberToDelete });
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
			sendToServer(new Object[] { Operation.UPDATE_DETAILS_OF_SUBSCRIBER, subscriber, user });
		} catch (IOException e) {
			System.err.println("Failed to send 'updateDetailsOfSubscriber' request: " + e.getMessage());
		}
	}

	/**
	 * the function send to server the command, subscriber details
	 */
	public void updateParkingHistoryOfSubscriber() {
		try {
			sendToServer(new Object[] { Operation.UPDATE_PARKING_HISTORY_OF_SUBSCRIBER, subscriber });
		} catch (IOException e) {
			System.err.println("Failed to send 'updateParkingHistoryOfSubscriber' request: " + e.getMessage());
		}
	}

	public void forgotMyParkingCode(int validatedSubscriberCode) {
		try {
			sendToServer(new Object[] { Operation.FORGEOT_MY_PARKING_CODE, validatedSubscriberCode });
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
			sendToServer(new Object[] { Operation.GET_ALL_SUBSCRIBERS });
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
			sendToServer(new Object[] { Operation.GET_ACTIVE_PARKINGS });
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
			sendToServer(new Object[] { Operation.GET_DETAILS_OF_ACTIVE_INFO, subscriber });
		} catch (IOException e) {
			System.err.println("Failed to send 'getDetailsOfActiveInfo' request: " + e.getMessage());
		}
	}

	/**
	 * Sends a request to extend the parking session based on parking code.
	 *
	 * @param parkingCode the parking code entered by the subscriber
	 */
	public void extendParking(int parkingCode, String subscriberCode) {
		try {
			sendToServer(new Object[] { Operation.EXTEND_PARKING, parkingCode, subscriberCode });
		} catch (IOException e) {
			System.err.println("Failed to send 'extendParking' request: " + e.getMessage());
		}
	}

	/**
	 * Sends a request to find out whether there is an existing order for the time asked or not.
	 * 
	 * @param subscriberCode, date and time
	 */
	public void checkIfOrderAlreadyExists(int subscriberCode, Date selectedDate, Time timeOfArrival) {
		try {
			sendToServer(new Object[] { Operation.IS_THERE_AN_EXISTED_ORDER, subscriberCode, selectedDate, timeOfArrival });
		} catch (IOException e) {
			System.err.println("Failed to send 'IS_THERE_AN_EXISTED_ORDER' request: " + e.getMessage());
		}
	}

	/**
	 * Sends a request to register a new subscriber along with their vehicle.
	 * Subscriber code and tag ID will be generated by the server.
	 *
	 * @param subscriber the subscriber to register (without code or tag)
	 * @param vehicleId  the vehicle ID associated with the subscriber
	 */
	public void registerSubscriber(Subscriber subscriber, String vehicleId) {
		try {
			sendToServer(new Object[] { Operation.REGISTER_SUBSCRIBER, subscriber, vehicleId });
		} catch (IOException e) {
			System.err.println("Failed to send 'registerSubscriberWithVehicle' request: " + e.getMessage());
		}
	}


	/**
	 * Requests parking availability stats from the server.
	 * The response includes total, occupied, and available spots.
	 */
	public void requestParkingAvailability() {
		try {
			sendToServer(new Object[] { Operation.GET_PARKING_AVAILABILITY });
		} catch (IOException e) {
			System.err.println("Failed to send 'get_parking_availability' request: " + e.getMessage());
		}
	}

	/**
	 * request parking report data from server.
	 * @param date (of requested report) 
	 */
	public void getParkingReport(Date date) {
		try {
			sendToServer(new Object[] { Operation.GET_PARKING_REPORT, date });
		} catch (IOException e) {
			System.err.println("Failed to send 'GetParkingReport' request: " + e.getMessage());
		}
	}

	/**
	 * request parking report dates from server.
	 */
	public void getDatesOfReports() {
		try {
			sendToServer(new Object[] {Operation.GET_DATES_OF_REPORTS});
		}catch (IOException e) {
			System.err.println("Failed to send 'getDatesOfReports' request: " + e.getMessage());
		}
	}

	public void getSubscriberReport(Integer month, Integer year) {
		try {
			sendToServer(new Object[]{Operation.GET_SUBSCRIBER_STATUS_REPORT, month, year});
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Validates if the subscriber code exists in the DB
	 *
	 * @param subscriberCode to check.
	 */
	public void checkSubscriberExists(int subscriberCode) {
		try {
			sendToServer(new Object[] { Operation.SUBSCRIBER_EXISTS, subscriberCode});
		} catch (IOException e) {
			// Log the error if the update request fails to send
			System.err.println("Failed to send 'subscriberExists' request to server: " + e.getMessage());
		}
	}

	/**
	 * Validates if the TagID exists in the DB
	 *
	 * @param TagID to check
	 */
	public void validateTag(String TagID) {
		try {
			sendToServer(new Object[] { Operation.TAG_EXISTS, TagID });
		} catch (IOException e) {
			// Log the error if the update request fails to send
			System.err.println("Failed to send 'TagExists' request to server: " + e.getMessage());
		}
	}

	/**
	 * Checks if a subscriber is inside the parking lot right now
	 *
	 * @param subscriberCode to check
	 */
	public void checkIfSubscriberAlreadyEntered(int subscriberCode) {
		try {
			sendToServer(new Object[] { Operation.SUBSCRIBER_ALREADY_ENTERED, subscriberCode });
		} catch (IOException e) {
			// Log the error if the update request fails to send
			System.err.println("Failed to send 'subscriberAlreadyEntered' request to server: " + e.getMessage());
		}
	}

	/**
	 * Checks whether a vehicle that is matched with a given TagID is inside the parking lot right now
	 *
	 * @param TagID to check
	 */
	public void checkIfTagIDAlreadyInside(String TagID) {
		try {
			sendToServer(new Object[] { Operation.TAG_ID_ALREADY_ENTERED, TagID});
		} catch (IOException e) {
			// Log the error if the update request fails to send
			System.err.println("Failed to send 'tagIdAlreadyEntered' request to server: " + e.getMessage());
		}
	}

	/**
	 * Finds the subscriber that matches the given TagID.
	 *
	 * @param TagID for seeking the subscriberCode
	 */
	public void findSubscriberWithTag(String TagID) {
		try {
			sendToServer(new Object[] { Operation.FIND_MATCHED_SUBSCRIBER_TO_THE_TAG, TagID});
		} catch (IOException e) {
			// Log the error if the update request fails to send
			System.err.println("Failed to send 'findMatchedSubToTheTag' request to server: " + e.getMessage());
		}
	}
	
	/**
	 * Requests to the server to make a delivery of the vehicle with an active reservation
	 *
	 * @param subscriberCode  
	 * @param confirmationCode The confirmation code of the reservation
	 */
	public void DeliveryViaReservation(int subscriberCode, int confirmationCode) {
		// Prepare the checking - if exists command as an object array and send to server
		try {
			sendToServer(new Object[] {Operation.DELIVERY_VIA_RESERVATION, subscriberCode, confirmationCode});

		} catch (IOException e) {
			// Log the error if the update request fails to send
			System.err.println("Failed to send 'DeliveryViaReservation' request to server: " + e.getMessage());
		}
	}

	/**
	 * Checks if there is free parking space in the given parking lot
	 *
	 * @param parkingLotName
	 */
	public void isThereFreeParkingSpace(String parkingLotName) {
		try {
			sendToServer(new Object[] {Operation.IS_THERE_FREE_PARKING_SPACE, parkingLotName});
		} catch (IOException e) {
			// Log the error if the update request fails to send
			System.err.println("Failed to send 'IsThereFreeParkingSpace' request to server: " + e.getMessage());
		}
	}

	/**
	 * Retrieves the vehicle ID that is matched with the subscriber code
	 *
	 * @param subscriberCode
	 */
	public void seekVehicleID(int subscriberCode) {
		try {
			sendToServer(new Object[] {Operation.GET_VEHICLE_ID, subscriberCode});

		} catch (IOException e) {
			//Log the error if the update request fails to send
			System.err.println("Failed to send 'getVehicleID' request to server: " + e.getMessage());
		}
	}

	/**
	 * Sends a ParkingEvent object to the server to register a vehicle delivery
	 * The event must contain all relevant data except exit date and time (they will be null)
	 *
	 * @param parkingEvent
	 */
	public void deliverVehicle(ParkingEvent parkingEvent) {
		try {
			sendToServer(new Object[] {Operation.DELIVER_VEHICLE, parkingEvent});
		} catch (IOException e) {
			//Log the error if the update request fails to send
			System.err.println("Failed to send 'DeliverVehicle' request to server: " + e.getMessage());
		}
	}

	/**
	 * Checks if the given subscriber has a valid upcoming reservation.
	 * Used after confirming the subscriber has not entered the parking lot
	 *
	 * @param SubscriberCode The subscriber code to check for existing reservations.
	 */
	public void isThereReservation(int SubscriberCode) {
		try {
			sendToServer(new Object[] {Operation.CHECK_IF_THERE_IS_RERSERVATION, SubscriberCode});

		} catch (IOException e) {
			// Log the error if the update request fails to send
			System.err.println("Failed to send 'checkIfTheresReservation' request to server: " + e.getMessage());
		}

	}
	
	/**
	 * Clears all user-specific session data from memory.
	 * This is called when logging out or switching accounts.
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

