package common;

import java.io.Serializable;

/**
 * A simple data holder for parking-related statistics,
 * typically used in generating summary reports.
 * Contains total counts for vehicle entries, parking extensions,
 * and late departures for a given period (e.g., per month).
 */
public class ParkingReport implements Serializable {

	private static final long serialVersionUID = 1L;

	/** Total number of parking entries recorded */
	private int totalEntries;

	/** Total number of parking sessions that were extended */
	private int totalExtends;

	/** Total number of sessions marked as late (beyond allowed duration) */
	private int totalLates;

	/**
	 * Constructs a ParkingReport with all relevant values.
	 *
	 * @param totalEntries total number of parking events
	 * @param totalExtends number of sessions that were extended
	 * @param totalLates   number of sessions that ended late
	 */
	public ParkingReport(int totalEntries, int totalExtends, int totalLates) {
		this.totalEntries = totalEntries;
		this.totalExtends = totalExtends;
		this.totalLates = totalLates;
	}

	/**
	 * @return total number of parking entries
	 */
	public int getTotalEntries() {
		return totalEntries;
	}

	/**
	 * @return total number of parking extensions
	 */
	public int getTotalExtends() {
		return totalExtends;
	}

	/**
	 * @return total number of late parking sessions
	 */
	public int getTotalLates() {
		return totalLates;
	}
}
