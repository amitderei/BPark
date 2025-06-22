package common;

import java.io.Serializable;
import java.util.Objects;

/**
 * Tiny data–holder we use for the **“Subscriber-Status” monthly report**.  
 * Each instance is one subscriber, one calendar month.
 *
 * <p>We keep the class <em>immutable</em> – once you build the object,
 * nothing inside can change. That way the report rows stay consistent.</p>
 *
 * <p>Written by students – feel free to improve!</p>
 */
public class SubscriberStatusRow implements Serializable {

    /** Mandatory for every Serializable class (easiest way to keep it stable). */
    private static final long serialVersionUID = 1L;

    /* ------------------------------------------------------------------
     * Fields (all final → read-only)
     * ------------------------------------------------------------------ */

    /** The subscriber’s numeric code (primary key in the DB). */
    private final int code;

    /** Full name we build from first + last (handy for tables and charts). */
    private final String name;

    /** How many parking events the subscriber had this month. */
    private final int totalEntries;

    /** How many times the subscriber clicked “Extend parking”. */
    private final int totalExtends;

    /** How many events were flagged as “late exit”. */
    private final int totalLates;

    /** Sum of parking hours for the month (minutes / 60). */
    private final double totalHours;

    /* ------------------------------------------------------------------
     * Constructor
     * ------------------------------------------------------------------ */

    /**
     * Builds an immutable row for the report.
     *
     * @param code         subscriber code
     * @param name         full name (first + last)
     * @param totalEntries number of parking events in the month
     * @param totalExtends number of extensions in the month
     * @param totalLates   number of late exits in the month
     * @param totalHours   total parking hours in the month
     */
    public SubscriberStatusRow(int code,
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

    /* ------------------------------------------------------------------
     * Getters (needed for TableView / charts)
     * ------------------------------------------------------------------ */

    /** @return subscriber code */
    public int getCode()         { return code; }

    /** @return full name */
    public String getName()      { return name; }

    /** @return parking events count */
    public int getTotalEntries() { return totalEntries; }

    /** @return extension count */
    public int getTotalExtends() { return totalExtends; }

    /** @return late-exit count */
    public int getTotalLates()   { return totalLates; }

    /** @return total hours parked */
    public double getTotalHours(){ return totalHours; }

    /* ------------------------------------------------------------------
     * equals / hashCode – auto-generated then cleaned up
     * ------------------------------------------------------------------ */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SubscriberStatusRow)) return false;
        SubscriberStatusRow that = (SubscriberStatusRow) o;
        return code == that.code &&
               totalEntries == that.totalEntries &&
               totalExtends == that.totalExtends &&
               totalLates == that.totalLates &&
               Double.compare(that.totalHours, totalHours) == 0 &&
               Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, name,
                            totalEntries, totalExtends,
                            totalLates, totalHours);
    }

    /* ------------------------------------------------------------------
     * Handy for debugging / logs
     * ------------------------------------------------------------------ */
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
