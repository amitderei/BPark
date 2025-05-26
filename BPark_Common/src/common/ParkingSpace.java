package common;

import java.io.Serializable;

/**
 * Represents a parking space in the system.
 * Each space has a unique number and occupancy status.
 */
public class ParkingSpace implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Unique ID of the parking space */
    private int parkingSpace;

    /** Whether the space is currently occupied */
    private boolean isOccupied;

    /**
     * Constructs a ParkingSpace object.
     *
     * @param parkingSpace the space ID
     * @param isOccupied   true if occupied, false if free
     */
    public ParkingSpace(int parkingSpace, boolean isOccupied) {
        this.parkingSpace = parkingSpace;
        this.isOccupied = isOccupied;
    }

    /**
     * @return the ID of this parking space
     */
    public int getParkingSpace() {
        return parkingSpace;
    }

    /**
     * @return true if this space is occupied
     */
    public boolean isOccupied() {
        return isOccupied;
    }

    /**
     * Sets the ID of the parking space.
     *
     * @param parkingSpace the new space ID
     */
    public void setParkingSpace(int parkingSpace) {
        this.parkingSpace = parkingSpace;
    }

    /**
     * Sets the occupancy status of this space.
     *
     * @param occupied true if now occupied
     */
    public void setOccupied(boolean occupied) {
        this.isOccupied = occupied;
    }

    @Override
    public String toString() {
        return "ParkingSpace{" +
                "parkingSpace=" + parkingSpace +
                ", isOccupied=" + isOccupied +
                '}';
    }
}
