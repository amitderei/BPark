package common;

import java.io.Serializable;

public class ParkingReport implements Serializable{
	private int totalEntries;
	private int totalExtends;
	private int totalLates;
	
	public ParkingReport(int totalEntries, int totalExtends, int totalLates) {
		this.totalEntries=totalEntries;
		this.totalExtends=totalExtends;
		this.totalLates=totalLates;
	}

	public int getTotalEntries() {
		return totalEntries;
	}

	public int getTotalExtends() {
		return totalExtends;
	}

	public int getTotalLates() {
		return totalLates;
	}
	
	
	
}
