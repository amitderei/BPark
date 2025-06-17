package common;

import java.io.Serializable;

public class ParkingReport implements Serializable{
	private int totalEntries;
	private int totalExtends;
	
	public ParkingReport(int totalEntries, int totalExtends) {
		this.totalEntries=totalEntries;
		this.totalExtends=totalExtends;
	}

	public int getTotalEntries() {
		return totalEntries;
	}

	public int getTotalExtends() {
		return totalExtends;
	}
	
	
}
