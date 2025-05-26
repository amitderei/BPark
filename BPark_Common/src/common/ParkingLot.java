package common;

import java.io.Serializable;

/**
 * Represents a parking lot in the BPARK system.
 * Contains information about its name, total number of spots,
 * and how many spots are currently occupied.
 */
public class ParkingLot implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Name of the parking lot */
    private String name;

    /** Total number of parking spots */
    private int totalSpots;

    /** Number of currently occupied spots */
    private int occupiedSpots;

    /**
     * Constructs a ParkingLot object.
     *
     * @param name           the parking lot name
     * @param totalSpots     the total number of spots in the lot
     * @param occupiedSpots  the number of spots currently occupied
     */
    public ParkingLot(String name, int totalSpots, int occupiedSpots) {
        this.name = name;
        this.totalSpots = totalSpots;
        this.occupiedSpots = occupiedSpots;
    }

    /**
     * @return the name of the parking lot
     */
    public String getName() {
        return name;
    }

    /**
     * @return the total number of parking spots
     */
    public int getTotalSpots() {
        return totalSpots;
    }

    /**
     * @return the number of currently occupied spots
     */
    public int getOccupiedSpots() {
        return occupiedSpots;
    }

    /**
     * Sets the name of the parking lot.
     *
     * @param name the new parking lot name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the total number of spots in the parking lot.
     *
     * @param totalSpots the total number of spots
     */
    public void setTotalSpots(int totalSpots) {
        this.totalSpots = totalSpots;
    }

    /**
     * Sets the number of currently occupied spots.
     *
     * @param occupiedSpots the new number of occupied spots
     */
    public void setOccupiedSpots(int occupiedSpots) {
        this.occupiedSpots = occupiedSpots;
    }

    @Override
    public String toString() {
        return "ParkingLot{" +
                "name='" + name + '\'' +
                ", totalSpots=" + totalSpots +
                ", occupiedSpots=" + occupiedSpots +
                '}';
    }
}
