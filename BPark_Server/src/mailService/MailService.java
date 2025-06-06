package mailService;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;
import jakarta.mail.internet.InternetAddress;

public class MailService {
	private static final String address = "bpark@outlook.co.il";
	private static final String password = "areml123!";

	public static void sendEmail(String to, String body){
		System.out.println("forgotMyParkingCode-mail");
		try {
		//Outlook SMTP settings
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.office365.com");
		props.put("mail.smtp.port", "587");
		
		Session session=Session.getInstance(props, new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(address, password);
			}
		});
		
		Message msg=new MimeMessage(session);
		msg.setFrom(new InternetAddress(address));
		msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
		msg.setSubject("Your parking code");
		msg.setText("Hello,\n Your parking code is: "+body+".\nBest regards,\nBpark Customer Team ");
		
		//send the message
		Transport.send(msg);
		
		System.out.println("Email was send successfully!");
		}
		catch(Exception e) {
			System.out.println("error: "+e.getMessage());
			 e.printStackTrace();
		}
	}
}
