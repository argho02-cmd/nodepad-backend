package com.example.loginpage.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class MailService {

    private final RestClient brevoClient = RestClient.builder()
            .baseUrl("https://api.brevo.com/v3")
            .build();

    @Value("${app.mail.from}")
    private String fromAddress;

    @Value("${app.mail.brevo.api-key:}")
    private String brevoApiKey;

    public boolean isConfigured() {
        return fromAddress != null && !fromAddress.isBlank() && isBrevoConfigured();
    }

    public String getActiveProviderName() {
        return "Brevo";
    }

    public void sendVerificationCode(String recipientEmail, String username, String verificationCode) {
        var messageBody = String.format(
                "Hello %s,%n%nYour verification code is: %s%n%nThis code expires in 10 minutes.%n",
                username,
                verificationCode
        );

        if (!isBrevoConfigured()) {
            throw new IllegalStateException("No email provider configured");
        }

        sendWithBrevo(recipientEmail, username, messageBody);
    }

    private boolean isBrevoConfigured() {
        return brevoApiKey != null && !brevoApiKey.isBlank();
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
}
