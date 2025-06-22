package reportService;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A unified scheduler that handles:
 * 1. Generating all missing monthly reports (parking + subscriber) at startup.
 * 2. Scheduling automatic generation of next reports on the 1st of every month at midnight.
 */
public class MonthlyReportScheduler {

    /** Instance used to create all monthly reports */
    private static final MonthlyReportGenarator generator = new MonthlyReportGenarator();

    /** Timer that manages the scheduling of report generation */
    private static final Timer timer = new Timer("MonthlyReportTimer");

    /** Starts the unified report scheduler. Call once from Server.serverStarted(). */
    public static void start() {
        generator.generatePastReports(); // Catch up on missing months
        scheduleNext();                  // Schedule next execution
    }

    /** Schedules the next monthly report job at 1st of next month 00:00. */
    private static void scheduleNext() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        // If time already passed for this month, go to next
        if (cal.getTime().before(new Date())) {
            cal.add(Calendar.MONTH, 1);
        }

        Date nextRun = cal.getTime();
        System.out.println("Next monthly report scheduled for: " + nextRun);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                generator.generateNewReports(); // Parking + Subscriber
                scheduleNext(); // Chain next month
            }
        }, nextRun);
    }
}
