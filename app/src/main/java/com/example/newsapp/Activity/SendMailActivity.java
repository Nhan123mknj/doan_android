package com.example.newsapp.Activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.newsapp.Helper.GmailSender;
import com.example.newsapp.R;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


public class SendMailActivity extends AppCompatActivity {

    ImageButton btnBack;
    Button btnSendMail;
    EditText email;
    EditText edtContent,edtTitle;

    TextView titleError, contentError, gmailError;
    String e_email, e_content, e_title;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_mail);
        initView();

        btnSendMail.setOnClickListener(v -> {
            changeText();
            sendMail(e_email, e_content, e_title);
        });

        btnBack.setOnClickListener(view -> finish());
    }

    private void sendMail(String eEmail, String eContent, String eTitle) {
        checkError(eEmail, eContent, eTitle);
        if (eEmail.isEmpty() || eContent.isEmpty() || eTitle.isEmpty()) {
            return;
        }
        new Thread(() -> {
            try {
                Properties properties = System.getProperties();
                properties.put("mail.smtp.host", GmailSender.GMAIL_HOST);
                properties.put("mail.smtp.port", "465");
                properties.put("mail.smtp.auth", "true");
                properties.put("mail.smtp.ssl.enable", "true");

                Session session = Session.getInstance(properties, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(GmailSender.SENDER_EMAIL, GmailSender.SENDER_PASSWORD);
                    }
                });
                MimeMessage mimeMessage = new MimeMessage(session);
                mimeMessage.setFrom(new InternetAddress(eEmail));
                mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(GmailSender.RECIPIENT_EMAIL));
                mimeMessage.setSubject(eTitle);
                mimeMessage.setText(eContent);
                new GmailSender().sendMail(mimeMessage);
                runOnUiThread(() -> Snackbar.make(btnSendMail, "Gửi mail thành công", Snackbar.LENGTH_LONG).show());
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Snackbar.make(btnSendMail, "Gửi mail thất bại", Snackbar.LENGTH_LONG).show());
            }
        }).start();
    }

    private void checkError(String eEmail, String eContent, String eTitle) {
        if (eEmail.isEmpty()) {
            gmailError.setVisibility(TextView.VISIBLE);
            return;
        } else {
            gmailError.setVisibility(TextView.GONE);
        }

        if (eContent.isEmpty()) {
            contentError.setVisibility(TextView.VISIBLE);
            return;
        } else {
            contentError.setVisibility(TextView.GONE);
        }

        if (eTitle.isEmpty()) {
            titleError.setVisibility(TextView.VISIBLE);
            return;
        } else {
            titleError.setVisibility(TextView.GONE);
        }

    }

    private void changeText() {
        e_email = email.getText().toString();
        e_content = edtContent.getText().toString();
        e_title = edtTitle.getText().toString();

    }

    private void initView() {
        btnBack = findViewById(R.id.btnBack);
        btnSendMail = findViewById(R.id.btnChangePass);
        email = findViewById(R.id.email);
        edtContent = findViewById(R.id.edtContent);
        gmailError = findViewById(R.id.gmailError);
        contentError = findViewById(R.id.contentError);
        titleError = findViewById(R.id.titleError);
        edtTitle = findViewById(R.id.edtTitle);
    }


}