package client;

import common.Order;
import common.ServerResponse;
import common.Subscriber;
import common.User;
import common.UserRole;
import controllers.CreateNewOrderViewController;
import controllers.GuestMainController;
import controllers.LoginController;
import controllers.OrderViewController;
import controllers.ParkingReservationSummaryController;
import controllers.SubscriberMainController;
import controllers.VehicleDeliveryController;
import controllers.VehiclePickupController;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import ocsf.client.AbstractClient;
import ui.UiUtils;

import java.io.IOException;
import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;

/**
 * Handles network communication between the BPARK client and server. Wraps
 * client requests and processes server responses.
 */
public class ClientController extends AbstractClient {

	/*
	 * ------------------------------------------------------------------ UI
	 * Controllers (injected after screen load)
	 * ------------------------------------------------------------------
	 */

	/** Controller for displaying order data */
	private OrderViewController controller;

	/** Controller for handling login screen flow */
	private LoginController loginController;

	/** Controller for vehicle pickup screen (subscriber) */
	private VehiclePickupController pickupController;

	/** Controller for new Guest page */
	private GuestMainController guestMainController;

	private CreateNewOrderViewController newOrderController;
	private ParkingReservationSummaryController summaryController;
	private SubscriberMainController subscriberMainController;
	private VehicleDeliveryController newDeliveryController;

	private Subscriber subscriber;

	/*
	 * ------------------------------------------------------------------
	 * Constructor
	 * ------------------------------------------------------------------
	 */

	/**
	 * Constructs a new ClientController instance.
	 *
	 * @param host the server's IP or hostname
	 * @param port the server's listening port
	 */
	public ClientController(String host, int port) {
		super(host, port);
	}

	/*
	 * ------------------------------------------------------------------ Setters /
	 * Getters ------------------------------------------------------------------
	 */

	/**
	 * Sets the OrderViewController used to update order data on screen.
	 *
	 * @param orderViewController the order screen controller
	 */
	public void setController(OrderViewController orderViewController) {
		this.controller = orderViewController;
	}

	/** @return the active order controller */
	public OrderViewController getController() {
		return controller;
	}

	/**
	 * Sets the login screen controller.
	 *
	 * @param loginController the login screen controller
	 */
	public void setLoginController(LoginController loginController) {
		this.loginController = loginController;
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

	/**
	 * set new order controller
	 * 
	 * @param controller
	 */
	public void setNewOrderController(CreateNewOrderViewController controller) {
		this.newOrderController = controller;
	}

	public void setSummaryController(ParkingReservationSummaryController controller) {
		this.summaryController = controller;

	}
	
	public void setDeliveryController(VehicleDeliveryController controller) {
		this.newDeliveryController = controller;

	}

	public void setSubscriber(Subscriber subscriber) {
		this.subscriber = subscriber;
	}

	public Subscriber getSubscriber() {
		return subscriber;
	}

	/*
	 * ------------------------------------------------------------------ Server
	 * Response Handling
	 * ------------------------------------------------------------------
	 */

	/**
	 * Processes messages received from the server. Handles login results, order
	 * data, error messages, vehicle pickup responses, and other system messages.
	 *
	 * @param msg the message sent from the server (expected to be ServerResponse or
	 *            String)
	 */
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
			// ------------------------------
			// Login results (success or fail)
			// ------------------------------

			if (response.isSucceed() && response.getData() instanceof User user) {
				if (loginController != null) {
					loginController.handleLoginSuccess(user);
				}
				return;
			} else if (!response.isSucceed() && loginController != null
					&& response.getMsg().toLowerCase().contains("invalid")) {
				loginController.handleLoginFailure(response.getMsg());
				return;
			}

			// ------------------------------
			// Order view controller feedback
			// ------------------------------

			else if (response.isSucceed() && response.getData() instanceof ArrayList<?> dataList && !dataList.isEmpty()
					&& dataList.get(0) instanceof Order) {
				@SuppressWarnings("unchecked")
				ArrayList<Order> orders = (ArrayList<Order>) dataList;
				if (controller != null) {
					controller.displayOrders(orders);
				}
				return;
			} else if (controller != null) {
				UiUtils.setStatus(controller.getStatusLabel(), response.getMsg(), response.isSucceed());
			}

			// General error message popup (only if not handled before)
			if (!response.isSucceed()) {
				UiUtils.showAlert("System Message", response.getMsg(), Alert.AlertType.ERROR);
			}

            // ------------------------------
            // Handle vehicle pickup responses
            // ------------------------------
            if (pickupController != null) {
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
			}
		});
	}

	/*
	 * ------------------------------------------------------------------
	 * Order-related requests
	 * ------------------------------------------------------------------
	 */

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

	/** Requests all orders from the server. */
	public void requestAllOrders() {
		try {
			sendToServer(new Object[] { "getAllOrders" });
			if (controller != null)
				UiUtils.setStatus(controller.getStatusLabel(), "Orders loaded successfully", true);
		} catch (IOException e) {
			System.err.println("Failed to send 'getAllOrders' request: " + e.getMessage());
		}
	}

	/**
	 * Requests a specific order by ID.
	 *
	 * @param orderNumber the order ID
	 */
	public void requestOrderByOrderNum(int orderNumber) {
		try {
			sendToServer(new Object[] { "getOrder", orderNumber });
		} catch (IOException e) {
			System.err.println("Failed to send 'getOrder' request: " + e.getMessage());
		}
	}

	/**
	 * Sends an update for a specific field in an order.
	 *
	 * @param orderNumber the order ID
	 * @param field       the field to update
	 * @param newValue    the new value to set
	 */
	public void updateOrder(int orderNumber, String field, String newValue) {
		try {
			sendToServer(new Object[] { "updateOrder", orderNumber, field, newValue });
		} catch (IOException e) {
			System.err.println("Failed to send 'updateOrder' request: " + e.getMessage());
		}
	}

	public void checkAvailability(Date date, Time time) {
		try {
			System.out.println("checkAvailability-client");
			sendToServer(new Object[] { "checkAvailability", date, time });
		} catch (IOException e) {
			System.err.println("Failed to send 'checkAvailability' request to server: " + e.getMessage());
		}
	}

	/*
	 * ------------------------------------------------------------------ Login /
	 * Subscriber flow
	 * ------------------------------------------------------------------
	 */

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
            sendToServer(new Object[]{"validateSubscriberByTag", tagId});
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
	 * Sends a request to resend the parking code (SMS + email).
	 *
	 * @param subscriberCode the subscriber code
	 */
	public void sendLostParkingCode(int subscriberCode) {
		try {
			sendToServer(new Object[] { "sendLostCode", subscriberCode });
		} catch (IOException e) {
			System.err.println("Failed to send 'sendLostCode' request: " + e.getMessage());
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

	public void setGuestController(GuestMainController controller) {
		this.guestMainController = controller;
	}

	public void setSubscriberMainController(SubscriberMainController controller) {
		this.subscriberMainController = controller;
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
	
}
