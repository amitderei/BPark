package reportService;

import java.sql.SQLException;
import java.time.LocalDate;
import db.DBController;

/**
 * Generates and stores the monthly Subscriber-Status report.
 * • generateLastMonth() -> snapshot for previous calendar month
 * • generateMissingSince(startMonth, startYear) → fill gaps
 */
public class MonthlySubscriberStatusGenerator {

    private final DBController db = new DBController();

    /** Stores a snapshot for the previous calendar month. */
    public void generateLastMonth() {
        LocalDate prev = LocalDate.now().minusMonths(1);
        storeMonth(prev.getMonthValue(), prev.getYear());
    }

    /**
     * Fills any missing snapshots from the given start (inclusive)
     * up to the last full month.
     */
    public void generateMissingSince(int startMonth, int startYear) {
        LocalDate cur  = LocalDate.of(startYear, startMonth, 1);
        LocalDate last = LocalDate.now().withDayOfMonth(1).minusMonths(1);
        while (!cur.isAfter(last)) {
            storeMonth(cur.getMonthValue(), cur.getYear());
            cur = cur.plusMonths(1);
        }
    }

    /* Helper – store one month */
    private void storeMonth(int month, int year) {
        try {
            int rows = db.storeSubscriberStatusReport(month, year);
            System.out.printf("Subscriber report stored: %04d-%02d (%d rows)%n",
                              year, month, rows);
        } catch (SQLException e) {
            System.err.println("Failed to store subscriber report " +
                               year + "-" + month + ": " + e.getMessage());
        }
    }
}
