package common;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Time;

/**
 * Represents a parking event record from the 'parkingEvent' table in the database.
 * Used to transfer parking session data between client and server.
 * Implements Serializable to support object transmission over the network.
 */
public class ParkingEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private int eventId;
    private int subscriberCode;
    private Date entryDate;
    private Time entryHour;
    private Date exitDate;
    private Time exitHour;
    private boolean wasExtended;
    private String nameParkingLot;
    private String vehicleId;
    private int parkingCode;

    /**
     * Constructs a ParkingEvent with all required fields.
     *
     * @param eventId         the unique event ID
     * @param subscriberCode  the subscriber code
     * @param entryDate       the entry date
     * @param entryHour       the entry time
     * @param exitDate        the exit date (nullable if still active)
     * @param exitHour        the exit time (nullable if still active)
     * @param wasExtended     whether the session was extended
     * @param nameParkingLot  the name of the parking lot
     * @param vehicleId       the vehicle ID
     * @param parkingCode     the unique parking code issued at entry
     */
    public ParkingEvent(int eventId, int subscriberCode, Date entryDate, Time entryHour,
                        Date exitDate, Time exitHour, boolean wasExtended,
                        String nameParkingLot, String vehicleId, int parkingCode) {
        this.eventId = eventId;
        this.subscriberCode = subscriberCode;
        this.entryDate = entryDate;
        this.entryHour = entryHour;
        this.exitDate = exitDate;
        this.exitHour = exitHour;
        this.wasExtended = wasExtended;
        this.nameParkingLot = nameParkingLot;
        this.vehicleId = vehicleId;
        this.parkingCode = parkingCode;
    }

    /** Returns the event ID. */
    public int getEventId() {
        return eventId;
    }

    /** Returns the subscriber code. */
    public int getSubscriberCode() {
        return subscriberCode;
    }

    /** Returns the entry date. */
    public Date getEntryDate() {
        return entryDate;
    }

    /** Returns the entry hour. */
    public Time getEntryHour() {
        return entryHour;
    }

    /** Returns the exit date, or null if not yet set. */
    public Date getExitDate() {
        return exitDate;
    }

    /** Returns the exit hour, or null if not yet set. */
    public Time getExitHour() {
        return exitHour;
    }

    /** Returns whether the session was extended. */
    public boolean isWasExtended() {
        return wasExtended;
    }

    /** Returns the name of the parking lot. */
    public String getNameParkingLot() {
        return nameParkingLot;
    }

    /** Returns the vehicle ID. */
    public String getVehicleId() {
        return vehicleId;
    }

    /** Returns the parking code assigned at entry. */
    public int getParkingCode() {
        return parkingCode;
    }

    /** Sets the exit date. */
    public void setExitDate(Date exitDate) {
        this.exitDate = exitDate;
    }

    /** Sets the exit hour. */
    public void setExitHour(Time exitHour) {
        this.exitHour = exitHour;
    }

    /** Sets the wasExtended flag. */
    public void setWasExtended(boolean wasExtended) {
        this.wasExtended = wasExtended;
    }

    @Override
    public String toString() {
        return "ParkingEvent #" + eventId +
               " | Subscriber: " + subscriberCode +
               " | Entry: " + entryDate + " " + entryHour +
               " | Exit: " + exitDate + " " + exitHour +
               " | Extended: " + wasExtended +
               " | Lot: " + nameParkingLot +
               " | Vehicle: " + vehicleId +
               " | Code: " + parkingCode;
    }
}

