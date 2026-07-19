package com.jobbed.auth.email;

import com.jobbed.common.error.exception.EmailDeliveryException;
import com.jobbed.security.AuthProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class SmtpEmailServiceTest {

    @Mock JavaMailSender mailSender;

    SmtpEmailService service;

    @BeforeEach
    void setUp() {
        AuthProperties properties = new AuthProperties(
                "test-secret-1234567890-abcdefghijklmnop",
                900, 604800, 1440, 30, "https://jobbed.example.com",
                new AuthProperties.Cookie("refreshToken", "/api/v1/auth", true, "Strict"));
        service = new SmtpEmailService(mailSender, properties, "no-reply@jobbed.example.com");
    }

    @Test
    void verificationMailFailureIsReported() {
        doThrow(new MailSendException("SMTP unavailable"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        assertThatThrownBy(() -> service.sendVerificationEmail("user@example.com", "Max", "token"))
                .isInstanceOf(EmailDeliveryException.class);
    }

    @Test
    void reminderMailFailureDoesNotBreakScheduler() {
        doThrow(new MailSendException("SMTP unavailable"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        assertThatCode(() -> service.sendReminderEmail(
                "user@example.com", "Max", "Interview", null, "/app/calendar"))
                .doesNotThrowAnyException();
    }
}
