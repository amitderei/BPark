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
 * Displays all currently active parking events (no exit time).
 * Accessed from the staff menu.
 */
public class ViewActiveParkingsController implements ClientAware {

    /* ---------- FXML-injected UI elements ---------- */
    @FXML private TableView<ParkingEvent> parkingTable;
    @FXML private TableColumn<ParkingEvent, Integer> colEventId;
    @FXML private TableColumn<ParkingEvent, Integer> colSubscriber;
    @FXML private TableColumn<ParkingEvent, String>  colVehicleId;
    @FXML private TableColumn<ParkingEvent, String>  colParkingCode;  // FIXED: Changed from Integer to String
    @FXML private TableColumn<ParkingEvent, String>  colLot;
    @FXML private TableColumn<ParkingEvent, Integer> colSpace;
    @FXML private TableColumn<ParkingEvent, String>  colEntryDate;
    @FXML private TableColumn<ParkingEvent, String>  colEntryTime;

    /* ---------- Runtime fields ---------- */
    private ClientController client;
    private final ObservableList<ParkingEvent> data = FXCollections.observableArrayList();

    /**
     * Called automatically after the FXML is loaded.
     * Sets up table columns.
     */
    @FXML
    private void initialize() {
        colEventId.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getEventId()));
        colSubscriber.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getSubscriberCode()));
        colVehicleId.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getVehicleId()));
        colParkingCode.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getParkingCode())); // FIXED
        colLot.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getNameParkingLot()));
        colSpace.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getParkingSpace()));

        colEntryDate.setCellValueFactory(cell -> {
            String formattedDate = cell.getValue().getEntryDate().format(DateTimeFormatter.ISO_DATE);
            return new ReadOnlyStringWrapper(formattedDate);
        });

        colEntryTime.setCellValueFactory(cell -> {
            String formattedTime = cell.getValue().getEntryHour().format(DateTimeFormatter.ofPattern("HH:mm"));
            return new ReadOnlyStringWrapper(formattedTime);
        });
    }

    /**
     * Injects the ClientController instance and triggers data fetch.
     */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
        client.setViewActiveParkingsController(this);
        client.requestActiveParkingEvents();
    }

    /**
     * Called by ClientController when active parking events are received.
     *
     * @param events List of currently active ParkingEvent objects
     */
    public void onActiveParkingsReceived(List<ParkingEvent> events) {
        Platform.runLater(() -> {
            data.setAll(events);
            parkingTable.setItems(data);
        });
    }
}