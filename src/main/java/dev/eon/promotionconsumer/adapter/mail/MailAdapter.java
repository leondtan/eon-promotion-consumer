package dev.eon.promotionconsumer.adapter.mail;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class MailAdapter {

    private Session _session;
    private String from;

    public MailAdapter(String host, String username, String password) {
        Properties properties = System.getProperties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.auth", "true");

        _session = Session.getInstance(properties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        this.from = username;

        _session.setDebug(false);
    }

    public void sendPromotionEmail(
            String recipientAddress,
            String name,
            String title,
            String body
    ) {
        try {
            MimeMessage message = new MimeMessage(_session);

            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipientAddress));
            message.setSubject(title);
            message.setContent(
                    "Hi, <b>" + name + "</b><br><br>" + body,
                    "text/html"
            );
            Transport.send(message);
        } catch (MessagingException mex) {
            mex.printStackTrace();
        }

    }

}
