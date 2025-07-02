package controllers;

import client.ClientController;
import common.ParkingEvent;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for the staff-facing screen that displays all currently parked vehicles.
 * 
 * Shows active ParkingEvent objects (those without exit time).
 * Data is requested once from the server and displayed in a table.
 */
public class ViewActiveParkingsController implements ClientAware {
	
    /** Main table for displaying all active parking events */
    @FXML 
    private TableView<ParkingEvent> parkingTable;

    /** Column for parking event ID */
    @FXML 
    private TableColumn<ParkingEvent, Integer> colEventId;

    /** Column for subscriber code */
    @FXML 
    private TableColumn<ParkingEvent, Integer> colSubscriber;

    /** Column for vehicle ID */
    @FXML 
    private TableColumn<ParkingEvent, String> colVehicleId;


    /** Column for parking space number */
    @FXML 
    private TableColumn<ParkingEvent, Integer> colSpace;

    /** Column for entry date (formatted) */
    @FXML 
    private TableColumn<ParkingEvent, String> colEntryDate;

    /** Column for entry time (formatted) */
    @FXML 
    private TableColumn<ParkingEvent, String> colEntryTime;

    /** Central client used to request data and receive callbacks */
    private ClientController client;

    /** Observable table data for live UI updates */
    private final ObservableList<ParkingEvent> data = FXCollections.observableArrayList();

    /**
     * Called automatically after the FXML is loaded.
     * Sets up value factories for each column in the table.
     */
    @FXML
    private void initialize() {
        colEventId     .setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getEventId()));
        colSubscriber  .setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getSubscriberCode()));
        colVehicleId   .setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getVehicleId()));
        colSpace       .setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getParkingSpace()));

        // Format entry date as "yyyy-MM-dd"
        colEntryDate.setCellValueFactory(c ->
            new ReadOnlyStringWrapper(
                c.getValue().getEntryDate().format(DateTimeFormatter.ISO_DATE)
            ));

        // Format entry time as "HH:mm"
        colEntryTime.setCellValueFactory(c ->
            new ReadOnlyStringWrapper(
                c.getValue().getEntryHour().format(DateTimeFormatter.ofPattern("HH:mm"))
            ));
    }

    /**
     * Injects the active ClientController instance for server communication.
     * Also registers this controller inside the client for callback access.
     *
     * @param client the connected client instance
     */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
        if (client != null)
            client.setViewActiveParkingsController(this);
    }

    /**
     * Sends a one-time request to the server to fetch all currently active parking sessions.
     * Should be called once after setClient().
     */
    public void requestActiveParkingEvents() {
        if (client != null)
            client.requestActiveParkingEvents();
    }

    /**
     * Called by the client when the server responds with a list of current parking events.
     * The method updates the table on the JavaFX Application Thread.
     *
     * @param events list of active ParkingEvent objects
     */
    public void onActiveParkingsReceived(List<ParkingEvent> events) {
        Platform.runLater(() -> {
            data.setAll(events);
            parkingTable.setItems(data);
        });
    }
}

