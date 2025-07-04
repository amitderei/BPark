package common;

/**
 * Represents all possible types of responses the server can send to the client.
 * Each type is used to indicate the result or purpose of a specific server operation.
 */
public enum ResponseType {

    /** Login attempt failed (wrong username or password) */
    LOGIN_FAILED,

    /** Login was successful, user is authenticated */
    LOGIN_SUCCESSFULL,

    /** No orders found for this subscriber */
    NO_ORDERS,

    /** A list of reservations was returned */
    ORDERS_DISPLAY,

    /** A reservation was successfully deleted */
    ORDER_DELETED,

    /** A parking report was returned */
    PARKING_REPORT_LOADED,

    /** Parking history data was successfully loaded */
    PARKING_HISTORY_LOADED,

    /** Subscriber or user details were updated in the system */
    DETAILS_UPDATED,

    /** Subscriber was verified successfully (by tag or code) */
    SUBSCRIBER_VERIFIED,

    /** A list of available monthly reports was received */
    REPOSTS_DATE_LOADED,

    /** A new reservation was added */
    ORDER_ADDED,

    /** No existing reservation found for given details */
    ORDER_NOT_EXISTS,

    /** A reservation already exists for the same time */
    ORDER_ALREADY_EXISTS,

    /** Returned list of all subscribers with how many times they were late */
    LATE_PICKUP_COUNTS,

    /** Response after a vehicle pickup attempt */
    PICKUP_VEHICLE,

    /** List of currently active parking events */
    ACTIVE_PARKINGS,

    /** Active parking info of a subscriber was loaded */
    PARKING_INFO_LOADED,

    /** A parking session was successfully extended */
    PARKING_SESSION_EXTENDED,

    /** Subscriber was registered successfully or failed */
    SUBSCRIBER_INSERTED,

    /** Parking availability statistics were received */
    PARKING_AVALIABILITY,

    /** There is availability to make a reservation */
    RESERVATION_VALID,

    /** Cannot make a reservation (no space available) */
    RESERVATION_INVALID,

    /** Subscriber status report was returned */
    SUBSCRIBER_STATUS,

    /** Used to verify that a subscriber code exists */
    SUBSCRIBER_CODE,

    /** Subscriber details were retrieved from DB */
    SUBSCRIBER_DETAILS,

    /** The subscriber currently has a reservation */
    RESERVATION_EXISTS,

    /** No current reservation found for the subscriber */
    RESERVATION_NOT_EXISTS,

    /** Confirmation code was validated (success or fail) */
    CONFIRMATION_CODE_VALIDATION,

    /** Whether there's a parking spot available in the lot */
    PARKING_SPACE_AVAILABILITY,

    /** The vehicle ID associated with a subscriber */
    VEHICLE_ID,

    /** A vehicle was delivered and saved in the system */
    DELIVER_VEHICLE,

    /** Indicates if a tag ID exists in the database */
    TAG_EXISTS,

    /** Subscriber code matched to a specific tag */
    MATCHED_SUBSCRIBER_TO_TAG,

    /** Subscriber's vehicle is not inside the lot (checked by code) */
    SUBSCRIBER_VEHICLE_ISNT_INSIDE,

    /** Subscriber's vehicle is not inside (checked by tag ID) */
    SUBSCRIBER_VEHICLE_ISNT_INSIDE_BY_TAG
}

