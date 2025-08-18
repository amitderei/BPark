package common;

/**
 * Represents a vehicle that can enter and park in the BPARK system.
 * Each vehicle has an identifier,
 * an optional owner (a subscriber), and a flag that indicates
 * whether the vehicle is currently parked.
 */
public class Vehicle {

    /** The vehicle's ID */
    private String vehicleId;

    /** The subscriber who owns the vehicle */
    private Subscriber owner;

    /** Indicates whether the vehicle is currently parked */
    private boolean parked;

    /**
     * Full constructor - sets all fields.
     *
     * @param vehicleId unique vehicle identifier
     * @param owner     subscriber that owns the vehicle
     * @param parked    true if the vehicle is currently in the parking lot
     */
    public Vehicle(String vehicleId, Subscriber owner, boolean parked) {
        this.vehicleId = vehicleId;
        this.owner = owner;
        this.parked = parked;
    }

    /**
     * Constructor used when the vehicle has no owner registered yet.
     *
     * @param vehicleId vehicle's unique identifier
     * @param parked    true if currently parked
     */
    public Vehicle(String vehicleId, boolean parked) {
        this(vehicleId, null, parked);
    }

    /**
     * @return the vehicle's ID
     */
    public String getVehicleId() {
        return vehicleId;
    }

    /**
     * Sets the vehicle's ID.
     *
     * @param id the new vehicle ID
     */
    public void setVehicleId(String id) {
        this.vehicleId = id;
    }

    /**
     * @return the subscriber who owns the vehicle
     */
    public Subscriber getOwner() {
        return owner;
    }

    /**
     * Sets the owner of the vehicle.
     *
     * @param s the subscriber who owns the vehicle
     */
    public void setOwner(Subscriber s) {
        this.owner = s;
    }

    /**
     * @return true if the vehicle is currently parked
     */
    public boolean isParked() {
        return parked;
    }

    /**
     * Sets the parking state of the vehicle.
     *
     * @param state true if parked, false otherwise
     */
    public void setParked(boolean state) {
        this.parked = state;
    }

    /**
     * Returns a short string summary of the vehicle.
     * Useful for debugging or logs.
     *
     * @return formatted string with vehicle info
     */
    @Override
    public String toString() {
        return "Vehicle{id='" + vehicleId + "', owner=" +
                (owner != null ? owner.getSubscriberCode() : "none") +
                ", parked=" + parked + '}';
    }
}
