package common;

import java.io.Serializable;

/**
 * A simple data holder for parking-related statistics,
 * typically used in generating summary reports.
 *
 * Contains aggregated data such as total parking entries, number of extensions,
 * number of late departures, and duration distribution (less than 4 hours,
 * between 4 to 8 hours, and more than 8 hours).
 */
public class ParkingReport implements Serializable {

	private static final long serialVersionUID = 1L;

	/** Total number of parking entries recorded */
	private int totalEntries;

	/** Total number of parking sessions that were extended */
	private int totalExtends;

	/** Total number of sessions marked as late (beyond allowed duration) */
	private int totalLates;

	private int lessThanFour;

	private int betweenFourToEight;

	private int moreThanEight;

	/**
	 * Constructs a ParkingReport with all relevant values.
	 *
	 * @param totalEntries total number of parking events
	 * @param totalExtends number of sessions that were extended
	 * @param totalLates   number of sessions that ended late
	 */
	public ParkingReport(int totalEntries, int totalExtends, int totalLates, int lessThanFour, int betweenFourToEight, int moreThanEight) {
		this.totalEntries = totalEntries;
		this.totalExtends = totalExtends;
		this.totalLates = totalLates;
		this.lessThanFour=lessThanFour;
		this.betweenFourToEight=betweenFourToEight;
		this.moreThanEight=moreThanEight;
	}

	/**
	 * Returns the total number of parking entries recorded.
	 *
	 * @return total parking entries
	 */
	public int getTotalEntries() {
		return totalEntries;
	}

	/**
	 * Returns the total number of parking sessions that were extended.
	 *
	 * @return number of extensions
	 */
	public int getTotalExtends() {
		return totalExtends;
	}
	/**
	 * Returns the total number of parking sessions that ended late.
	 *
	 * @return number of late sessions
	 */
	public int getTotalLates() {
		return totalLates;
	}

	/**
	 * Returns the serialization version UID for the class.
	 *
	 * @return the serial version UID
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	/**
	 * Returns the number of parking sessions shorter than four hours.
	 *
	 * @return count of sessions under 4 hours
	 */
	public int getLessThanFour() {
		return lessThanFour;
	}

	/**
	 * Returns the number of parking sessions between four and eight hours.
	 *
	 * @return count of sessions between 4 to 8 hours
	 */
	public int getBetweenFourToEight() {
		return betweenFourToEight;
	}

	/**
	 * Returns the number of parking sessions longer than eight hours.
	 *
	 * @return count of sessions over 8 hours
	 */
	public int getMoreThanEight() {
		return moreThanEight;
	}
}
