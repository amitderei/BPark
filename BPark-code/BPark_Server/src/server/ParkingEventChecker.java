package server;

import java.util.List;

import common.ParkingEvent;
import db.DBController;
import mailService.MailService;
import mailService.TypeOfMail;

/**
 * A background thread that checks for overdue parking sessions.
 * Its job is to find all active parkings that are marked as "late"
 * and haven't yet received an email, send them a late notice,
 * and mark them as notified in the database.
 */
public class ParkingEventChecker extends Thread {

    /** Database controller used to query and update parking data */
    private DBController db = new DBController();

    /** Mail service used to send notifications to subscribers */
    private MailService mailService = new MailService();

    /**
     * Starts the thread. This method runs in a loop and performs the following:
     * - Checks for late parking events that haven't received a notification
     * - Sends an email and SMS to the subscriber
     * - Updates the DB to mark that a message has been sent
     * - Waits 5 minutes and repeats
     */
    @Override
    public void run() {
        while (true) {
            try {
                // Get list of late parkings that haven't been notified yet
                List<ParkingEvent> sendEmailList = db.getActiveParkingEventsThatLateAndDoesntReceiveMail();


                // Go through each late parking and send notification
                for (int i = 0; i < sendEmailList.size(); i++) {
                    // Get email and phone number of the subscriber
                    String[] getEmailAndPhoneNumberOfLating = db.getEmailAndPhoneNumber(
                            sendEmailList.get(i).getSubscriberCode());

                    String email = getEmailAndPhoneNumberOfLating[0];
                    String phoneNum = getEmailAndPhoneNumberOfLating[1];


                    // Send late notification
                    mailService.sendEmail(email, phoneNum, TypeOfMail.LATE);

                    // Mark in DB that email has been sent
                    db.markSendMail(sendEmailList.get(i).getSubscriberCode());
                }

                // Wait 5 minutes before checking again
                Thread.sleep(5 * 60 * 1000);

            } catch (Exception e) {
                try {
                    // In case of error, wait 1 minute before retrying
                    Thread.sleep(60 * 1000);
                } catch (Exception ex) {
                    System.out.println("Error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
}
