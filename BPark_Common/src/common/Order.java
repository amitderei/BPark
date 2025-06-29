package common;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Time;

/**
 * A model class that represents a single parking order,
 * as stored in the 'Order' table in the database.
 * This object is passed between client and server to carry all order-related
 * information. It implements Serializable so it can be sent over the network.
 */
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Unique ID of the order (primary key) */
    private int orderNumber;

    /** Number of the parking space reserved in this order */
    private int parkingSpace;

    /** Date on which the order is scheduled */
    private Date orderDate;

    /** Confirmation code used to validate the order */
    private String confirmationCode;

    /** ID of the subscriber who placed the order */
    private int subscriberId;

    /** Date when the order was actually placed in the system */
    private Date dateOfPlacingAnOrder;

    /** Time when the vehicle is expected to arrive */
    private Time arrivalTime;
    
    /** 
     * the status of order: active, in active...
     */
    private StatusOfOrder status;

    /**
     * Constructs an Order object with all relevant fields.
     *
     * @param orderNumber          Unique ID of the order
     * @param parkingSpace         Number of the reserved parking space
     * @param orderDate            Date on which the order is scheduled
     * @param arrivalTime          Expected arrival time of the vehicle
     * @param confirmationCode     Code used to confirm the order
     * @param subscriberId         ID of the subscriber who placed the order
     * @param dateOfPlacingAnOrder Date when the order was submitted
     * @param status the status of order
     **/
    public Order(int orderNumber, int parkingSpace, Date orderDate, Time arrivalTime, String confirmationCode,
                 int subscriberId, Date dateOfPlacingAnOrder, StatusOfOrder status) {
        this.orderNumber = orderNumber;
        this.parkingSpace = parkingSpace;
        this.orderDate = orderDate;
        this.arrivalTime = arrivalTime;
        this.confirmationCode = confirmationCode;
        this.subscriberId = subscriberId;
        this.dateOfPlacingAnOrder = dateOfPlacingAnOrder;
        this.status=status;
    }

    /**
     * Returns the unique order number.
     *
     * @return order number
     */
    public int getOrderNumber() {
        return orderNumber;
    }

    /**
     * Returns the assigned parking space number.
     *
     * @return parking space number
     */
    public int getParkingSpace() {
        return parkingSpace;
    }

    /**
     * Returns the date the order is scheduled for.
     *
     * @return order date
     */
    public Date getOrderDate() {
        return orderDate;
    }

    /**
     * Returns the confirmation code for this order.
     *
     * @return confirmation code
     */
    public String getConfirmationCode() {
        return confirmationCode;
    }

    /**
     * Returns the subscriber ID who made the order.
     *
     * @return subscriber ID
     */
    public int getSubscriberId() {
        return subscriberId;
    }

    /**
     * Returns the actual date the order was placed.
     *
     * @return date of placing the order
     */
    public Date getDateOfPlacingAnOrder() {
        return dateOfPlacingAnOrder;
    }

    /**
     * Returns the expected arrival time for this order.
     *
     * @return arrival time
     */
    public Time getArrivalTime() {
        return arrivalTime;
    }

    /**
     * Sets the parking space for this order.
     *
     * @param parkingSpace new parking space number
     */
    public void setParkingSpace(int parkingSpace) {
        this.parkingSpace = parkingSpace;
    }

    /**
     * Sets the order number (ID). Use with caution – typically set only once.
     *
     * @param orderNumber new order number
     */
    public void setOrderNumber(int orderNumber) {
        this.orderNumber = orderNumber;
    }

    /**
     * Sets the date the order is scheduled for.
     *
     * @param orderDate new order date
     */
    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    /**
     * Sets the subscriber ID that this order belongs to.
     *
     * @param subscriberId new subscriber ID
     */
    public void setSubscriberId(int subscriberId) {
        this.subscriberId = subscriberId;
    }

    /**
     * Returns a string representation of the order.
     * Mainly used for logging or debugging.
     *
     * @return formatted string with order details
     */
    @Override
    public String toString() {
        return "Order #" + orderNumber +
                ", Parking: " + parkingSpace +
                ", Order Date: " + orderDate +
                ", Confirmation Code: " + confirmationCode +
                ", Subscriber ID: " + subscriberId +
                ", Placing Date: " + dateOfPlacingAnOrder;
    }
}
