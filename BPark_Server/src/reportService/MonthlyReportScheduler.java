package reportService;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MonthlyReportScheduler {
	private static MonthlyReportGenarator monthlyReportGenarator=new MonthlyReportGenarator();
	private static Timer timer=new Timer();
	public static void start() {
		monthlyReportGenarator.generatePastReport();
		scheduleNext();
	}

	private static void scheduleNext() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);

		if (calendar.getTime().before(new Date())) {
			calendar.add(Calendar.MONTH, 1);
		}

		Date nextRun = calendar.getTime();
        System.out.println("Next report scheduled for: " + nextRun);

        timer.schedule(new TimerTask() {
            public void run() {
                monthlyReportGenarator.generateNewReport(); 
                scheduleNext(); 
            }
        }, nextRun);
	}
}
