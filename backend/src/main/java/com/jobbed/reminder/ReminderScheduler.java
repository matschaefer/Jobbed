package com.jobbed.reminder;

import com.jobbed.application.JobApplicationRepository;
import com.jobbed.auth.email.EmailService;
import com.jobbed.notification.NotificationService;
import com.jobbed.notification.NotificationType;
import com.jobbed.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;

@Component
public class ReminderScheduler {
    private static final Logger log = LoggerFactory.getLogger(ReminderScheduler.class);
    private final ReminderRepository reminders;
    private final UserRepository users;
    private final JobApplicationRepository applications;
    private final NotificationService notifications;
    private final EmailService email;

    public ReminderScheduler(ReminderRepository reminders, UserRepository users,
            JobApplicationRepository applications, NotificationService notifications, EmailService email) {
        this.reminders = reminders; this.users = users; this.applications = applications;
        this.notifications = notifications; this.email = email;
    }

    @Scheduled(fixedDelayString = "${app.scheduler.reminder-delay-ms:60000}",
               initialDelayString = "${app.scheduler.reminder-initial-delay-ms:15000}")
    @Transactional
    public void sendDueReminders() {
        Instant now = Instant.now();
        for (Reminder r : reminders.lockDue(now, PageRequest.of(0, 50))) {
            var user = users.findById(r.getUserId()).orElse(null);
            if (user == null) continue;
            String action = r.getApplicationId() != null ? "/app/applications/" + r.getApplicationId() : "/app/calendar";
            notifications.create(r.getUserId(), r.getReminderType() == ReminderType.INTERVIEW
                    ? NotificationType.INTERVIEW : NotificationType.REMINDER,
                    r.getTitle(), r.getDescription() != null ? r.getDescription() : "Deine Erinnerung ist fällig.",
                    action, "reminder:" + r.getId());
            email.sendReminderEmail(user.getEmail(), user.getFirstName(), r.getTitle(), r.getDescription(), action);
            r.setSent(true); r.setSentAt(now);
            log.debug("Erinnerung {} einmalig verarbeitet.", r.getId());
        }
    }

    @Scheduled(cron = "${app.scheduler.deadline-cron:0 5 7 * * *}")
    @Transactional
    public void createDeadlineWarnings() {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        for (var app : applications.findByDeadlineBetween(today, today.plusDays(2))) {
            long days = app.getDeadline().toEpochDay() - today.toEpochDay();
            String title = days == 0 ? "Deadline heute" : "Deadline in " + days + " Tagen";
            notifications.create(app.getUserId(), NotificationType.DEADLINE, title,
                    app.getJobTitle() + " bei " + app.getCompany().getName(), "/app/applications/" + app.getId(),
                    "deadline:" + app.getId() + ":" + app.getDeadline());
        }
    }
}
