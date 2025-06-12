package mailService;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.util.Properties;

public class MailService {
	public void sendEmail(String to, String body, TypeOfMail typeOfMail) {
		final String username = "bpark.mail.service@gmail.com";
		final String password = "zxusdihmkovcegqu";

		Properties props = new Properties();
		props.put("mail.smtp.host", "smtp.gmail.com"); // host name
		props.put("mail.smtp.port", "587"); // port number
		props.put("mail.smtp.auth", "true"); // need authenticator
		props.put("mail.smtp.starttls.enable", "true"); // enable STARTTLS (encryption protocol)
		props.put("mail.smtp.ssl.protocols", "TLSv1.2"); // version of TLS protocol (for encryption)

		Session session = Session.getInstance(props, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});

		try {
			System.out.println("Try to send email...");
			// bulid the message (the email)
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(username));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
			switch (typeOfMail) {
			case FORGOT_PASSWORD:
				message.setSubject("Your Parking Code");
				message.setText("Helloת\n\nYou requested a code to access your account.\nYor parking code is:\n" + body + "\nIf you did not request this code, please ignore this message.\nFor your security, do not share this code with anyone.\n\nThank you,\n The BPARK team");
				break;
			case LATE:
				message.setSubject("Reminder – Please Remove Your Vehicle from the Parking Lot");
				message.setText("Hello,\n\nThis is a friendly reminder that your allotted parking time has ended.\n Please remove your vehicle as soon as possible to allow access for other users.\n\n"
						+ "If your vehicle has already been moved, please disregard this message.\n\n"
						+ "Thank you for your cooperation,\nThe BPARK Team");
				break;
			}

			// send message
			Transport.send(message);

			System.out.println("Email sent successfully!");

		} catch (Exception e) {
			System.out.println("Error send mail: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
