package com.jobbed.auth.email;

/**
 * Versand transaktionaler E-Mails. Abstrahiert vom konkreten Transport, damit
 * lokal Mailpit und später ein Produktions-SMTP/Provider genutzt werden können.
 */
public interface EmailService {

    void sendVerificationEmail(String to, String firstName, String rawToken);

    void sendPasswordResetEmail(String to, String firstName, String rawToken);

    void sendReminderEmail(String to, String firstName, String title, String description, String actionUrl);
}
