package mailService;
import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.util.Properties;

public class MailService {
    public void sendEmail(String to, String body) {
        final String username = "bpark.mail.service@gmail.com";
        final String password = "zxusdihmkovcegqu";

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com"); //host name
        props.put("mail.smtp.port", "587"); //port number
        props.put("mail.smtp.auth", "true"); //need authenticator
        props.put("mail.smtp.starttls.enable", "true"); //enable STARTTLS (encryption protocol)
        props.put("mail.smtp.ssl.protocols", "TLSv1.2"); //version of TLS protocol (for encryption)
        
        Session session = Session.getInstance(props,
            new Authenticator() {
        	@Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            }
        );

        try {
            System.out.println("Try to send email...");
            //bulid the message (the email)
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to)); 
            message.setSubject("Your parking code");
            message.setText("Hello!\n\nYour parking code is: "+ body+"\n\nBest regards,\nBpark customer team");
            
            //send message
            Transport.send(message);

            System.out.println("Email sent successfully!");

        } catch (Exception e) {
        	System.out.println("Error send mail: "+e.getMessage());
            e.printStackTrace();
        }
    }
}
