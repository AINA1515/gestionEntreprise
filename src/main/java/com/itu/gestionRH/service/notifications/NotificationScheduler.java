package com.itu.gestionRH.service.notifications;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.notifications.scheduler", name = "enabled", havingValue = "true", matchIfMissing = true)
public class NotificationScheduler {
    private final NotificationService notifications;
    private final NotificationReminderService reminders;

    public NotificationScheduler(NotificationService notifications, NotificationReminderService reminders) {
        this.notifications=notifications; this.reminders=reminders;
    }

    @Scheduled(fixedDelayString = "${app.notifications.scheduler.delay-ms:15000}")
    public void envoyerOutbox() { notifications.envoyerEcheances(50); }

    @Scheduled(cron = "${app.notifications.reminders.cron:0 0 8 * * *}")
    public void programmerRappels() { reminders.programmerRappels(); }
}
