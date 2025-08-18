package common;

import java.io.Serializable;

/**
 * A basic class that represents a single parking space in the system.
 * Each space has a unique number (ID), and a flag indicating whether
 * it is currently occupied or not.
 * This class is serializable so it can be transferred between server and client.
 */
public class ParkingSpace implements Serializable {

    private static final long serialVersionUID = 1L;

    /** The unique number assigned to this parking space */
    private int parkingSpace;

    /** True if the space is currently taken, false if it's available */
    private boolean isOccupied;

    /**
     * Full constructor for ParkingSpace.
     *
     * @param parkingSpace unique ID of the parking space
     * @param isOccupied   true if the space is currently occupied
     */
    public ParkingSpace(int parkingSpace, boolean isOccupied) {
        this.parkingSpace = parkingSpace;
        this.isOccupied = isOccupied;
    }

    /**
     * Gets the ID of the parking space.
     *
     * @return space number
     */
    public int getParkingSpace() {
        return parkingSpace;
    }

    /**
     * Returns the current occupancy status.
     *
     * @return true if occupied, false otherwise
     */
    public boolean isOccupied() {
        return isOccupied;
    }

    /**
     * Updates the space number.
     *
     * @param parkingSpace new space ID
     */
    public void setParkingSpace(int parkingSpace) {
        this.parkingSpace = parkingSpace;
    }

    /**
     * Updates the occupancy status.
     *
     * @param occupied true if the space is now taken
     */
    public void setOccupied(boolean occupied) {
        this.isOccupied = occupied;
    }

    /**
     * Returns a string summary of the space, used for logging/debugging.
     *
     * @return formatted string with ID and status
     */
    @Override
    public String toString() {
        return "ParkingSpace{" +
                "parkingSpace=" + parkingSpace +
                ", isOccupied=" + isOccupied +
                '}';
    }
}

