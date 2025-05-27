package common;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Time;

/**
 * Represents a parking order record from the 'Order' table in the database.
 * This class is used to encapsulate order data for client-server communication.
 * Implements Serializable to support object transmission over network (OCSF).
 */
public class Order implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int orderNumber;
	public void setSubscriberId(int subscriberId) {
		this.subscriberId = subscriberId;
	}

	public Time getArrivalTime() {
		return arrivalTime;
	}

	private int parkingSpace;
	private Date orderDate;
	private String confirmationCode;
	private int subscriberId;
	private Date dateOfPlacingAnOrder;
	private Time arrivalTime;

	/**
	 * Constructs an Order object with all required fields.
	 *
	 * @param orderNumber          Unique identifier for the order (primary key).
	 * @param parkingSpace         Assigned parking space number.
	 * @param orderDate            The date the order was created.
	 * @param confirmationCode     Internal confirmation code for validation.
	 * @param subscriberId         The ID of the subscriber who placed the order.
	 * @param dateOfPlacingAnOrder Actual date the order was placed.
	 */
	public Order(int orderNumber, int parkingSpace, Date orderDate,Time arrivalTime, String confirmationCode, int subscriberId,
            Date dateOfPlacingAnOrder) {
		this.orderNumber = orderNumber;
		this.parkingSpace = parkingSpace;
		this.orderDate = orderDate;
		this.confirmationCode = confirmationCode;
		this.subscriberId = subscriberId;
		this.dateOfPlacingAnOrder = dateOfPlacingAnOrder;
		this.arrivalTime=arrivalTime;
	}

	// Getters

	/**
	 * @return the unique order number.
	 */
	public int getOrderNumber() {
		return orderNumber;
	}

	/**
	 * @return the parking space number.
	 */
	public int getParkingSpace() {
		return parkingSpace;
	}

	/**
	 * @return the order creation date.
	 */
	public Date getOrderDate() {
		return orderDate;
	}

	/**
	 * @return the confirmation code associated with the order.
	 */
	public String getConfirmationCode() {
		return confirmationCode;
	}

	/**
	 * @return the ID of the subscriber who placed the order.
	 */
	public int getSubscriberId() {
		return subscriberId;
	}

	/**
	 * @return the date the order was actually placed.
	 */
	public Date getDateOfPlacingAnOrder() {
		return dateOfPlacingAnOrder;
	}

	// Setters

	/**
	 * Sets a new parking space for this order.
	 *
	 * @param parkingSpace the new parking space number.
	 */
	public void setParkingSpace(int parkingSpace) {
		this.parkingSpace = parkingSpace;
	}

	/**
	 * Sets a new order date.
	 *
	 * @param orderDate the new date of the order.
	 */
	public void setOrderDate(Date orderDate) {
		this.orderDate = orderDate;
	}

	/**
	 * Returns a formatted string representation of the order. Useful for console
	 * printing or debugging.
	 */
	@Override
	public String toString() {
		return "Order #" + orderNumber + ", Parking: " + parkingSpace + ", Order Date: " + orderDate
				+ ", Confirmation Code: " + confirmationCode + ", Subscriber ID: " + subscriberId + ", Placing Date: "
				+ dateOfPlacingAnOrder;
	}
}
