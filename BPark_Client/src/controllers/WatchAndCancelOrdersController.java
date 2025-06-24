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
 * Controller for the "My Reservations" screen.
 * Displays a table of upcoming reservations for the subscriber,
 * and allows cancellation of individual orders.
 */
public class WatchAndCancelOrdersController implements ClientAware {
    /** Header label at the top of the screen */
    @FXML private Label headline;

    /** Table view to display all upcoming orders */
    @FXML private TableView<Order> reservationTable;

    /** Column: Order number (ID) */
    @FXML private TableColumn<Order, Integer> orderNumberColumn;

    /** Column: Date of the reservation */
    @FXML private TableColumn<Order, Date> dateColumn;

    /** Column: Time of arrival */
    @FXML private TableColumn<Order, Time> timeColumn;

    /** Column: Confirmation code assigned to the order */
    @FXML private TableColumn<Order, String> confirmationCodeColumn;

    /** Column: Cancel button per row */
    @FXML private TableColumn<Order, Void> cancelOrderColumn;

    /** Client controller used to communicate with the server */
    private ClientController client;

    /**
     * Called once after setClient().
     * Initializes the table columns, requests the subscriber's orders from the server,
     * and adds a "Cancel" button to each row.
     */
    public void defineTable() {
        // Link each column to the matching Order field
        orderNumberColumn.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("arrivalTime"));
        confirmationCodeColumn.setCellValueFactory(new PropertyValueFactory<>("confirmationCode"));

        // Ask the server to send the subscriber's future orders
        client.askForReservations();

        // Add a "Cancel" button in each row
        cancelOrderColumn.setCellFactory(col -> new TableCell<>() {
            private final Button deleteBtn = new Button("Cancel");

            {
                // When clicked, delete the order by its ID
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
     * Called by ClientController when the server responds with the list of orders.
     * Fills the table with the received list.
     *
     * @param orders a list of Order objects to display
     */
    public void displayOrders(ArrayList<Order> orders) {
        ObservableList<Order> rows = FXCollections.observableArrayList(orders);
        reservationTable.setItems(rows);
    }

    /**
     * Stores the ClientController reference for communication with the server.
     *
     * @param client the active ClientController
     */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
    }
}
