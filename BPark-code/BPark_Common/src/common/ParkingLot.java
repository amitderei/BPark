package common;

import java.io.Serializable;

/**
 * A simple data class that represents a parking lot in the BPARK system.
 * Holds basic information like the lot's name, the total number of parking spots,
 * and how many of those spots are currently occupied.
 * Implements Serializable so it can be sent across the network between server and client.
 */
public class ParkingLot implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Name of the parking lot (e.g., "Lot A", "Underground 2") */
    private String name;

    /** Total number of parking spots available in this lot */
    private int totalSpots;

    /** Number of spots that are currently in use */
    private int occupiedSpots;

    /**
     * Full constructor for ParkingLot.
     *
     * @param name           name of the lot
     * @param totalSpots     total number of parking spaces in this lot
     * @param occupiedSpots  number of spots currently taken
     */
    public ParkingLot(String name, int totalSpots, int occupiedSpots) {
        this.name = name;
        this.totalSpots = totalSpots;
        this.occupiedSpots = occupiedSpots;
    }

    /**
     * Gets the name of the parking lot.
     *
     * @return parking lot name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the total number of parking spots.
     *
     * @return total spot count
     */
    public int getTotalSpots() {
        return totalSpots;
    }

    /**
     * Gets the number of currently occupied spots.
     *
     * @return number of spots in use
     */
    public int getOccupiedSpots() {
        return occupiedSpots;
    }

    /**
     * Sets a new name for this parking lot.
     *
     * @param name the new name to assign
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Updates the total number of spots in the lot.
     *
     * @param totalSpots new total capacity
     */
    public void setTotalSpots(int totalSpots) {
        this.totalSpots = totalSpots;
    }

    /**
     * Updates the number of currently occupied spots.
     *
     * @param occupiedSpots number of spots currently in use
     */
    public void setOccupiedSpots(int occupiedSpots) {
        this.occupiedSpots = occupiedSpots;
    }

    /**
     * Returns a string summary of the parking lot, useful for debugging or logging.
     *
     * @return formatted string with name, total, and occupied spot count
     */
    @Override
    public String toString() {
        return "ParkingLot{" +
                "name='" + name + '\'' +
                ", totalSpots=" + totalSpots +
                ", occupiedSpots=" + occupiedSpots +
                '}';
    }
}
