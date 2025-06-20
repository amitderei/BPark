package reportService;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import db.DBController;
public class MonthlyReportGenarator {
	private DBController db= new DBController();

	/**
	 * generate new report of new month
	 */
	public void generateNewReport() {
		db.createParkingReport(new Date(System.currentTimeMillis()));
	}

	/**
	 * generate the past months from May 2025. (only the missing months)
	 */
	public void generatePastReport() {
		LocalDate end;
		List<Date> dates = new ArrayList<>();
		LocalDate init = LocalDate.of(2025, 5, 1);
		LocalDate now = LocalDate.now();
		if (now.getDayOfMonth() == 1) {
			end = now;
		} else {
			end = now.withDayOfMonth(1).minusMonths(1);
		}
		while (!init.isAfter(end)) {
			dates.add(Date.valueOf(init));
			init = init.plusMonths(1);
		}

		for (int i = 0; i < dates.size(); i++) {
			if (!db.parkingReportExists(dates.get(i))) {
				db.createParkingReport(dates.get(i));
			}
		}
	}
}
