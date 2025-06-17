package controllers;

import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;

import client.ClientController;
import common.Order;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * “My Reservations” table for a subscriber.  
 * Shows every future order and lets the user cancel one with a button.
 */
public class WatchAndCancelOrdersController implements ClientAware {

    /* ---------- headline label (“Watch & Cancel Orders”) ---------- */
    @FXML private Label headline;

    /* ---------- TableView + columns ---------- */
    @FXML private TableView<Order> reservationTable;
    @FXML private TableColumn<Order,Integer> orderNumberColumn;
    @FXML private TableColumn<Order,Date>    dateColumn;
    @FXML private TableColumn<Order,Time>    timeColumn;
    @FXML private TableColumn<Order,String>  confirmationCodeColumn;
    @FXML private TableColumn<Order,Void>    cancelOrderColumn;

    /* shared socket handler */
    private ClientController client;

    /**
     * Builds the table, then asks the server for the subscriber’s
     * current reservations.  Also adds a “Cancel” button to each row.
     * Call once after setClient().
     */
    public void defineTable() {

        orderNumberColumn   .setCellValueFactory(new PropertyValueFactory<>("orderNumber"));
        dateColumn          .setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        timeColumn          .setCellValueFactory(new PropertyValueFactory<>("arrivalTime"));
        confirmationCodeColumn.setCellValueFactory(new PropertyValueFactory<>("confirmationCode"));

        // pull the data
        client.askForReservations();

        // add a Cancel button per row
        cancelOrderColumn.setCellFactory(col -> new TableCell<>() {

            private final Button deleteBtn = new Button("Cancel");

            {
                deleteBtn.setOnAction(e -> {
                    Order order = getTableView().getItems().get(getIndex());
                    client.deleteOrder(order.getOrderNumber());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        });
    }

    /**
     * Populates the table after ClientController delivers the list.
     *
     * @param orders list of future orders
     */
    public void displayOrders(ArrayList<Order> orders) {
        ObservableList<Order> rows = FXCollections.observableArrayList(orders);
        reservationTable.setItems(rows);
    }

    /** Saves the ClientController reference. */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
    }
}
