package com.universal.auth.service;

import com.universal.auth.config.AppProperties;
import com.universal.auth.domain.entities.EmailVerification;
import com.universal.auth.domain.entities.PasswordReset;
import com.universal.auth.domain.entities.User;
import com.universal.auth.repository.EmailVerificationRepository;
import com.universal.auth.repository.PasswordResetRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailVerificationRepository emailVerificationRepository;
    private final PasswordResetRepository passwordResetRepository;
    private final AppProperties appProperties;

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
        String name = user.getFullName() != null ? user.getFullName() : user.getUsername();

        sendHtmlEmail(
                user.getEmail(),
                "Verify your email address",
                buildVerificationEmailHtml(name, link)
        );
    }

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
        String name = user.getFullName() != null ? user.getFullName() : user.getUsername();

        sendHtmlEmail(
                user.getEmail(),
                "Reset your password",
                buildPasswordResetEmailHtml(name, link)
        );
    }

    private void sendHtmlEmail(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(appProperties.getMail().getFrom());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MailException | MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    private String buildVerificationEmailHtml(String name, String link) {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"></head>
                <body style="margin:0;padding:0;background:#f4f4f4;font-family:Arial,sans-serif">
                  <table width="100%%" cellpadding="0" cellspacing="0">
                    <tr><td align="center" style="padding:40px 0">
                      <table width="600" cellpadding="0" cellspacing="0" style="background:#fff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.1)">
                        <tr><td style="background:#4f46e5;padding:32px 40px">
                          <h1 style="margin:0;color:#fff;font-size:24px">Verify your email</h1>
                        </td></tr>
                        <tr><td style="padding:40px">
                          <p style="margin:0 0 16px;color:#374151;font-size:16px">Hello <strong>%s</strong>,</p>
                          <p style="margin:0 0 24px;color:#374151;font-size:16px">
                            Please confirm your email address by clicking the button below.
                            This link expires in <strong>24 hours</strong>.
                          </p>
                          <a href="%s" style="display:inline-block;background:#4f46e5;color:#fff;text-decoration:none;padding:14px 28px;border-radius:6px;font-size:16px;font-weight:bold">
                            Verify Email
                          </a>
                          <p style="margin:24px 0 0;color:#6b7280;font-size:14px">
                            Or copy this link into your browser:<br>
                            <a href="%s" style="color:#4f46e5;word-break:break-all">%s</a>
                          </p>
                          <p style="margin:24px 0 0;color:#6b7280;font-size:14px">
                            If you did not create an account, you can safely ignore this email.
                          </p>
                        </td></tr>
                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """.formatted(name, link, link, link);
    }

    private String buildPasswordResetEmailHtml(String name, String link) {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"></head>
                <body style="margin:0;padding:0;background:#f4f4f4;font-family:Arial,sans-serif">
                  <table width="100%%" cellpadding="0" cellspacing="0">
                    <tr><td align="center" style="padding:40px 0">
                      <table width="600" cellpadding="0" cellspacing="0" style="background:#fff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.1)">
                        <tr><td style="background:#dc2626;padding:32px 40px">
                          <h1 style="margin:0;color:#fff;font-size:24px">Reset your password</h1>
                        </td></tr>
                        <tr><td style="padding:40px">
                          <p style="margin:0 0 16px;color:#374151;font-size:16px">Hello <strong>%s</strong>,</p>
                          <p style="margin:0 0 24px;color:#374151;font-size:16px">
                            We received a request to reset your password.
                            Click the button below — this link expires in <strong>1 hour</strong>.
                          </p>
                          <a href="%s" style="display:inline-block;background:#dc2626;color:#fff;text-decoration:none;padding:14px 28px;border-radius:6px;font-size:16px;font-weight:bold">
                            Reset Password
                          </a>
                          <p style="margin:24px 0 0;color:#6b7280;font-size:14px">
                            Or copy this link into your browser:<br>
                            <a href="%s" style="color:#dc2626;word-break:break-all">%s</a>
                          </p>
                          <p style="margin:24px 0 0;color:#6b7280;font-size:14px">
                            If you did not request a password reset, please ignore this email.
                            Your password will not change.
                          </p>
                        </td></tr>
                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """.formatted(name, link, link, link);
    }
}
