package common;

/**
 * Tiny data-holder for a car that can enter BPARK.
 * A vehicle is identified by a plate / RFID string.  
 * We also keep who owns the car (a Subscriber) and
 * whether the car is currently inside a lot.
 *
 */
public class Vehicle {

    private String      vehicleId;   // licence plate or RFID
    private Subscriber  owner;       // the subscriber that registered the car
    private boolean     parked;      // true ⇒ car is in the lot

    /**
     * Full constructor – sets all fields.
     *
     * @param vehicleId unique vehicle identifier
     * @param owner     subscriber that owns the vehicle
     * @param parked    current parking state (true = inside)
     */
    public Vehicle(String vehicleId, Subscriber owner, boolean parked) {
        this.vehicleId = vehicleId;
        this.owner     = owner;
        this.parked    = parked;
    }

    /**
     * Convenience constructor when the owner is not yet known.
     *
     * @param vehicleId unique vehicle identifier
     * @param parked    current parking state (true = inside)
     */
    public Vehicle(String vehicleId, boolean parked) {
        this(vehicleId, null, parked);
    }

    /* ───────── getters / setters ───────── */

    public String getVehicleId()          { return vehicleId; }
    public void   setVehicleId(String id) { this.vehicleId = id; }

    public Subscriber getOwner()                { return owner; }
    public void       setOwner(Subscriber s)    { this.owner = s; }

    public boolean isParked()               { return parked; }
    public void    setParked(boolean state) { this.parked = state; }

    /* ───────── utility ───────── */

    /**
     * Simple one-line dump for logs / debugging.
     *
     * @return human-readable description
     */
    @Override
    public String toString() {
        return "Vehicle{id='" + vehicleId + "', owner=" +
               (owner != null ? owner.getSubscriberCode() : "none") +
               ", parked=" + parked + '}';
    }
}

