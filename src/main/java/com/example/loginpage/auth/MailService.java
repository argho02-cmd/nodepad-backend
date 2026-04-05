package com.example.loginpage.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class MailService {

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://api.resend.com")
            .build();

    @Value("${app.mail.from}")
    private String fromAddress;

    @Value("${app.mail.resend.api-key:}")
    private String resendApiKey;

    public boolean isConfigured() {
        return resendApiKey != null && !resendApiKey.isBlank()
                && fromAddress != null && !fromAddress.isBlank();
    }

    public void sendVerificationCode(String recipientEmail, String username, String verificationCode) {
        var requestBody = new ResendEmailRequest(
                fromAddress,
                recipientEmail,
                "Your verification code",
                String.format(
                        "Hello %s,%n%nYour verification code is: %s%n%nThis code expires in 10 minutes.%n",
                        username,
                        verificationCode
                )
        );

        restClient.post()
                .uri("/emails")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + resendApiKey)
                .body(requestBody)
                .retrieve()
                .toBodilessEntity();
    }

    private record ResendEmailRequest(
            String from,
            String to,
            String subject,
            String text
    ) {
    }
}
