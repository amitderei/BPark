package controllers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import client.ClientController;
import common.ParkingEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

/**
 * Controller for displaying the subscriber's current active parking event.
 * 
 * Presents details such as entry time, vehicle ID, parking space, and calculates the
 * expected exit time based on whether the session was extended.
 */
public class ViewActiveParkingInfoController implements ClientAware {

    // =================== FXML: Static captions and value labels ===================

    /** Label showing the screen headline (e.g., "Active Parking Info") */
    @FXML private Label headline;

    /** Caption label for the event ID */
    @FXML private Label eventId;
    /** Label that shows the event ID value */
    @FXML private Label eventIdInfo;

    /** Caption label for the vehicle ID */
    @FXML private Label vehicleId;
    /** Label that shows the vehicle ID value */
    @FXML private Label vehicleIdInfo;

    /** Caption label for the entry date */
    @FXML private Label entryDate;
    /** Label that shows the entry date value */
    @FXML private Label entryDateInfo;

    /** Caption label for the entry hour */
    @FXML private Label entryHour;
    /** Label that shows the entry hour value */
    @FXML private Label entryHourInfo;

    /** Caption label for the extension status */
    @FXML private Label extended;
    /** Label that shows whether the parking was extended */
    @FXML private Label extendedInfo;

    /** Caption label for the expected exit date */
    @FXML private Label expectedExitDate;
    /** Label that shows the expected exit date */
    @FXML private Label expectedExitDateInfo;

    /** Caption label for the expected exit hour */
    @FXML private Label expectedExitHour;
    /** Label that shows the expected exit hour */
    @FXML private Label expectedExitHourInfo;

    /** Caption label for the parking space number */
    @FXML private Label parkingSpace;
    /** Label that shows the parking space number */
    @FXML private Label parkingSpaceInfo;

    /** Caption label for the parking code */
    @FXML private Label parkingCode;
    /** Label that shows the parking code */
    @FXML private Label parkingCodeInfo;

    // =================== Runtime variables ===================

    /** Reference to the main client controller used for server communication */
    private ClientController client;

    /** The active parking event returned by the server */
    private ParkingEvent parkingEvent;

    // =================== Dependency Injection ===================

    /**
     * Injects the shared client controller used for server requests and data.
     *
     * @param client the active ClientController
     */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
    }

    /**
     * Stores the ParkingEvent object representing the active parking session.
     *
     * @param parkingEvent the event to be displayed
     */
    public void setParkingEvent(ParkingEvent parkingEvent) {
        this.parkingEvent = parkingEvent;
    }

    // =================== Server Request ===================

    /**
     * Requests the current active parking session of the subscriber from the server.
     */
    public void getDetailsOfActiveInfo() {
        client.getDetailsOfActiveInfo();
    }

    // =================== UI Population ===================

    /**
     * Populates all labels on the screen using the stored parking event data.
     * 
     * Calculates the expected exit time by adding 4 or 8 hours to the entry time,
     * depending on whether the parking session was extended.
     * 
     * This method must be called after setParkingEvent().
     */
    public void setTexts() {
        if (parkingEvent == null) return;

        // Populate static fields
        eventIdInfo.setText(String.valueOf(parkingEvent.getEventId()));
        vehicleIdInfo.setText(parkingEvent.getVehicleId());
        entryDateInfo.setText(parkingEvent.getEntryDate().toString());
        entryHourInfo.setText(parkingEvent.getEntryHour().format(DateTimeFormatter.ofPattern("HH:mm")));
        parkingSpaceInfo.setText(String.valueOf(parkingEvent.getParkingSpace()));
        parkingCodeInfo.setText(parkingEvent.getParkingCode());

        // Calculate expected exit time
        LocalDateTime exitTime = parkingEvent.getEntryDate().atTime(parkingEvent.getEntryHour());

        if (parkingEvent.isWasExtended()) {
            extendedInfo.setText("Yes");
            exitTime = exitTime.plusHours(8);
        } else {
            extendedInfo.setText("No");
            exitTime = exitTime.plusHours(4);
        }

        expectedExitDateInfo.setText(exitTime.toLocalDate().toString());
        expectedExitHourInfo.setText(exitTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
    }
}

