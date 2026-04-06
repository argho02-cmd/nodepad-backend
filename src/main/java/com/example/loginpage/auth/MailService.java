package com.example.loginpage.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class MailService {

    private final RestClient resendClient = RestClient.builder()
            .baseUrl("https://api.resend.com")
            .build();

    private final RestClient brevoClient = RestClient.builder()
            .baseUrl("https://api.brevo.com/v3")
            .build();

    @Value("${app.mail.from}")
    private String fromAddress;

    @Value("${app.mail.resend.api-key:}")
    private String resendApiKey;

    @Value("${app.mail.brevo.api-key:}")
    private String brevoApiKey;

    public boolean isConfigured() {
        return fromAddress != null && !fromAddress.isBlank()
                && (isBrevoConfigured() || isResendConfigured());
    }

    public String getActiveProviderName() {
        if (isBrevoConfigured()) {
            return "Brevo";
        }
        if (isResendConfigured()) {
            return "Resend";
        }
        return "email provider";
    }

    public void sendVerificationCode(String recipientEmail, String username, String verificationCode) {
        var messageBody = String.format(
                "Hello %s,%n%nYour verification code is: %s%n%nThis code expires in 10 minutes.%n",
                username,
                verificationCode
        );

        if (isBrevoConfigured()) {
            sendWithBrevo(recipientEmail, username, messageBody);
            return;
        }

        if (isResendConfigured()) {
            sendWithResend(recipientEmail, messageBody);
            return;
        }

        throw new IllegalStateException("No email provider configured");
    }

    private boolean isBrevoConfigured() {
        return brevoApiKey != null && !brevoApiKey.isBlank();
    }

    private boolean isResendConfigured() {
        return resendApiKey != null && !resendApiKey.isBlank();
    }

    private void sendWithBrevo(String recipientEmail, String username, String messageBody) {
        var requestBody = new BrevoEmailRequest(
                new BrevoSender("Secure Notepad", fromAddress),
                new BrevoRecipient[]{new BrevoRecipient(recipientEmail, username)},
                "Your verification code",
                messageBody
        );

        brevoClient.post()
                .uri("/smtp/email")
                .contentType(MediaType.APPLICATION_JSON)
                .header("api-key", brevoApiKey)
                .body(requestBody)
                .retrieve()
                .toBodilessEntity();
    }

    private void sendWithResend(String recipientEmail, String messageBody) {
        var requestBody = new ResendEmailRequest(
                fromAddress,
                recipientEmail,
                "Your verification code",
                messageBody
        );

        resendClient.post()
                .uri("/emails")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + resendApiKey)
                .body(requestBody)
                .retrieve()
                .toBodilessEntity();
    }

    private record BrevoEmailRequest(
            BrevoSender sender,
            BrevoRecipient[] to,
            String subject,
            String textContent
    ) {
    }

    private record BrevoSender(
            String name,
            String email
    ) {
    }

    private record BrevoRecipient(
            String email,
            String name
    ) {
    }

    private record ResendEmailRequest(
            String from,
            String to,
            String subject,
            String text
    ) {
    }
}
