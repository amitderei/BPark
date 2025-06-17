package controllers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import client.ClientController;
import common.ParkingEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

/**
 * Displays the current active parking event for the logged-in subscriber.  
 * After the controller receives a ParkingEvent from the server it fills
 * all labels, including the calculated exit time (4 h / 8 h if extended).
 */
public class ViewActiveParkingInfoController implements ClientAware {

    /* ---------- static captions + value labels ---------- */
    @FXML private Label headline;

    @FXML private Label eventId;             @FXML private Label eventIdInfo;
    @FXML private Label vehicleId;           @FXML private Label vehicleIdInfo;
    @FXML private Label entryDate;           @FXML private Label entryDateInfo;
    @FXML private Label entryHour;           @FXML private Label entryHourInfo;
    @FXML private Label extended;            @FXML private Label extendedInfo;
    @FXML private Label expectedExitDate;    @FXML private Label expectedExitDateInfo;
    @FXML private Label expectedExitHour;    @FXML private Label expectedExitHourInfo;
    @FXML private Label parkingSpace;        @FXML private Label parkingSpaceInfo;
    @FXML private Label parkingCode;         @FXML private Label parkingCodeInfo;

    /* ---------- runtime ---------- */
    private ClientController client;
    /** Holds the active parking event returned by the server. */
    private ParkingEvent parkingEvent;

    // ---------------------------------------------------------------------
    // DI
    // ---------------------------------------------------------------------

    @Override
    public void setClient(ClientController client) {
        this.client = client;
    }

    /** Stores the event so setTexts() can populate the screen. */
    public void setParkingEvent(ParkingEvent parkingEvent) {
        this.parkingEvent = parkingEvent;
    }

    // ---------------------------------------------------------------------
    // Server request
    // ---------------------------------------------------------------------

    /** Asks the server for the subscriber’s currently active parking event. */
    public void getDetailsOfActiveInfo() {
        client.getDetailsOfActiveInfo();
    }

    // ---------------------------------------------------------------------
    // UI population
    // ---------------------------------------------------------------------

    /**
     * Copies all data from parkingEvent into the labels.
     * Handles the "was extended" flag by adding 4 h or 8 h accordingly.
     * Call this after setParkingEvent().
     */
    public void setTexts() {
        if (parkingEvent == null) return;

        // plain fields
        eventIdInfo.setText(String.valueOf(parkingEvent.getEventId()));
        vehicleIdInfo.setText(parkingEvent.getVehicleId());
        entryDateInfo.setText(parkingEvent.getEntryDate().toString());
        entryHourInfo.setText(parkingEvent.getEntryHour()
                                               .format(DateTimeFormatter.ofPattern("HH:mm")));
        parkingSpaceInfo.setText(String.valueOf(parkingEvent.getParkingSpace()));
        parkingCodeInfo.setText(parkingEvent.getParkingCode());

        // calculate exit time
        LocalDateTime exitTime = parkingEvent.getEntryDate()
                                             .atTime(parkingEvent.getEntryHour());

        if (parkingEvent.isWasExtended()) {
            extendedInfo.setText("Yes");
            exitTime = exitTime.plusHours(8);
        } else {
            extendedInfo.setText("No");
            exitTime = exitTime.plusHours(4);
        }

        expectedExitDateInfo.setText(exitTime.toLocalDate().toString());
        expectedExitHourInfo.setText(exitTime.toLocalTime()
                                             .format(DateTimeFormatter.ofPattern("HH:mm")));
    }
}

