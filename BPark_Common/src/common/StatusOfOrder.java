package common;

/**
 * Represents the current status of an order made by a subscriber.
 * The status helps track if the order is still valid, was used, expired, or canceled.
 */
public enum StatusOfOrder {
    
    /** 
     * The order was used successfully - the subscriber entered the parking lot 
     * at the scheduled date and time. 
     */
    FULFILLED,

    /** 
     * The order is still active - its scheduled time hasn’t passed yet. 
     */
    ACTIVE,

    /** 
     * The order is no longer valid - the scheduled time has already passed 
     * and it wasn’t used. 
     */
    INACTIVE,

    /** 
     * The order was canceled by the subscriber before it was used. 
     */
    CANCELLED
}
