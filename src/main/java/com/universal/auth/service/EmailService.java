package com.universal.auth.service;

import com.universal.auth.config.AppProperties;
import com.universal.auth.domain.entities.EmailVerification;
import com.universal.auth.domain.entities.PasswordReset;
import com.universal.auth.domain.entities.User;
import com.universal.auth.repository.EmailVerificationRepository;
import com.universal.auth.repository.PasswordResetRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailVerificationRepository emailVerificationRepository;
    private final PasswordResetRepository passwordResetRepository;
    private final AppProperties appProperties;

    /**
     * Sends an email verification link to the user.
     * Any previous unused tokens for that user are invalidated first.
     */
    @Transactional
    public void sendVerificationEmail(User user) {
        emailVerificationRepository.invalidatePreviousTokens(user.getUserId());

        String token = UUID.randomUUID().toString();

        EmailVerification verification = new EmailVerification();
        verification.setUser(user);
        verification.setToken(token);
        verification.setExpiresAt(LocalDateTime.now().plusHours(24));
        verification.setIsUsed(false);
        verification.setCreatedAt(LocalDateTime.now());
        emailVerificationRepository.save(verification);

        String link = appProperties.getMail().getVerificationBaseUrl() + "?token=" + token;
        sendEmail(
                user.getEmail(),
                "Verify your email address",
                "Hello " + user.getFullName() + ",\n\n"
                        + "Please verify your email address by clicking the link below:\n"
                        + link + "\n\n"
                        + "This link expires in 24 hours.\n\n"
                        + "If you did not create an account, you can safely ignore this email."
        );
    }

    /**
     * Sends a password reset link to the user.
     * Any previous unused reset tokens are invalidated first.
     */
    @Transactional
    public void sendPasswordResetEmail(User user) {
        passwordResetRepository.invalidatePreviousTokens(user.getUserId());

        String token = UUID.randomUUID().toString();

        PasswordReset reset = new PasswordReset();
        reset.setUser(user);
        reset.setResetToken(token);
        reset.setExpiresAt(LocalDateTime.now().plusHours(1));
        reset.setIsUsed(false);
        passwordResetRepository.save(reset);

        String link = appProperties.getMail().getResetBaseUrl() + "?token=" + token;
        sendEmail(
                user.getEmail(),
                "Reset your password",
                "Hello " + user.getFullName() + ",\n\n"
                        + "We received a request to reset your password. Click the link below:\n"
                        + link + "\n\n"
                        + "This link expires in 1 hour.\n\n"
                        + "If you did not request a password reset, please ignore this email."
        );
    }

    private void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(appProperties.getMail().getFrom());
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
}
