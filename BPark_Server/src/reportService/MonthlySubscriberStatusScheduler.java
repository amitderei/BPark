package reportService;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Starts a Timer that:
 * 1. Fills missing subscriber-status snapshots at server startup.
 * 2. Schedules automatic generation every month on the 1st at 00:00.
 */
public class MonthlySubscriberStatusScheduler {

    private static final MonthlySubscriberStatusGenerator gen =
            new MonthlySubscriberStatusGenerator();
    private static final Timer timer = new Timer("SubscriberReportTimer");

    /** Call once from Server.serverStarted() */
    public static void start() {
        gen.generateMissingSince(5, 2025);  // May-2025 onward
        scheduleNext();
    }

    /* Schedules the next run at the 1st 00:00 */
    private static void scheduleNext() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        if (cal.getTime().before(new Date())) {
            cal.add(Calendar.MONTH, 1);
        }

        Date nextRun = cal.getTime();
        System.out.println("Next subscriber report: " + nextRun);

        timer.schedule(new TimerTask() {
            @Override public void run() {
                gen.generateLastMonth();
                scheduleNext(); // chain for following month
            }
        }, nextRun);
    }
}
