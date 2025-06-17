package controllers;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import client.ClientController;
import common.ParkingEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Shows the subscriber’s parking history—every past event
 * plus the current “Active” event (if one exists).
 */
public class ViewParkingHistoryController implements ClientAware {

    /* headline label (“Parking History”) */
    @FXML private Label headline;

    /* ---------- table + columns ---------- */
    @FXML private TableView<ParkingEvent> parkingHistoryTable;
    @FXML private TableColumn<ParkingEvent,Integer> eventNumberColumn;
    @FXML private TableColumn<ParkingEvent,LocalDate> entryDateColumn;
    @FXML private TableColumn<ParkingEvent,LocalTime> entryHourColumn;
    @FXML private TableColumn<ParkingEvent,LocalDate> exitDateColumn;
    @FXML private TableColumn<ParkingEvent,LocalTime> exitHourColumn;
    @FXML private TableColumn<ParkingEvent,String>  status;

    /* shared socket handler */
    private ClientController client;


    /** Saves the ClientController for later server calls. */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
    }

    // --------------------------------------------------
    // table setup
    // --------------------------------------------------

    /**
     * Builds column bindings and cell-formatters, then asks the
     * server for the subscriber’s parking history.
     * Call once after setClient().
     */
    public void setTable() {

        eventNumberColumn.setCellValueFactory(new PropertyValueFactory<>("eventId"));
        entryDateColumn  .setCellValueFactory(new PropertyValueFactory<>("entryDate"));
        entryHourColumn  .setCellValueFactory(new PropertyValueFactory<>("entryHour"));
        exitDateColumn   .setCellValueFactory(new PropertyValueFactory<>("exitDate"));
        exitHourColumn   .setCellValueFactory(new PropertyValueFactory<>("exitHour"));
        status           .setCellValueFactory(new PropertyValueFactory<>("status"));

        // format entry time – HH:mm:ss
        entryHourColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(LocalTime t, boolean empty) {
                super.updateItem(t, empty);
                if (!empty && t != null)
                    setText(t.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            }
        });

        // if exit date is null → “Active”
        exitDateColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(LocalDate d, boolean empty) {
                super.updateItem(d, empty);
                if (empty) return;
                setText(d == null ? "Active"
                                  : d.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            }
        });

        // same logic for exit time
        exitHourColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(LocalTime t, boolean empty) {
                super.updateItem(t, empty);
                if (empty) return;
                setText(t == null ? "Active"
                                  : t.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            }
        });

        // request the data
        client.updateParkingHistoryOfSubscriber();
    }

    // --------------------------------------------------
    // server callback
    // --------------------------------------------------

    /**
     * Fills the table after ClientController delivers the list.
     *
     * @param parkingEventsHistory list returned from the server
     */
    public void displayHistory(ArrayList<ParkingEvent> parkingEventsHistory) {
        ObservableList<ParkingEvent> rows =
                FXCollections.observableArrayList(parkingEventsHistory);
        parkingHistoryTable.setItems(rows);
    }
}
