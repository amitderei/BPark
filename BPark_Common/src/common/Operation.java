package common;

/**
 * Represents all the types of operations (requests) that the client can send to the server.
 */
public enum Operation {

    /** Get all available report dates for the parking reports screen */
    GET_DATES_OF_REPORTS,

    /** Load a specific parking report for a given date */
    GET_PARKING_REPORT,

    /** Ask the server how many spots are free/occupied in total */
    GET_PARKING_AVAILABILITY,

    /** Register a new subscriber into the system */
    REGISTER_SUBSCRIBER,

    /** Ask to extend the parking session of a subscriber */
    EXTEND_PARKING,

    /** Get info about the currently active parking event of a subscriber */
    GET_DETAILS_OF_ACTIVE_INFO,

    /** Get a list of all vehicles currently parked */
    GET_ACTIVE_PARKINGS,

    /** Get all subscribers along with how many times they were late picking up */
    GET_ALL_SUBSCRIBERS,

    /** Used when a subscriber clicks "Forgot parking code" - sends email again */
    FORGEOT_MY_PARKING_CODE,

    /** Request to load the full parking history of a subscriber */
    UPDATE_PARKING_HISTORY_OF_SUBSCRIBER,

    /** Ask the server to delete a specific order by order number */
    DELETE_ORDER,

    /** Get all current/future reservations of a subscriber */
    ASK_FOR_RESERVATIONS,

    /** Request full details of a subscriber (based on logged in user) */
    SUBSCRIBER_DETAILS,

    /** Request to collect a car from the lot by providing subscriber + code */
    COLLECT_CAR,

    /** Validate subscriber using tag ID */
    VALIDATE_SUBSCRIBER_BY_TAG,

    /** Validate subscriber using numeric code */
    VALIDATE_SUBSCRIBER_BY_SUBSCRIBER_CODE,

    /** Login request - check if username + password are correct */
    LOGIN,

    /** Ask if it's possible to make a reservation at a specific time */
    CHECK_AVAILABILITY_FOR_ORDER,

    /** Add a new reservation to the system */
    ADD_NEW_ORDER,

    /** Disconnect request - called when client logs out or closes app */
    DISCONNECT,

    /** Ask for the subscriber status report of a certain month/year */
    GET_SUBSCRIBER_STATUS_REPORT,

    /** Check if a subscriber already has an order for a specific date/time */
    IS_THERE_AN_EXISTED_ORDER,

    /** Check if a subscriber code exists in the system */
    SUBSCRIBER_EXISTS,

    /** Check if a tag exists (used before entry by tag) */
    TAG_EXISTS,

    /** Check if a subscriber already entered the lot (used to avoid duplicate entry) */
    SUBSCRIBER_ALREADY_ENTERED,

    /** Check if a vehicle with a given tag is already inside */
    TAG_ID_ALREADY_ENTERED,

    /** Find which subscriber is matched to a specific tag */
    FIND_MATCHED_SUBSCRIBER_TO_THE_TAG,

    /** Verify if a reservation exists and confirmation code is valid */
    DELIVERY_VIA_RESERVATION,

    /** Check if there's any available parking space in a specific lot */
    IS_THERE_FREE_PARKING_SPACE,

    /** Ask for the vehicle ID of a subscriber */
    GET_VEHICLE_ID,

    /** Add a new vehicle entry (parking event) into the lot */
    DELIVER_VEHICLE,

    /** Check if a subscriber has a reservation right now */
    CHECK_IF_THERE_IS_RERSERVATION,

    /** Update subscriber or user account details */
    UPDATE_DETAILS_OF_SUBSCRIBER
}

