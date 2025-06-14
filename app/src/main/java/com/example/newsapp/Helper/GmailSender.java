package com.example.newsapp.Helper;

import javax.mail.internet.MimeMessage;

public class GmailSender {
    public static final String SENDER_EMAIL = "bonhero1999@gmail.com";
    public static final String SENDER_PASSWORD = "piok zrcj cknp mrho";
    public static final String RECIPIENT_EMAIL = "bonhero1999@gmail.com";
    public static final String GMAIL_HOST = "smtp.gmail.com";


    public void sendMail(MimeMessage mimeMessage) {
        try {
            javax.mail.Transport.send(mimeMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
