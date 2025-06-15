package server;

import java.util.List;

import common.ParkingEvent;
import db.DBController;
import mailService.MailService;
import mailService.TypeOfMail;

public class ParkingEventChecker extends Thread{
	private DBController db=new DBController();
	private MailService mailService=new MailService();
	@Override
	public void run() {
		
		while (true) {
			try {
				List<ParkingEvent> sendEmailList= db.getActiveParkingEventsThatLateAndDoesntReceiveMail();
				System.out.println("new thread created");
				for(int i=0;i<sendEmailList.size(); i++) {
					String [] getEmailAndPhoneNumberOfLating=db.getEmailAndPhoneNumber(sendEmailList.get(i).getSubscriberCode());
					String email=getEmailAndPhoneNumberOfLating[0];
					String phoneNum= getEmailAndPhoneNumberOfLating[1];
					System.out.println("new email is created");
					mailService.sendEmail(email, phoneNum, TypeOfMail.LATE);
					db.markSendMail(sendEmailList.get(i).getSubscriberCode());
				}
				Thread.sleep(5 * 60 * 1000);
			}catch(Exception e) {
				try {
                    Thread.sleep(60 * 1000); 
                    } catch (Exception ex) {
                    	System.out.println("Error: " +e.getMessage());
                    	e.getStackTrace();
                    }
			}
		}
	}
}