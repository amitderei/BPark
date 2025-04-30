package common;

import java.io.Serializable;
import java.sql.Date;

/**
 * Represents a single parking order from the 'Order' table.
 */
public class Order implements Serializable {
    private int orderNumber;
    private int parkingSpace;
    private Date orderDate;
    private int confirmationCode;
    private int subscriberId;
    private Date dateOfPlacingAnOrder;

    public Order(int orderNumber, int parkingSpace, Date orderDate,
                 int confirmationCode, int subscriberId, Date dateOfPlacingAnOrder) {
        this.orderNumber = orderNumber;
        this.parkingSpace = parkingSpace;
        this.orderDate = orderDate;
        this.confirmationCode = confirmationCode;
        this.subscriberId = subscriberId;
        this.dateOfPlacingAnOrder = dateOfPlacingAnOrder;
    }

    // Getters
    public int getOrderNumber() { return orderNumber; }
    public int getParkingSpace() { return parkingSpace; }
    public Date getOrderDate() { return orderDate; }
    public int getConfirmationCode() { return confirmationCode; }
    public int getSubscriberId() { return subscriberId; }
    public Date getDateOfPlacingAnOrder() { return dateOfPlacingAnOrder; }

    // Setters
    public void setParkingSpace(int parkingSpace) { this.parkingSpace = parkingSpace; }
    public void setOrderDate(Date orderDate) { this.orderDate = orderDate; }

    @Override
    public String toString() {
        return "Order #" + orderNumber + ", Parking: " + parkingSpace + ", Date: " + orderDate;
    }
}
