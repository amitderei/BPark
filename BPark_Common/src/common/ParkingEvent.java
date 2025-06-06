package common;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Represents a parking event record from the 'parkingEvent' table in the
 * database. Used to transfer parking session data between client and server.
 * Implements Serializable to support object transmission over the network.
 */
public class ParkingEvent implements Serializable {

	private static final long serialVersionUID = 1L;

	private int eventId;
	private int subscriberCode;
	private int parkingSpace;
	private LocalDate entryDate;
	private LocalTime entryTime;
	private LocalDate exitDate;
	private LocalTime exitTime;
	private boolean wasExtended;
	private String lot;
	private String vehicleID;
	private String parkingCode;
	
	private static final long minutesFourHours=240;
	private static final long minutesEightHours=480;

	/**
	 * Constructs a ParkingEvent with all required fields.
	 *
	 * @param eventId        the unique event ID
	 * @param subscriberCode the subscriber code
	 * @param parkingSpace   the parking space used during the event
	 * @param entryDate      the entry date
	 * @param entryHour      the entry time
	 * @param exitDate       the exit date (nullable if still active)
	 * @param exitHour       the exit time (nullable if still active)
	 * @param wasExtended    whether the parking session was extended
	 * @param nameParkingLot the name of the parking lot
	 * @param vehicleId      the vehicle ID parked
	 * @param parkingCode    the unique parking code assigned at entry
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
	
	public ParkingEvent() {}

	/**
	 * Returns the event ID.
	 *
	 * @return the parking event ID
	 */
	public int getEventId() {
		return eventId;
	}
	
	/**
	 * Sets the unique identifier (eventId) for this parking event.
	 *
	 * @param eventId the identifier assigned to this event
	 */
	public void setEventId(int eventId) {
		this.eventId = eventId;
	}


	/**
	 * Returns the subscriber code.
	 *
	 * @return the subscriber's unique code
	 */
	public int getSubscriberCode() {
		return subscriberCode;
	}

	/**
	 * Returns the parking space number.
	 *
	 * @return the number of the parking space used
	 */
	public int getParkingSpace() {
		return parkingSpace;
	}

	/**
	 * Returns the entry date of the parking event.
	 *
	 * @return the entry date
	 */
	public LocalDate getEntryDate() {
		return entryDate;
	}

	/**
	 * Returns the entry time of the parking event.
	 *
	 * @return the entry time
	 */
	public LocalTime getEntryHour() {
		return entryTime;
	}

	/**
	 * Returns the exit date of the parking event.
	 *
	 * @return the exit date, or null if not yet set
	 */
	public LocalDate getExitDate() {
		return exitDate;
	}

	/**
	 * Returns the exit time of the parking event.
	 *
	 * @return the exit time, or null if not yet set
	 */
	public LocalTime getExitHour() {
		return exitTime;
	}

	/**
	 * Indicates whether the parking session was extended.
	 *
	 * @return true if extended, false otherwise
	 */
	public boolean isWasExtended() {
		return wasExtended;
	}

	/**
	 * Returns the name of the parking lot.
	 *
	 * @return the parking lot name
	 */
	public String getNameParkingLot() {
		return lot;
	}

	/**
	 * Returns the vehicle ID associated with the event.
	 *
	 * @return the vehicle ID
	 */
	public String getVehicleId() {
		return vehicleID;
	}

	/**
	 * Returns the parking code issued at entry.
	 *
	 * @return the parking code
	 */
	public String getParkingCode() {
		return parkingCode;
	}

	/**
	 * Sets the exit date.
	 *
	 * @param exitDate the date the vehicle exited
	 */
	public void setExitDate(LocalDate exitDate) {
		this.exitDate = exitDate;
	}

	/**
	 * Sets the exit time.
	 *
	 * @param exitHour the time the vehicle exited
	 */
	public void setExitHour(LocalTime exitHour) {
		this.exitTime = exitHour;
	}

	/**
	 * Sets whether the session was extended.
	 *
	 * @param wasExtended true if extended, false otherwise
	 */
	public void setWasExtended(boolean wasExtended) {
		this.wasExtended = wasExtended;
	}

	public String getLot() {
		return lot;
	}
	
	

	public void setSubscriberCode(int subscriberCode) {
		this.subscriberCode = subscriberCode;
	}

	public void setParkingSpace(int parkingSpace) {
		this.parkingSpace = parkingSpace;
	}

	public void setEntryDate(LocalDate entryDate) {
		this.entryDate = entryDate;
	}

	public void setEntryTime(LocalTime entryTime) {
		this.entryTime = entryTime;
	}

	public void setExitTime(LocalTime exitTime) {
		this.exitTime = exitTime;
	}

	public void setLot(String lot) {
		this.lot = lot;
	}

	public void setVehicleID(String vehicleID) {
		this.vehicleID = vehicleID;
	}

	public void setParkingCode(String parkingCode) {
		this.parkingCode = parkingCode;
	}
	
	public String getStatus() {
		LocalTime exitHour;
		LocalDate exitDateNow;
		LocalTime entryHour=entryTime;
		if (exitTime==null) {
			exitHour= LocalTime.now();
			exitDateNow=LocalDate.now();
		}
		else {
			exitHour=exitTime;
			exitDateNow=exitDate;
		}
		LocalDateTime entry=LocalDateTime.of(entryDate, entryHour);
		LocalDateTime exit=LocalDateTime.of(exitDateNow, exitHour);
		
		Duration duration=Duration.between(entry, exit);
		long totalMinutes=duration.toMinutes();
		
		if (wasExtended) {
			
			if(totalMinutes>minutesEightHours) {
				return "Delayed";
			}
			else {
				return "On time";
			}
		}
		else {
			if(totalMinutes>minutesFourHours) {
				return "Delayed";
			}
			else {
				return "On time";
			}
		}
	}

	@Override
	public String toString() {
		return "ParkingEvent #" + eventId + " | Subscriber: " + subscriberCode + " | Space: " + parkingSpace
				+ " | Entry: " + entryDate + " " + entryTime + " | Exit: " + exitDate + " " + exitTime + " | Extended: "
				+ wasExtended + " | Lot: " + lot + " | Vehicle: " + vehicleID + " | Code: " + parkingCode;
	}
}
