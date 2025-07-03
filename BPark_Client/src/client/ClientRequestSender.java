package client;

import common.*;
import java.io.IOException;
import java.sql.Date;
import java.sql.Time;

/**
 * Handles all outgoing requests from the client to the server.
 * This class doesn't handle any response logic – it just sends.
 * Think of it as the "transmitter" for the client-side app.
 *
 * You can call these methods from any controller like:
 * 
 *     client.getRequestSender().requestLogin(...);
 * 
 *
 * @author BPARK
 */
public class ClientRequestSender {

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

    /** Sends a login attempt to the server */
    public void requestLogin(String username, String password) {
        try {
            client.sendToServer(new Object[] { Operation.LOGIN, username, password });
        } catch (IOException e) {
            System.err.println("Failed to send login request: " + e.getMessage());
        }
    }

    /** Adds a new reservation/order to the DB */
    public void addNewOrder(Order newOrder) {
        try {
            client.sendToServer(new Object[] { Operation.ADD_NEW_ORDER, newOrder });
        } catch (IOException e) {
            System.err.println("Failed to send 'addNewOrder' request to server: " + e.getMessage());
        }
    }

    /** Checks if a parking lot has space on a given date/time */
    public void checkAvailability(Date date, Time time) {
        try {
            client.sendToServer(new Object[] { Operation.CHECK_AVAILABILITY_FOR_ORDER, date, time });
        } catch (IOException e) {
            System.err.println("Failed to send 'checkAvailability' request: " + e.getMessage());
        }
    }

    /** Confirms a subscriber is valid using their code */
    public void validateSubscriber(int subscriberCode) {
        try {
            client.sendToServer(new Object[] { Operation.VALIDATE_SUBSCRIBER_BY_SUBSCRIBER_CODE, subscriberCode });
        } catch (IOException e) {
            System.err.println("Failed to send 'validateSubscriber' request: " + e.getMessage());
        }
    }

    /** Confirms a subscriber is valid using their RFID tag */
    public void validateSubscriberByTag(String tagId) {
        try {
            client.sendToServer(new Object[] { Operation.VALIDATE_SUBSCRIBER_BY_TAG, tagId });
        } catch (IOException e) {
            System.err.println("Failed to send 'validateSubscriberByTag' request: " + e.getMessage());
        }
    }

    /** Sends pickup request (subscriber wants to collect car) */
    public void collectCar(int subscriberCode, int parkingCode) {
        try {
            client.sendToServer(new Object[] { Operation.COLLECT_CAR, subscriberCode, parkingCode });
        } catch (IOException e) {
            System.err.println("Failed to send 'collectCar' request: " + e.getMessage());
        }
    }

    /** Gets full subscriber details from server */
    public void subscriberDetails(User user) {
        try {
            client.sendToServer(new Object[] { Operation.SUBSCRIBER_DETAILS, user });
        } catch (IOException e) {
            System.err.println("Failed to send 'subscriberDetails' request: " + e.getMessage());
        }
    }

    /** Requests all reservations for the currently logged-in subscriber */
    public void askForReservations() {
        try {
            client.sendToServer(new Object[] { Operation.ASK_FOR_RESERVATIONS, client.getSubscriber() });
        } catch (IOException e) {
            System.err.println("Failed to send 'askForReservations' request: " + e.getMessage());
        }
    }

    /** Deletes a specific order (by its primary key) */
    public void deleteOrder(int orderNumberToDelete) {
        try {
            client.sendToServer(new Object[] { Operation.DELETE_ORDER, orderNumberToDelete });
        } catch (IOException e) {
            System.err.println("Failed to send 'deleteOrder' request: " + e.getMessage());
        }
    }

    /** Updates both subscriber and user personal info */
    public void updateDetailsOfSubscriber(Subscriber subscriber, User user) {
        try {
            client.sendToServer(new Object[] { Operation.UPDATE_DETAILS_OF_SUBSCRIBER, subscriber, user });
        } catch (IOException e) {
            System.err.println("Failed to send 'updateDetailsOfSubscriber' request: " + e.getMessage());
        }
    }

    /** Refreshes parking history from server */
    public void updateParkingHistoryOfSubscriber() {
        try {
            client.sendToServer(new Object[] { Operation.UPDATE_PARKING_HISTORY_OF_SUBSCRIBER, client.getSubscriber() });
        } catch (IOException e) {
            System.err.println("Failed to send 'updateParkingHistoryOfSubscriber' request: " + e.getMessage());
        }
    }

    /** Forgot code flow – request parking code recovery */
    public void forgotMyParkingCode(int subscriberCode) {
        try {
            client.sendToServer(new Object[] { Operation.FORGEOT_MY_PARKING_CODE, subscriberCode });
        } catch (IOException e) {
            System.err.println("Failed to send 'forgotMyParkingCode' request: " + e.getMessage());
        }
    }

    /** Gets all subscribers and how many times they were late */
    public void requestAllSubscribers() {
        try {
            client.sendToServer(new Object[] { Operation.GET_ALL_SUBSCRIBERS });
        } catch (IOException e) {
            System.err.println("Failed to send 'get_all_subscribers' request: " + e.getMessage());
        }
    }

    /** Gets all currently active parking events (for staff view) */
    public void requestActiveParkingEvents() {
        try {
            client.sendToServer(new Object[] { Operation.GET_ACTIVE_PARKINGS });
        } catch (IOException e) {
            System.err.println("Failed to send 'requestActiveParkingEvents' request: " + e.getMessage());
        }
    }

    /** Gets details of current active parking event for this subscriber */
    public void getDetailsOfActiveInfo() {
        try {
            client.sendToServer(new Object[] { Operation.GET_DETAILS_OF_ACTIVE_INFO, client.getSubscriber() });
        } catch (IOException e) {
            System.err.println("Failed to send 'getDetailsOfActiveInfo' request: " + e.getMessage());
        }
    }

    /** Sends a request to extend the current parking session */
    public void extendParking(int parkingCode, String subscriberCode) {
        try {
            client.sendToServer(new Object[] { Operation.EXTEND_PARKING, parkingCode, subscriberCode });
        } catch (IOException e) {
            System.err.println("Failed to send 'extendParking' request: " + e.getMessage());
        }
    }

    /** Checks if an order already exists for a date/time */
    public void checkIfOrderAlreadyExists(int subscriberCode, Date date, Time time) {
        try {
            client.sendToServer(new Object[] {
                    Operation.IS_THERE_AN_EXISTED_ORDER, subscriberCode, date, time
            });
        } catch (IOException e) {
            System.err.println("Failed to send 'IS_THERE_AN_EXISTED_ORDER' request: " + e.getMessage());
        }
    }

    /** Registers a new subscriber with a vehicle */
    public void registerSubscriber(Subscriber subscriber, String vehicleId) {
        try {
            client.sendToServer(new Object[] {
                    Operation.REGISTER_SUBSCRIBER, subscriber, vehicleId
            });
        } catch (IOException e) {
            System.err.println("Failed to send 'registerSubscriber' request: " + e.getMessage());
        }
    }

    /** Gets a live snapshot of parking availability stats */
    public void requestParkingAvailability() {
        try {
            client.sendToServer(new Object[] { Operation.GET_PARKING_AVAILABILITY });
        } catch (IOException e) {
            System.err.println("Failed to send 'get_parking_availability' request: " + e.getMessage());
        }
    }

    /** Requests parking report (usage + revenue) for a specific day */
    public void getParkingReport(Date date) {
        try {
            client.sendToServer(new Object[] { Operation.GET_PARKING_REPORT, date });
        } catch (IOException e) {
            System.err.println("Failed to send 'getParkingReport' request: " + e.getMessage());
        }
    }

    /** Gets list of all dates that have existing parking reports */
    public void getDatesOfReports() {
        try {
            client.sendToServer(new Object[] { Operation.GET_DATES_OF_REPORTS });
        } catch (IOException e) {
            System.err.println("Failed to send 'getDatesOfReports' request: " + e.getMessage());
        }
    }

    /** Fetches a monthly subscriber status report */
    public void getSubscriberReport(Integer month, Integer year) {
        try {
            client.sendToServer(new Object[] {
                    Operation.GET_SUBSCRIBER_STATUS_REPORT, month, year
            });
        } catch (IOException e) {
            System.err.println("Failed to send 'getSubscriberReport' request: " + e.getMessage());
        }
    }

    /** Checks if subscriber exists (by code) */
    public void checkSubscriberExists(int subscriberCode) {
        try {
            client.sendToServer(new Object[] { Operation.SUBSCRIBER_EXISTS, subscriberCode });
        } catch (IOException e) {
            System.err.println("Failed to send 'subscriberExists' request: " + e.getMessage());
        }
    }

    /** Checks if a given tag ID exists in DB */
    public void validateTag(String tagID) {
        try {
            client.sendToServer(new Object[] { Operation.TAG_EXISTS, tagID });
        } catch (IOException e) {
            System.err.println("Failed to send 'TAG_EXISTS' request: " + e.getMessage());
        }
    }

    /** Checks if subscriber already entered the lot */
    public void checkIfSubscriberAlreadyEntered(int subscriberCode) {
        try {
            client.sendToServer(new Object[] {
                    Operation.SUBSCRIBER_ALREADY_ENTERED, subscriberCode
            });
        } catch (IOException e) {
            System.err.println("Failed to send 'subscriberAlreadyEntered' request: " + e.getMessage());
        }
    }

    /** Checks if a vehicle (by tag) is already inside */
    public void checkIfTagIDAlreadyInside(String tagID) {
        try {
            client.sendToServer(new Object[] {
                    Operation.TAG_ID_ALREADY_ENTERED, tagID
            });
        } catch (IOException e) {
            System.err.println("Failed to send 'tagIdAlreadyEntered' request: " + e.getMessage());
        }
    }

    /** Finds the subscriber who owns a given tag */
    public void findSubscriberWithTag(String tagID) {
        try {
            client.sendToServer(new Object[] {
                    Operation.FIND_MATCHED_SUBSCRIBER_TO_THE_TAG, tagID
            });
        } catch (IOException e) {
            System.err.println("Failed to send 'findMatchedSubToTheTag' request: " + e.getMessage());
        }
    }

    /** Sends a request to process delivery via reservation */
    public void deliveryViaReservation(int subscriberCode, int confirmationCode) {
        try {
            client.sendToServer(new Object[] {
                    Operation.DELIVERY_VIA_RESERVATION, subscriberCode, confirmationCode
            });
        } catch (IOException e) {
            System.err.println("Failed to send 'DeliveryViaReservation' request: " + e.getMessage());
        }
    }

    /** Checks if a parking lot has space */
    public void isThereFreeParkingSpace(String parkingLotName) {
        try {
            client.sendToServer(new Object[] {
                    Operation.IS_THERE_FREE_PARKING_SPACE, parkingLotName
            });
        } catch (IOException e) {
            System.err.println("Failed to send 'IsThereFreeParkingSpace' request: " + e.getMessage());
        }
    }

    /** Gets vehicle ID matched to subscriber */
    public void seekVehicleID(int subscriberCode) {
        try {
            client.sendToServer(new Object[] {
                    Operation.GET_VEHICLE_ID, subscriberCode
            });
        } catch (IOException e) {
            System.err.println("Failed to send 'getVehicleID' request: " + e.getMessage());
        }
    }

    /** Registers a new vehicle delivery at the gate */
    public void deliverVehicle(ParkingEvent parkingEvent) {
        try {
            client.sendToServer(new Object[] {
                    Operation.DELIVER_VEHICLE, parkingEvent
            });
        } catch (IOException e) {
            System.err.println("Failed to send 'DeliverVehicle' request: " + e.getMessage());
        }
    }

    /** Checks if subscriber has a valid upcoming reservation */
    public void isThereReservation(int subscriberCode) {
        try {
            client.sendToServer(new Object[] {
                    Operation.CHECK_IF_THERE_IS_RERSERVATION, subscriberCode
            });
        } catch (IOException e) {
            System.err.println("Failed to send 'checkIfTheresReservation' request: " + e.getMessage());
        }
    }
}
