package common;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * A data class representing a parking session, stored in the 'parkingEvent' table.
 * 
 * This object is used to transfer parking event data between client and server.
 * It includes information such as entry/exit times, parking space, vehicle ID,
 * and whether the session was extended.
 * 
 * Implements Serializable to support transmission over a network.
 */
public class ParkingEvent implements Serializable {

	/**
	 * Serial version UID for ensuring compatibility during serialization.
	 */
	private static final long serialVersionUID = 1L;

	/** Unique identifier for this parking event */
	private int eventId;

	/** Subscriber's unique code related to this event */
	private int subscriberCode;

	/** The number of the parking space occupied */
	private int parkingSpace;

	/** Date of entry into the parking lot */
	private LocalDate entryDate;

	/** Time of entry into the parking lot */
	private LocalTime entryTime;

	/** Date of exit from the parking lot (nullable if still parked) */
	private LocalDate exitDate;

	/** Time of exit from the parking lot (nullable if still parked) */
	private LocalTime exitTime;

	/** Indicates whether this parking session was extended */
	private boolean wasExtended;

	/** Name of the parking lot where the vehicle was parked */
	private String lot;

	/** Identifier of the vehicle parked during the session */
	private String vehicleID;

	/** Unique parking code assigned to this session */
	private String parkingCode;

	/** Duration threshold for standard session (4 hours) in minutes */
	private static final long minutesFourHours = 240;

	/** Duration threshold for extended session (8 hours) in minutes */
	private static final long minutesEightHours = 480;

	/**
	 * Full constructor for ParkingEvent.
	 *
	 * @param subscriberCode Subscriber's ID
	 * @param parkingSpace   Assigned parking space number
	 * @param entryDate      Date of vehicle entry
	 * @param entryTime      Time of vehicle entry
	 * @param exitDate       Date of vehicle exit (nullable)
	 * @param exitTime       Time of vehicle exit (nullable)
	 * @param wasExtended    Indicates if session was extended
	 * @param vehicleID2     Vehicle identifier
	 * @param lot            Parking lot name
	 * @param parkingCode    Unique code assigned to session
	 */
	public ParkingEvent(int subscriberCode, int parkingSpace, LocalDate entryDate, LocalTime entryTime,
						LocalDate exitDate, LocalTime exitTime, boolean wasExtended, String vehicleID2, String lot,
						String parkingCode) {
		this.subscriberCode = subscriberCode;
		this.parkingSpace = parkingSpace;
		this.entryDate = entryDate;
		this.entryTime = entryTime;
		this.exitDate = exitDate;
		this.exitTime = exitTime;
		this.wasExtended = wasExtended;
		this.vehicleID = vehicleID2;
		this.lot = lot;
		this.parkingCode = parkingCode;
	}

	/** Default constructor (used for deserialization or empty creation) */
	public ParkingEvent() {}

	/** @return Event ID */
	public int getEventId() {
		return eventId;
	}

	/** @return Subscriber's code */
	public int getSubscriberCode() {
		return subscriberCode;
	}

	/** @return Parking space number */
	public int getParkingSpace() {
		return parkingSpace;
	}

	/** @return Entry date */
	public LocalDate getEntryDate() {
		return entryDate;
	}

	/** @return Entry time */
	public LocalTime getEntryHour() {
		return entryTime;
	}

	/** @return Exit date (null if not set yet) */
	public LocalDate getExitDate() {
		return exitDate;
	}

	/** @return Exit time (null if not set yet) */
	public LocalTime getExitHour() {
		return exitTime;
	}

	/** @return Whether the session was extended */
	public boolean isWasExtended() {
		return wasExtended;
	}

	/** @return Name of the parking lot */
	public String getNameParkingLot() {
		return lot;
	}

	/** @return Vehicle ID */
	public String getVehicleId() {
		return vehicleID;
	}

	/** @return Parking session code */
	public String getParkingCode() {
		return parkingCode;
	}

	/** @return Lot name (alias for consistency) */
	public String getLot() {
		return lot;
	}

	/**
	 * Sets the event ID.
	 *
	 * @param eventId unique identifier for this event
	 */
	public void setEventId(int eventId) {
		this.eventId = eventId;
	}

	/** @param subscriberCode ID of the subscriber */
	public void setSubscriberCode(int subscriberCode) {
		this.subscriberCode = subscriberCode;
	}

	/** @param parkingSpace space number used */
	public void setParkingSpace(int parkingSpace) {
		this.parkingSpace = parkingSpace;
	}

	/** @param entryDate vehicle entry date */
	public void setEntryDate(LocalDate entryDate) {
		this.entryDate = entryDate;
	}

	/** @param entryTime vehicle entry time */
	public void setEntryTime(LocalTime entryTime) {
		this.entryTime = entryTime;
	}

	/** @param exitDate date of vehicle exit */
	public void setExitDate(LocalDate exitDate) {
		this.exitDate = exitDate;
	}

	/** @param exitTime time of vehicle exit */
	public void setExitTime(LocalTime exitTime) {
		this.exitTime = exitTime;
	}

	/** @param wasExtended flag indicating whether session was extended */
	public void setWasExtended(boolean wasExtended) {
		this.wasExtended = wasExtended;
	}

	/** @param lot name of the parking lot */
	public void setLot(String lot) {
		this.lot = lot;
	}

	/** @param vehicleID ID of the parked vehicle */
	public void setVehicleID(String vehicleID) {
		this.vehicleID = vehicleID;
	}

	/** @param parkingCode unique session code */
	public void setParkingCode(String parkingCode) {
		this.parkingCode = parkingCode;
	}

	/**
	 * Calculates the current status of the parking session.
	 * If the session exceeded allowed time (4h regular or 8h extended),
	 * it returns "Delayed", otherwise "On time".
	 *
	 * @return "On time" or "Delayed"
	 */
	public String getStatus() {
		LocalTime exitHour;
		LocalDate exitDateNow;
		LocalTime entryHour = entryTime;

		//if the parking is still active
		if (exitTime == null) {
			exitHour = LocalTime.now();
			exitDateNow = LocalDate.now();
		} else { //if the parking is done
			exitHour = exitTime;
			exitDateNow = exitDate;
		}
		
		// calculates the minutes the parking is active
		LocalDateTime entry = LocalDateTime.of(entryDate, entryHour);
		LocalDateTime exit = LocalDateTime.of(exitDateNow, exitHour);
		Duration duration = Duration.between(entry, exit);
		long totalMinutes = duration.toMinutes();
		
		//check if the subscriber is late according to if the parking was extended
		if (wasExtended) {
			return totalMinutes > minutesEightHours ? "Delayed" : "On time";
		} else {
			return totalMinutes > minutesFourHours ? "Delayed" : "On time";
		}
	}

	/**
	 * Returns a string version of the event, useful for logs or debugging.
	 *
	 * @return summary of parking event
	 */
	@Override
	public String toString() {
		return "ParkingEvent #" + eventId + " | Subscriber: " + subscriberCode + " | Space: " + parkingSpace
				+ " | Entry: " + entryDate + " " + entryTime + " | Exit: " + exitDate + " " + exitTime + " | Extended: "
				+ wasExtended + " | Lot: " + lot + " | Vehicle: " + vehicleID + " | Code: " + parkingCode;
	}
}

