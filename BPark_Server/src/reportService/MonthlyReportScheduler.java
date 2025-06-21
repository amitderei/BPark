package reportService;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A scheduler that automatically generates a monthly parking report
 * on the first day of each month at midnight.
 * When started, it also generates any missing reports from previous months.
 */
public class MonthlyReportScheduler {

    /** Instance used to create monthly reports */
    private static MonthlyReportGenarator monthlyReportGenarator = new MonthlyReportGenarator();

    /** Timer that manages the scheduling of report generation */
    private static Timer timer = new Timer();

    /**
     * Starts the report scheduler.
     * Generates missing reports from May 2025 up to the current month,
     * and sets up a timer to automatically generate next month's report.
     */
    public static void start() {
        // Generate all past reports (if not already generated)
        monthlyReportGenarator.generatePastReport();

        // Schedule the next monthly report
        scheduleNext();
    }

    /**
     * Schedules the report generation task for the 1st of the next month at 00:00.
     */
    private static void scheduleNext() {
        Calendar calendar = Calendar.getInstance();

        // Set the time to the 1st of the current month at 00:00
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        // If that time has already passed, schedule for the next month
        if (calendar.getTime().before(new Date())) {
            calendar.add(Calendar.MONTH, 1);
        }

        Date nextRun = calendar.getTime();
        System.out.println("Next report scheduled for: " + nextRun);

        // Schedule the report task
        timer.schedule(new TimerTask() {
            public void run() {
                // Generate the current month's report
                monthlyReportGenarator.generateNewReport();

                // Reschedule for the next month
                scheduleNext();
            }
        }, nextRun);
    }
}
