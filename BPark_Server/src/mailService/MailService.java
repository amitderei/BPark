package mailService;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.util.Properties;

/**
 * Service class responsible for sending emails through Gmail SMTP.
 * Used by the system to notify subscribers in different situations
 * such as password recovery or overdue parking.
 */
public class MailService {

    /**
     * Sends an email to a given address based on the specified message type.
     *
     * @param to          the recipient email address
     * @param body        the content of the email (or parking code)
     * @param typeOfMail  the type of message to send (FORGOT_PASSWORD, LATE, etc.)
     */
    public void sendEmail(String to, String body, TypeOfMail typeOfMail) {
        final String username = "bpark.mail.service@gmail.com"; // system email
        final String password = "zxusdihmkovcegqu"; // app password

        // Set up mail properties for Gmail SMTP
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");               // SMTP host
        props.put("mail.smtp.port", "587");                          // TLS port
        props.put("mail.smtp.auth", "true");                         // requires authentication
        props.put("mail.smtp.starttls.enable", "true");              // enable TLS encryption
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");             // specify TLS version

        // Create an authenticated session using username and password
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            System.out.println("Try to send email...");

            // Build the email message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));

            // Set message content based on the type of mail
            switch (typeOfMail) {
                case FORGOT_PASSWORD:
                    message.setSubject("Your Parking Code");
                    message.setText("Hello,\n\nYou requested a code to access your account.\n"
                            + "Your parking code is:\n" + body + "\n\n"
                            + "If you did not request this code, please ignore this message.\n"
                            + "For your security, do not share this code with anyone.\n\n"
                            + "Thank you,\nThe BPARK team");
                    break;

                case LATE:
                    message.setSubject("Reminder – Please Remove Your Vehicle from the Parking Lot");
                    message.setText("Hello,\n\nThis is a friendly reminder that your allotted parking time has ended.\n"
                            + "Please remove your vehicle as soon as possible to allow access for other users.\n\n"
                            + "If your vehicle has already been moved, please disregard this message.\n\n"
                            + "Thank you for your cooperation,\nThe BPARK Team");
                    break;

                case GENERIC_MESSAGE:
                    message.setSubject("BPARK Notification");
                    message.setText(body);
                    break;
            }

            // Send the email
            Transport.send(message);

            System.out.println("Email sent successfully!");

        } catch (Exception e) {
            System.out.println("Error send mail: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
