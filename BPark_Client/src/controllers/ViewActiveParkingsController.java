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
 * "Live Parked" table for staff.  
 * Lists every vehicle that is currently inside the lot
 * (no exit time set). Populated by a single server call.
 */
public class ViewActiveParkingsController implements ClientAware {

    /* ---------- FXML: table + columns ---------- */
    @FXML private TableView<ParkingEvent> parkingTable;
    @FXML private TableColumn<ParkingEvent, Integer> colEventId;
    @FXML private TableColumn<ParkingEvent, Integer> colSubscriber;
    @FXML private TableColumn<ParkingEvent, String>  colVehicleId;
    @FXML private TableColumn<ParkingEvent, String>  colParkingCode;
    @FXML private TableColumn<ParkingEvent, String>  colLot;
    @FXML private TableColumn<ParkingEvent, Integer> colSpace;
    @FXML private TableColumn<ParkingEvent, String>  colEntryDate;
    @FXML private TableColumn<ParkingEvent, String>  colEntryTime;

    /* ---------- runtime ---------- */
    private ClientController client;
    private final ObservableList<ParkingEvent> data =
            FXCollections.observableArrayList();

    /* =====================================================
     *  table setup
     * ===================================================== */

    /** Builds column value-factories once the FXML is loaded. */
    @FXML
    private void initialize() {
        colEventId     .setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getEventId()));
        colSubscriber  .setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getSubscriberCode()));
        colVehicleId   .setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getVehicleId()));
        colParkingCode .setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getParkingCode()));
        colLot         .setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getNameParkingLot()));
        colSpace       .setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getParkingSpace()));

        colEntryDate.setCellValueFactory(c ->
            new ReadOnlyStringWrapper(c.getValue().getEntryDate().format(DateTimeFormatter.ISO_DATE)));

        colEntryTime.setCellValueFactory(c ->
            new ReadOnlyStringWrapper(c.getValue().getEntryHour().format(DateTimeFormatter.ofPattern("HH:mm"))));
    }

    /* =====================================================
     *  client wiring
     * ===================================================== */

    /**
     * Saves the ClientController and lets it call back with data.
     *
     * @param client active client instance
     */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
        if (client != null)
            client.setViewActiveParkingsController(this);
    }

    /* =====================================================
     *  server request + callback
     * ===================================================== */

    /** Sends "get_active_parkings" to the server. Call after setClient(). */
    public void requestActiveParkingEvents() {
        if (client != null)
            client.requestActiveParkingEvents();
    }

    /**
     * Fills the table when the server responds.
     *
     * @param events list of current ParkingEvent objects
     */
    public void onActiveParkingsReceived(List<ParkingEvent> events) {
        Platform.runLater(() -> {
            data.setAll(events);
            parkingTable.setItems(data);
        });
    }
}

