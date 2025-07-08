package client;

import common.*;
import java.io.IOException;
import java.sql.Date;
import java.sql.Time;

/**
 * This class is in charge of sending requests from the client to the server.
 * It doesn't deal with responses at all - just sends data when asked.
 * 
 * Think of it as the "transmitter" of the app: other controllers call it
 * whenever they need to communicate something to the server.
 *
 * Example usage from any controller:
 *     client.getRequestSender().requestLogin(...);
 *
 */
public class ClientRequestSender {

	/** The client network layer used to send messages to the server. */
    private final ClientController client;

    /**
     * Basic constructor – keeps a reference to the ClientController
     * so we can call sendToServer() from here.
     *
     * @param client the client network layer
     */
    public ClientRequestSender(ClientController client) {
        this.client = client;
    }

    /**
     * Sends a login request to the server.
     *
     * @param username exact username (case-sensitive)
     * @param password plain-text password
     *
     * @throws IOException if the message cannot be sent
     *                     (e.g. socket closed or network error)
     */
    public void requestLogin(String username, String password) throws IOException {
        Object[] msg = { Operation.LOGIN, username, password };
        client.sendToServer(msg);      // may throw IOException → propagated
    }


    /**
     * Adds a new order to the database.
     * @param newOrder the order to add
     */
    public void addNewOrder(Order newOrder) {
        try {
            client.sendToServer(new Object[] { Operation.ADD_NEW_ORDER, newOrder });
        } catch (IOException e) {
            System.err.println("Failed to send 'addNewOrder' request to server: " + e.getMessage());
        }
    }
    
    /**
     * Checks if the subscriber has any existing reservation within 4 hours of the requested date and time.
     * @param subscriberCode the subscriber's ID
     * @param date the requested reservation date
     * @param time the requested reservation time
     */
    public void checkReservationConflict(int subscriberCode, Date date, Time time) {
        try {
            client.sendToServer(new Object[] {
                    Operation.CHECK_RESERVATION_CONFLICT, subscriberCode, date, time
            });
        } catch (IOException e) {
            System.err.println("Failed to send 'checkReservationConflict' request: " + e.getMessage());
        }
    }


    /**
     * Asks the server if there's availability for the given date and time.
     * @param date the requested date
     * @param time the requested time
     */
    public void checkAvailability(Date date, Time time) {
        try {
            client.sendToServer(new Object[] { Operation.CHECK_AVAILABILITY_FOR_ORDER, date, time });
        } catch (IOException e) {
            System.err.println("Failed to send 'checkAvailability' request: " + e.getMessage());
        }
    }

    /**
     * Checks if a subscriber is valid using their subscriber code.
     * @param subscriberCode the code entered by the subscriber
     */
    public void validateSubscriber(int subscriberCode) {
        try {
            client.sendToServer(new Object[] { Operation.VALIDATE_SUBSCRIBER_BY_SUBSCRIBER_CODE, subscriberCode });
        } catch (IOException e) {
            System.err.println("Failed to send 'validateSubscriber' request: " + e.getMessage());
        }
    }

    /**
     * Validates a subscriber using their RFID tag.
     * @param tagId the tag scanned at the entrance
     */
    public void validateSubscriberByTag(String tagId) {
        try {
            client.sendToServer(new Object[] { Operation.VALIDATE_SUBSCRIBER_BY_TAG, tagId });
        } catch (IOException e) {
            System.err.println("Failed to send 'validateSubscriberByTag' request: " + e.getMessage());
        }
    }

    /**
     * Sends a request to collect a vehicle by subscriber code and parking code.
     * @param subscriberCode the subscriber's ID
     * @param parkingCode the code entered to pick up the car
     */
    public void collectCar(int subscriberCode, int parkingCode) {
        try {
            client.sendToServer(new Object[] { Operation.COLLECT_CAR, subscriberCode, parkingCode });
        } catch (IOException e) {
            System.err.println("Failed to send 'collectCar' request: " + e.getMessage());
        }
    }

    /**
     * Asks the server for all subscriber details linked to the given user.
     * @param user the logged-in user
     */
    public void subscriberDetails(User user) {
        try {
            client.sendToServer(new Object[] { Operation.SUBSCRIBER_DETAILS, user });
        } catch (IOException e) {
            System.err.println("Failed to send 'subscriberDetails' request: " + e.getMessage());
        }
    }

    /**
     * Requests the list of all reservations for the currently logged-in subscriber.
     */
    public void askForReservations() {
        try {
            client.sendToServer(new Object[] { Operation.ASK_FOR_RESERVATIONS, client.getSubscriber() });
        } catch (IOException e) {
            System.err.println("Failed to send 'askForReservations' request: " + e.getMessage());
        }
    }

    /**
     * Sends a request to delete an order by its order number.
     * @param orderNumberToDelete the primary key of the order to delete
     */
    public void deleteOrder(int orderNumberToDelete) {
        try {
            client.sendToServer(new Object[] { Operation.DELETE_ORDER, orderNumberToDelete });
        } catch (IOException e) {
            System.err.println("Failed to send 'deleteOrder' request: " + e.getMessage());
        }
    }

    /**
     * Sends updated subscriber and user details to the server.
     * @param subscriber the updated subscriber info
     * @param user the updated user info
     */	
    public void updateDetailsOfSubscriber(Subscriber subscriber, User user) {
        try {
            client.sendToServer(new Object[] { Operation.UPDATE_DETAILS_OF_SUBSCRIBER, subscriber, user });
        } catch (IOException e) {
            System.err.println("Failed to send 'updateDetailsOfSubscriber' request: " + e.getMessage());
        }
    }

    /**
     * Asks the server for the subscriber’s parking history.
     */
    public void updateParkingHistoryOfSubscriber() {
        try {
            client.sendToServer(new Object[] { Operation.UPDATE_PARKING_HISTORY_OF_SUBSCRIBER, client.getSubscriber() });
        } catch (IOException e) {
            System.err.println("Failed to send 'updateParkingHistoryOfSubscriber' request: " + e.getMessage());
        }
    }

    /**
     * Requests the parking code again in case the subscriber forgot it.
     * @param subscriberCode the subscriber's ID
     */
    public void forgotMyParkingCode(int subscriberCode) {
        try {
            client.sendToServer(new Object[] { Operation.FORGEOT_MY_PARKING_CODE, subscriberCode });
        } catch (IOException e) {
            System.err.println("Failed to send 'forgotMyParkingCode' request: " + e.getMessage());
        }
    }

    /**
     * Requests all subscribers and how many times each was late.
     */
    public void requestAllSubscribers() {
        try {
            client.sendToServer(new Object[] { Operation.GET_ALL_SUBSCRIBERS });
        } catch (IOException e) {
            System.err.println("Failed to send 'get_all_subscribers' request: " + e.getMessage());
        }
    }

    /**
     * Requests all currently active parking sessions for staff view.
     */
    public void requestActiveParkingEvents() {
        try {
            client.sendToServer(new Object[] { Operation.GET_ACTIVE_PARKINGS });
        } catch (IOException e) {
            System.err.println("Failed to send 'requestActiveParkingEvents' request: " + e.getMessage());
        }
    }

    /**
     * Requests details about the subscriber's currently active parking session.
     */
    public void getDetailsOfActiveInfo() {
        try {
            client.sendToServer(new Object[] { Operation.GET_DETAILS_OF_ACTIVE_INFO, client.getSubscriber() });
        } catch (IOException e) {
            System.err.println("Failed to send 'getDetailsOfActiveInfo' request: " + e.getMessage());
        }
    }

    /**
     * Sends a request to extend a parking session.
     * @param parkingCode the active parking code
     * @param subscriberCode the subscriber's ID as a string
     */
    public void extendParking(int parkingCode, String subscriberCode) {
        try {
            client.sendToServer(new Object[] { Operation.EXTEND_PARKING, parkingCode, subscriberCode });
        } catch (IOException e) {
            System.err.println("Failed to send 'extendParking' request: " + e.getMessage());
        }
    }

    /**
     * Checks if the subscriber already has an order at the given date and time.
     * @param subscriberCode the subscriber's ID
     * @param date the date of the new reservation
     * @param time the time of the new reservation
     */
    public void checkIfOrderAlreadyExists(int subscriberCode, Date date, Time time) {
        try {
            client.sendToServer(new Object[] {
                    Operation.IS_THERE_AN_EXISTED_ORDER, subscriberCode, date, time
            });
        } catch (IOException e) {
            System.err.println("Failed to send 'IS_THERE_AN_EXISTED_ORDER' request: " + e.getMessage());
        }
    }

    /**
     * Registers a new subscriber and their vehicle.
     * @param subscriber the subscriber to register
     * @param vehicleId the ID of the vehicle being registered
     */
    public void registerSubscriber(Subscriber subscriber, String vehicleId) {
        try {
            client.sendToServer(new Object[] {
                    Operation.REGISTER_SUBSCRIBER, subscriber, vehicleId
            });
        } catch (IOException e) {
            System.err.println("Failed to send 'registerSubscriber' request: " + e.getMessage());
        }
    }

    /**
     * Requests current parking availability statistics.
     */
    public void requestParkingAvailability() {
        try {
            client.sendToServer(new Object[] { Operation.GET_PARKING_AVAILABILITY });
        } catch (IOException e) {
            System.err.println("Failed to send 'get_parking_availability' request: " + e.getMessage());
        }
    }

    /**
     * Requests a parking report (usage and revenue) for a specific day.
     * @param date the day for which to fetch the report
     */
    public void getParkingReport(Date date) {
        try {
            client.sendToServer(new Object[] { Operation.GET_PARKING_REPORT, date });
        } catch (IOException e) {
            System.err.println("Failed to send 'getParkingReport' request: " + e.getMessage());
        }
    }

    /**
     * Requests all available dates that have reports.
     */
    public void getDatesOfReports() {
        try {
            client.sendToServer(new Object[] { Operation.GET_DATES_OF_REPORTS });
        } catch (IOException e) {
            System.err.println("Failed to send 'getDatesOfReports' request: " + e.getMessage());
        }
    }

    /**
     * Asks for the subscriber status report for a specific month and year.
     * @param month the month of the report
     * @param year the year of the report
     */
    public void getSubscriberReport(Integer month, Integer year) {
        try {
            client.sendToServer(new Object[] {
                    Operation.GET_SUBSCRIBER_STATUS_REPORT, month, year
            });
        } catch (IOException e) {
            System.err.println("Failed to send 'getSubscriberReport' request: " + e.getMessage());
        }
    }

    /**
     * Checks if a subscriber exists based on their code.
     * @param subscriberCode the subscriber's ID
     */
    public void checkSubscriberExists(int subscriberCode) {
        try {
            client.sendToServer(new Object[] { Operation.SUBSCRIBER_EXISTS, subscriberCode });
        } catch (IOException e) {
            System.err.println("Failed to send 'subscriberExists' request: " + e.getMessage());
        }
    }

    /**
     * Checks if the tag exists in the system.
     * @param tagID the tag to validate
     */
    public void validateTag(String tagID) {
        try {
            client.sendToServer(new Object[] { Operation.TAG_EXISTS, tagID });
        } catch (IOException e) {
            System.err.println("Failed to send 'TAG_EXISTS' request: " + e.getMessage());
        }
    }

    /**
     * Checks if the subscriber has already entered the parking lot.
     * @param subscriberCode the subscriber's ID
     */
    public void checkIfSubscriberAlreadyEntered(int subscriberCode) {
        try {
            client.sendToServer(new Object[] {
                    Operation.SUBSCRIBER_ALREADY_ENTERED, subscriberCode
            });
        } catch (IOException e) {
            System.err.println("Failed to send 'subscriberAlreadyEntered' request: " + e.getMessage());
        }
    }

    /**
     * Checks if a vehicle with the given tag ID is already inside.
     * @param tagID the RFID tag of the vehicle
     */
    public void checkIfTagIDAlreadyInside(String tagID) {
        try {
            client.sendToServer(new Object[] {
                    Operation.TAG_ID_ALREADY_ENTERED, tagID
            });
        } catch (IOException e) {
            System.err.println("Failed to send 'tagIdAlreadyEntered' request: " + e.getMessage());
        }
    }

    /**
     * Finds the subscriber who owns the given tag.
     * @param tagID the tag to match with a subscriber
     */
    public void findSubscriberWithTag(String tagID) {
        try {
            client.sendToServer(new Object[] {
                    Operation.FIND_MATCHED_SUBSCRIBER_TO_THE_TAG, tagID
            });
        } catch (IOException e) {
            System.err.println("Failed to send 'findMatchedSubToTheTag' request: " + e.getMessage());
        }
    }

    /**
     * Starts the delivery process using a reservation.
     * @param subscriberCode the subscriber's ID
     * @param confirmationCode the reservation confirmation code
     */
    public void deliveryViaReservation(int subscriberCode, int confirmationCode) {
        try {
            client.sendToServer(new Object[] {
                    Operation.DELIVERY_VIA_RESERVATION, subscriberCode, confirmationCode
            });
        } catch (IOException e) {
            System.err.println("Failed to send 'DeliveryViaReservation' request: " + e.getMessage());
        }
    }

    /**
     * Checks if the specified parking lot has at least one free space.
     * @param parkingLotName the name of the parking lot
     */
    public void isThereFreeParkingSpace(String parkingLotName, int subscriberCode) {
        try {
            client.sendToServer(new Object[] {
                    Operation.IS_THERE_FREE_PARKING_SPACE, parkingLotName, subscriberCode,
            });
        } catch (IOException e) {
            System.err.println("Failed to send 'IsThereFreeParkingSpace' request: " + e.getMessage());
        }
    }

    /**
     * Requests the vehicle ID for the given subscriber.
     * @param subscriberCode the subscriber’s ID
     */
    public void seekVehicleID(int subscriberCode) {
        try {
            client.sendToServer(new Object[] {
                    Operation.GET_VEHICLE_ID, subscriberCode
            });
        } catch (IOException e) {
            System.err.println("Failed to send 'getVehicleID' request: " + e.getMessage());
        }
    }

    /**
     * Sends a request to log a new vehicle delivery at the parking entrance.
     * @param parkingEvent the parking event to register
     */
    public void deliverVehicle(ParkingEvent parkingEvent) {
        try {
            client.sendToServer(new Object[] {
                    Operation.DELIVER_VEHICLE, parkingEvent
            });
        } catch (IOException e) {
            System.err.println("Failed to send 'DeliverVehicle' request: " + e.getMessage());
        }
    }

    /**
     * Checks if the subscriber has a valid upcoming reservation.
     * @param subscriberCode the subscriber's ID
     */
    public void isThereReservation(int subscriberCode) {
        try {
            client.sendToServer(new Object[] {
                    Operation.CHECK_IF_THERE_IS_RERSERVATION, subscriberCode
            });
        } catch (IOException e) {
            System.err.println("Failed to send 'checkIfTheresReservation' request: " + e.getMessage());
        }
    }
    
    /**
     * Sends a disconnect request to the server, letting it know why the client is disconnecting.
     * This is used both when the user logs out or when the app exits completely.
     *
     * @param reason the reason for disconnecting 
     */
    public void sendDisconnect(String reason) {
        try {
            client.sendToServer(new Object[] { Operation.DISCONNECT, reason });
        } catch (IOException e) {
            System.err.println("[CLIENT] Failed to send disconnect: " + e.getMessage());
        }
    }



}
