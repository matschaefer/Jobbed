package com.jobbed.auth.email;

import com.jobbed.security.AuthProperties;
import com.jobbed.common.error.exception.EmailDeliveryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * SMTP-basierter E-Mail-Versand (lokal an Mailpit). Versandfehler werden geloggt,
 * aber nicht propagiert, damit z. B. eine Registrierung nicht an einem
 * temporären Mail-Problem scheitert. Token/Links werden nicht geloggt.
 */
@Service
public class SmtpEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailService.class);

    private final JavaMailSender mailSender;
    private final AuthProperties authProperties;
    private final String from;

    public SmtpEmailService(JavaMailSender mailSender,
                            AuthProperties authProperties,
                            @Value("${app.mail.from:no-reply@jobbed.local}") String from) {
        this.mailSender = mailSender;
        this.authProperties = authProperties;
        this.from = from;
    }

    @Override
    public void sendVerificationEmail(String to, String firstName, String rawToken) {
        String link = authProperties.frontendBaseUrl() + "/verify-email?token=" + encode(rawToken);
        String body = "Hallo " + firstName + ",\n\n"
                + "willkommen bei Jobbed! Bitte bestätige deine E-Mail-Adresse über den folgenden Link:\n\n"
                + link + "\n\n"
                + "Der Link ist " + authProperties.verificationTokenTtlMinutes() + " Minuten gültig.\n\n"
                + "Viele Grüße\nDein Jobbed-Team";
        sendRequired(to, "Jobbed – E-Mail bestätigen", body);
    }

    @Override
    public void sendPasswordResetEmail(String to, String firstName, String rawToken) {
        String link = authProperties.frontendBaseUrl() + "/reset-password?token=" + encode(rawToken);
        String body = "Hallo " + firstName + ",\n\n"
                + "du hast das Zurücksetzen deines Passworts angefordert. Über den folgenden Link kannst du "
                + "ein neues Passwort vergeben:\n\n"
                + link + "\n\n"
                + "Der Link ist " + authProperties.resetTokenTtlMinutes() + " Minuten gültig. "
                + "Falls du die Anfrage nicht gestellt hast, ignoriere diese E-Mail.\n\n"
                + "Viele Grüße\nDein Jobbed-Team";
        sendRequired(to, "Jobbed – Passwort zurücksetzen", body);
    }

    @Override
    public void sendReminderEmail(String to, String firstName, String title, String description, String actionUrl) {
        String body = "Hallo " + firstName + ",\n\n" + title + "\n"
                + (description == null ? "" : description + "\n")
                + "\nIn Jobbed öffnen: " + authProperties.frontendBaseUrl() + actionUrl
                + "\n\nViele Grüße\nDein Jobbed-Team";
        sendBestEffort(to, "Jobbed – " + title, body);
    }

    private void sendRequired(String to, String subject, String body) {
        try {
            send(to, subject, body);
            log.debug("E-Mail '{}' an Empfänger versendet.", subject);
        } catch (MailException ex) {
            log.error("Transaktionale E-Mail konnte nicht versendet werden (Betreff='{}').", subject);
            throw new EmailDeliveryException();
        }
    }

    private void sendBestEffort(String to, String subject, String body) {
        try {
            send(to, subject, body);
            log.debug("E-Mail '{}' an Empfänger versendet.", subject);
        } catch (MailException ex) {
            log.warn("Erinnerungs-E-Mail konnte nicht versendet werden (Betreff='{}'): {}",
                    subject, ex.getMessage());
        }
    }

    private void send(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
