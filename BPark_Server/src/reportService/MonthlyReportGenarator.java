package reportService;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import db.DBController;

/**
 * This class is responsible for generating monthly parking reports.
 * It supports generating:
 * - The current month's report
 * - All past reports from May 2025 onwards (if missing)
 */
public class MonthlyReportGenarator {

    /** Database controller used to create and check reports */
    private DBController db = new DBController();

    /**
     * Generates a new report for the current month.
     * This method should be called at the start of each new month.
     */
    public void generateNewReport() {
        // Create report for today
        db.createParkingReport(new Date(System.currentTimeMillis()));
    }

    /**
     * Generates reports for past months, starting from May 2025.
     * Only missing reports are created – if a report already exists for a month, it is skipped.
     * This is useful to catch up after system downtime or late deployment.
     */
    public void generatePastReport() {
        List<Date> dates = new ArrayList<>();
        LocalDate init = LocalDate.of(2025, 5, 1); // Start date (first supported month)
        LocalDate now = LocalDate.now();
        LocalDate end;

        // Determine last full month to include
        if (now.getDayOfMonth() == 1) {
            // If today is the 1st, we can include this month
            end = now;
        } else {
            // Otherwise, go back to the start of the previous month
            end = now.withDayOfMonth(1).minusMonths(1);
        }

        // Build list of months to check, month by month
        while (!init.isAfter(end)) {
            dates.add(Date.valueOf(init));
            init = init.plusMonths(1);
        }

        // For each month, check if a report exists; if not, create it
        for (int i = 0; i < dates.size(); i++) {
            if (!db.parkingReportExists(dates.get(i))) {
                db.createParkingReport(dates.get(i));
            }
        }
    }
}
