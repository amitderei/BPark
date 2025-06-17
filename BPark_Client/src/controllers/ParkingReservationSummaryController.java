package controllers;

import client.ClientController;
import common.Order;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Shows a “Thank you” page after a reservation is confirmed.
 * Pulls data from the Order object and fills the summary labels so
 * the subscriber can screenshot or print the confirmation.
 */
public class ParkingReservationSummaryController implements ClientAware {

    /* ---------- FXML labels (static captions) ---------- */
    @FXML private Label headline;
    @FXML private Label reservationNumber;
    @FXML private Label confirmationCode;
    @FXML private Label subscriberCode;
    @FXML private Label orderSubmissionDate;
    @FXML private Label reservationDate;
    @FXML private Label reservationTime;
    @FXML private Label parkingSpace;
    @FXML private Label thankU;
    @FXML private Label support;

    /* ---------- FXML labels that hold dynamic values ---------- */
    @FXML private Label reservationNumberOfOrder;
    @FXML private Label confirmationCodeOfOrder;
    @FXML private Label subscriberCodeOfOrder;
    @FXML private Label orderSubmissionDateOfOrder;
    @FXML private Label reservationDateOfOrder;
    @FXML private Label reservationTimeOfOrder;
    @FXML private Label parkingSpaceOfOrder;

    /* ---------- runtime ---------- */
    private ClientController client;
    private Order order;   // order just placed

    /* =====================================================
     *  ClientAware
     * ===================================================== */

    /**
     * Stores the shared ClientController reference.
     *
     * @param client active client instance
     */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
    }

    /* =====================================================
     *  Working state getters / setters
     * ===================================================== */

    /** @return the order currently displayed */
    public Order getOrder() {
        return order;
    }

    /**
     * Caches the order so the view can show it.
     *
     * @param order newly created order
     */
    public void setOrder(Order order) {
        this.order = order;
    }

    /* =====================================================
     *  UI population
     * ===================================================== */

    /**
     * Fills all “value” labels with data from the given Order.
     * Call this immediately after navigating to the summary screen.
     *
     * @param order order to display
     */
    public void setLabels(Order order) {

        reservationNumberOfOrder .setText(String.valueOf(order.getOrderNumber()));
        confirmationCodeOfOrder  .setText(order.getConfirmationCode());
        subscriberCodeOfOrder    .setText(String.valueOf(order.getSubscriberId()));
        orderSubmissionDateOfOrder.setText(order.getDateOfPlacingAnOrder().toString());
        reservationDateOfOrder   .setText(order.getOrderDate().toString());
        reservationTimeOfOrder   .setText(order.getArrivalTime().toString());
        parkingSpaceOfOrder      .setText(String.valueOf(order.getParkingSpace()));
    }
}
