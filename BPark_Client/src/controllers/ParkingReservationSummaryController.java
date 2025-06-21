package controllers;

import client.ClientController;
import common.Order;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controller for the reservation summary screen.
 * 
 * This screen is shown after a subscriber successfully completes
 * a parking reservation. It displays a confirmation message and
 * shows all reservation details pulled from the Order object.
 */
public class ParkingReservationSummaryController implements ClientAware {

    // =======================
    // Static caption labels
    // =======================

    /** Large headline at the top of the confirmation screen */
    @FXML private Label headline;

    /** Label caption for "Reservation Number" */
    @FXML private Label reservationNumber;

    /** Label caption for "Confirmation Code" */
    @FXML private Label confirmationCode;

    /** Label caption for "Subscriber Code" */
    @FXML private Label subscriberCode;

    /** Label caption for "Order Submission Date" */
    @FXML private Label orderSubmissionDate;

    /** Label caption for "Reservation Date" */
    @FXML private Label reservationDate;

    /** Label caption for "Reservation Time" */
    @FXML private Label reservationTime;

    /** Label caption for "Parking Space" */
    @FXML private Label parkingSpace;

    /** "Thank you" message shown after successful reservation */
    @FXML private Label thankU;

    /** Contact/support message shown below the summary */
    @FXML private Label support;

    // =======================
    // Dynamic value labels
    // =======================

    /** Displays the reservation number (auto-generated) */
    @FXML private Label reservationNumberOfOrder;

    /** Displays the confirmation code assigned to this reservation */
    @FXML private Label confirmationCodeOfOrder;

    /** Displays the subscriber's code (ID in the system) */
    @FXML private Label subscriberCodeOfOrder;

    /** Displays the date when the order was submitted */
    @FXML private Label orderSubmissionDateOfOrder;

    /** Displays the requested reservation date */
    @FXML private Label reservationDateOfOrder;

    /** Displays the requested reservation time */
    @FXML private Label reservationTimeOfOrder;

    /** Displays the assigned parking space number */
    @FXML private Label parkingSpaceOfOrder;

    // =======================
    // Runtime state
    // =======================

    /** Shared ClientController instance for server communication */
    private ClientController client;

    /** Holds the order just created and shown to the user */
    private Order order;

    // =====================================================
    // ClientAware interface
    // =====================================================

    /**
     * Stores the ClientController so this screen can communicate
     * with the server if needed.
     *
     * @param client the active ClientController instance
     */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
    }

    // =====================================================
    // Order state accessors
    // =====================================================

    /**
     * Returns the order currently displayed on this summary screen.
     *
     * @return the Order object being shown
     */
    public Order getOrder() {
        return order;
    }

    /**
     * Sets the internal order reference so that it can be shown later.
     * This method does not trigger UI updates.
     *
     * @param order the Order to store
     */
    public void setOrder(Order order) {
        this.order = order;
    }

    // =====================================================
    // UI binding
    // =====================================================

    /**
     * Populates all dynamic label fields with data
     * extracted from the provided Order object.
     * Should be called right after this screen is loaded.
     *
     * @param order the order object containing reservation details
     */
    public void setLabels(Order order) {
        reservationNumberOfOrder.setText(String.valueOf(order.getOrderNumber()));
        confirmationCodeOfOrder.setText(order.getConfirmationCode());
        subscriberCodeOfOrder.setText(String.valueOf(order.getSubscriberId()));
        orderSubmissionDateOfOrder.setText(order.getDateOfPlacingAnOrder().toString());
        reservationDateOfOrder.setText(order.getOrderDate().toString());
        reservationTimeOfOrder.setText(order.getArrivalTime().toString());
        parkingSpaceOfOrder.setText(String.valueOf(order.getParkingSpace()));
    }
}

