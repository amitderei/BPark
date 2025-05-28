package common;

/**
 * Represents a vehicle in the system.
 * Each vehicle has a unique ID, an owner (subscriber), and a flag indicating whether it is currently parked.
 */
public class Vehicle {

    private String vehicleId;

    // Uncomment this once the Subscriber class is implemented
    /* private Subscriber owner; */

    private boolean isParking;

    /**
     * Constructor to initialize a vehicle with its ID and parking status.
     *
     * @param vehicleId The unique ID of the vehicle.
     * @param isParking True if the vehicle is currently parked, false otherwise.
     *
     * // @param owner The subscriber who owns the vehicle. (Enable once Subscriber class is ready)
     */
    public Vehicle(String vehicleId, /* Subscriber owner, */ boolean isParking) {
        this.vehicleId = vehicleId;
        // this.owner = owner; // Uncomment when ready
        this.isParking = isParking;
    }

    /**
     * Gets the ID of the vehicle.
     *
     * @return The vehicle's unique ID.
     */
    public String getVehicleId() {
        return vehicleId;
    }

    /**
     * Sets the ID of the vehicle.
     *
     * @param vehicleId The vehicle's new ID.
     */
    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    // Uncomment these methods after implementing the Subscriber class

    /*
    /**
     * Gets the owner (subscriber) of the vehicle.
     *
     * @return The subscriber who owns the vehicle.
     */
    /*
    public Subscriber getOwner() {
        return owner;
    }
    */

    /*
    /**
     * Sets the owner (subscriber) of the vehicle.
     *
     * @param owner The subscriber to set as the owner.
     */
    /*
    public void setOwner(Subscriber owner) {
        this.owner = owner;
    }
    */

    /**
     * Checks if the vehicle is currently parked.
     *
     * @return True if parked, false otherwise.
     */
    public boolean isParking() {
        return isParking;
    }

    /**
     * Sets the vehicle's parking status.
     *
     * @param isParking True if the vehicle is parked, false otherwise.
     */
    public void setParking(boolean isParking) {
        this.isParking = isParking;
    }
    
    /**
     * Returns a string representation of the Vehicle object.
     * The format typically includes the vehicle ID and parking status.
     * This method is useful for logging, debugging, or displaying the vehicle information.
     *
     * @return a string describing the vehicle.
     */
    @Override
    public String toString() {
        return "Vehicle [vehicleId=" + vehicleId + /*", owner=" *+ owner + */", + isParking=" + isParking + "]";
    }
}
