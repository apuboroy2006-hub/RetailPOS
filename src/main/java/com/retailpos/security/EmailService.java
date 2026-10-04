package com.retailpos.security;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;


public class EmailService {

    private final String username;
    private final String password;

public EmailService() {

    Properties config =
            new Properties();

    try (
            FileInputStream input =
                    new FileInputStream(
                            "config/smtp.properties"
                    )
    ) {

        config.load(input);

    } catch (IOException e) {

        throw new IllegalStateException(
                "Unable to load SMTP configuration.",
                e
        );
    }

    username =
            config.getProperty(
                    "SMTP_USERNAME"
            );

    password =
            config.getProperty(
                    "SMTP_PASSWORD"
            );

    if (username == null || username.isBlank()) {

        throw new IllegalStateException(
                "SMTP_USERNAME is not configured."
        );
    }

    if (password == null || password.isBlank()) {

        throw new IllegalStateException(
                "SMTP_PASSWORD is not configured."
        );
    }
}
public EmailService(
        String username,
        String password
) {

    this.username = username;
    this.password = password;

    if (username == null || username.isBlank()) {
        throw new IllegalStateException(
                "SMTP username is required."
        );
    }

    if (password == null || password.isBlank()) {
        throw new IllegalStateException(
                "SMTP password is required."
        );
    }
}

    public void sendOtpEmail(
            String recipientEmail,
            String username,
            String otp
    ) {

        try {

            Properties properties = new Properties();

            properties.put(
                    "mail.smtp.host",
                    "smtp.gmail.com"
            );

            properties.put(
                    "mail.smtp.port",
                    "587"
            );

            properties.put(
                    "mail.smtp.auth",
                    "true"
            );

            properties.put(
                    "mail.smtp.starttls.enable",
                    "true"
            );

            Session session =
                    Session.getInstance(
                            properties,
                            new Authenticator() {

                                @Override
                                protected PasswordAuthentication
                                getPasswordAuthentication() {

                                    return new PasswordAuthentication(
                                            EmailService.this.username,
                                            EmailService.this.password
                                    );
                                }
                            }
                    );

            Message message =
                    new MimeMessage(session);

           message.setFrom(
        new InternetAddress(EmailService.this.username)
);

            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(recipientEmail)
            );

            message.setSubject(
                    "Retail POS - Device Verification OTP"
            );

            message.setText(
                    "Hello " + username + ",\n\n"
                    + "Your Retail POS verification OTP is:\n\n"
                    + otp + "\n\n"
                    + "This OTP is valid for 5 minutes.\n\n"
                    + "If you did not attempt to log in, "
                    + "please ignore this email.\n\n"
                    + "Retail POS Security"
            );

            Transport.send(message);

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to send OTP email.",
                    e
            );
        }
    }
}