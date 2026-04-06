package com.example.loginpage.auth;


import com.example.loginpage.config.JwtService;
import com.example.loginpage.entity.Rolle;
import com.example.loginpage.entity.User;
import com.example.loginpage.entity.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final UserRepository repository;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final MailService mailService;

    public AuthenticationResponse register(RegisterRequest request) {
        var existingUser = repository.findByEmail(request.getEmail()).orElse(null);
        if (existingUser != null && existingUser.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        if (!mailService.isConfigured()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Email sending is not configured. Set BREVO_API_KEY or RESEND_API_KEY and MAIL_FROM first."
            );
        }

        var verificationCode = generateVerificationCode();
        var user = existingUser != null ? existingUser : User.builder().rolle(Rolle.USER).build();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(encoder.encode(request.getPassword()));
        user.setRolle(Rolle.USER);
        user.setEnabled(false);
        user.setVerificationCode(verificationCode);
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(10));

        repository.save(user);

        try {
            mailService.sendVerificationCode(user.getEmail(), user.getDisplayName(), verificationCode);
        } catch (Exception exception) {
            log.error(
                    "Failed to send verification email to {} using {}",
                    user.getEmail(),
                    mailService.getActiveProviderName(),
                    exception
            );
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Failed to send verification email. Check your email provider configuration."
            );
        }

        return AuthenticationResponse.builder()
                .username(user.getDisplayName())
                .email(user.getEmail())
                .message("Verification code sent to your email")
                .requiresVerification(true)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        var user = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!user.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Please verify your email first");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .username(user.getDisplayName())
                .email(user.getEmail())
                .message("Logged in successfully")
                .build();
    }

    public AuthenticationResponse verify(VerifyRequest request) {
        var user = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.isEnabled()) {
            var existingToken = jwtService.generateToken(user);
            return AuthenticationResponse.builder()
                    .token(existingToken)
                    .username(user.getDisplayName())
                    .email(user.getEmail())
                    .message("Email already verified")
                    .build();
        }

        if (user.getVerificationCode() == null || user.getVerificationCodeExpiresAt() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No verification code found for this account");
        }

        if (user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Verification code has expired");
        }

        if (!user.getVerificationCode().equals(request.getCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid verification code");
        }

        user.setEnabled(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiresAt(null);
        repository.save(user);

        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .username(user.getDisplayName())
                .email(user.getEmail())
                .message("Email verified successfully")
                .build();
    }

    private String generateVerificationCode() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
    }
}
