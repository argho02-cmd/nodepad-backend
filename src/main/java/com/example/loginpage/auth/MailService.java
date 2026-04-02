package com.example.loginpage.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    public boolean isConfigured() {
        return mailUsername != null && !mailUsername.isBlank()
                && fromAddress != null && !fromAddress.isBlank();
    }

    public void sendVerificationCode(String recipientEmail, String username, String verificationCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(recipientEmail);
        message.setSubject("Your verification code");
        message.setText(String.format(
                "Hello %s,%n%nYour verification code is: %s%n%nThis code expires in 10 minutes.%n",
                username,
                verificationCode
        ));

        mailSender.send(message);
    }
}
