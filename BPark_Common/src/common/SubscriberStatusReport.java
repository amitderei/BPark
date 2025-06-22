package common;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents one subscriber's status for a specific month.
 * Used to build the monthly report that includes entries, extensions,
 * late exits, and total hours parked.
 *
 * This class is immutable – all fields are final and values don’t change.
 */
public class SubscriberStatusReport implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Subscriber code (unique ID from the database) */
    private final int code;

    /** Full name of the subscriber (first + last) */
    private final String name;

    /** How many times the subscriber parked this month */
    private final int totalEntries;

    /** How many times they extended their parking */
    private final int totalExtends;

    /** How many times they left late */
    private final int totalLates;

    /** Total parking time in hours (rounded) */
    private final double totalHours;

    /**
     * Builds a report row with all the data for one subscriber in one month.
     *
     * @param code         subscriber's ID
     * @param name         full name
     * @param totalEntries number of times parked
     * @param totalExtends number of extensions
     * @param totalLates   number of late exits
     * @param totalHours   total time parked (in hours)
     */
    public SubscriberStatusReport(int code,
                               String name,
                               int totalEntries,
                               int totalExtends,
                               int totalLates,
                               double totalHours) {
        this.code = code;
        this.name = name;
        this.totalEntries = totalEntries;
        this.totalExtends = totalExtends;
        this.totalLates = totalLates;
        this.totalHours = totalHours;
    }

    /** @return the subscriber’s code */
    public int getCode() {
        return code;
    }

    /** @return the subscriber’s full name */
    public String getName() {
        return name;
    }

    /** @return how many times they parked */
    public int getTotalEntries() {
        return totalEntries;
    }

    /** @return how many times they extended */
    public int getTotalExtends() {
        return totalExtends;
    }

    /** @return how many times they were late */
    public int getTotalLates() {
        return totalLates;
    }

    /** @return total hours parked in the month */
    public double getTotalHours() {
        return totalHours;
    }

    /**
     * Compares this object with another to check if they're equal
     * (based on all fields).
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SubscriberStatusReport)) return false;
        SubscriberStatusReport that = (SubscriberStatusReport) o;
        return code == that.code &&
               totalEntries == that.totalEntries &&
               totalExtends == that.totalExtends &&
               totalLates == that.totalLates &&
               Double.compare(that.totalHours, totalHours) == 0 &&
               Objects.equals(name, that.name);
    }

    /**
     * Generates a hash code for this object.
     * Helps with storing in sets, hash maps, etc.
     */
    @Override
    public int hashCode() {
        return Objects.hash(code, name,
                            totalEntries, totalExtends,
                            totalLates, totalHours);
    }

    /**
     * Returns a text version of this row – used for debugging.
     */
    @Override
    public String toString() {
        return "SubscriberStatusRow{" +
               "code=" + code +
               ", name='" + name + '\'' +
               ", totalEntries=" + totalEntries +
               ", totalExtends=" + totalExtends +
               ", totalLates=" + totalLates +
               ", totalHours=" + totalHours +
               '}';
    }
}

