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
 * Controller for the screen that displays a subscriber’s parking history.
 * 
 * Shows all past ParkingEvent records and highlights the current active session
 * if one exists. The table is populated from the server response.
 */
public class ViewParkingHistoryController implements ClientAware {
    /** Headline label ("Parking History") */
    @FXML private Label headline;

    /** Table displaying the list of past/active parking events */
    @FXML private TableView<ParkingEvent> parkingHistoryTable;

    /** Event ID column */
    @FXML private TableColumn<ParkingEvent, Integer> eventNumberColumn;

    /** Entry date column */
    @FXML private TableColumn<ParkingEvent, LocalDate> entryDateColumn;

    /** Entry hour column */
    @FXML private TableColumn<ParkingEvent, LocalTime> entryHourColumn;

    /** Exit date column (nullable) */
    @FXML private TableColumn<ParkingEvent, LocalDate> exitDateColumn;

    /** Exit hour column (nullable) */
    @FXML private TableColumn<ParkingEvent, LocalTime> exitHourColumn;

    /** Status column (inferred based on exit date/time) */
    @FXML private TableColumn<ParkingEvent, String> status;

    /** Shared client instance used to request the data from server */
    private ClientController client;

    /**
     * Injects the ClientController to allow server communication.
     *
     * @param client active client instance
     */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
    }

    /**
     * Initializes the parking history table: sets up value factories,
     * cell formatting (especially for nullable fields), and sends
     * a request to the server for the subscriber’s data.
     *
     * This method should be called after setClient.
     */
    public void setTable() {
        // Basic column bindings from ParkingEvent fields
        eventNumberColumn.setCellValueFactory(new PropertyValueFactory<>("eventId"));
        entryDateColumn.setCellValueFactory(new PropertyValueFactory<>("entryDate"));
        entryHourColumn.setCellValueFactory(new PropertyValueFactory<>("entryHour"));
        exitDateColumn.setCellValueFactory(new PropertyValueFactory<>("exitDate"));
        exitHourColumn.setCellValueFactory(new PropertyValueFactory<>("exitHour"));
        status.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Format entry time (HH:mm:ss)
        entryHourColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalTime t, boolean empty) {
                super.updateItem(t, empty);
                if (!empty && t != null)
                    setText(t.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            }
        });

        // Exit date: if null → show "Active"
        exitDateColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate d, boolean empty) {
                super.updateItem(d, empty);
                if (empty) return;
                setText(d == null ? "Active"
                        : d.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            }
        });

        // Exit hour: if null → show "Active"
        exitHourColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalTime t, boolean empty) {
                super.updateItem(t, empty);
                if (empty) return;
                setText(t == null ? "Active"
                        : t.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            }
        });

        // Request subscriber's parking history from the server
        client.updateParkingHistoryOfSubscriber();
    }

    /**
     * Called by the ClientController once the parking history data is ready.
     * 
     * Converts the received list into an ObservableList and displays
     * it in the table.
     *
     * @param parkingEventsHistory list of all past and active events
     */
    public void displayHistory(ArrayList<ParkingEvent> parkingEventsHistory) {
        if (parkingEventsHistory == null) {
            System.err.println("[ERROR] Tried to display null parking history – ignoring.");
            return;
        }

        ObservableList<ParkingEvent> rows = FXCollections.observableArrayList(parkingEventsHistory);
        parkingHistoryTable.setItems(rows);
    }

}
