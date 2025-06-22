package reportService;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import db.DBController;

/**
 * Unified report generator for both Parking and Subscriber reports.
 * Supports generating:
 * - New reports for the current month
 * - Catch-up reports from February 2025 up to the last full month
 */
public class MonthlyReportGenarator {

    private final DBController db = new DBController();

    /**
     * Generates parking and subscriber reports for the current month (if missing).
     * Called automatically on the 1st of each month.
     */
    public void generateNewReports() {
        Date today = new Date(System.currentTimeMillis());
        LocalDate current = today.toLocalDate();
        int month = current.getMonthValue();
        int year  = current.getYear();

        if (!db.parkingReportExists(today)) {
            db.createParkingReport(today);
        }

        try {
            if (!db.subscriberStatusReportExists(month, year)) {
                db.storeSubscriberStatusReport(month, year);
            }
        } catch (SQLException e) {
            System.err.printf("Failed to store subscriber report %04d-%02d: %s%n", year, month, e.getMessage());
        }
    }

    /**
     * Generates any missing reports from February 2025 up to the last full month.
     * Useful for system bootstrapping or after downtime.
     */
    public void generatePastReports() {
        List<Date> dates = new ArrayList<>();
        LocalDate init = LocalDate.of(2025, 2, 1); // First supported month
        LocalDate now  = LocalDate.now();

        // Last full month (not current if day > 1)
        LocalDate end = now.getDayOfMonth() == 1
                        ? now.minusMonths(1)
                        : now.withDayOfMonth(1).minusMonths(1);

        while (!init.isAfter(end)) {
            Date reportDate = Date.valueOf(init);
            int month = init.getMonthValue();
            int year  = init.getYear();

            // Parking report
            if (!db.parkingReportExists(reportDate)) {
                db.createParkingReport(reportDate);
            }

            // Subscriber report
            try {
                if (!db.subscriberStatusReportExists(month, year)) {
                    db.storeSubscriberStatusReport(month, year);
                }
            } catch (SQLException e) {
                System.err.printf("Failed to store subscriber report %04d-%02d: %s%n", year, month, e.getMessage());
            }

            init = init.plusMonths(1);
        }
    }
}

