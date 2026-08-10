package com.taskflow.api.scheduler;

import com.taskflow.api.entity.Task;
import com.taskflow.api.repository.TaskRepository;
import com.taskflow.api.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Background job that finds tasks whose deadline falls within the next 24 hours and haven't
 * already been reminded about, then dispatches a reminder for each and flags it as sent so the
 * next run doesn't duplicate the notification.
 *
 * Runs every hour by default (see application.yml: taskflow.scheduler.reminder-cron), which is
 * frequent enough to catch anything entering the 24h window without hammering the database.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskDeadlineReminderScheduler {

    private final TaskRepository taskRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "${taskflow.scheduler.reminder-cron:0 0 * * * *}")
    @Transactional
    public void sendUpcomingDeadlineReminders() {
        Instant now = Instant.now();
        Instant windowEnd = now.plus(Duration.ofHours(24));

        List<Task> tasksDueSoon = taskRepository.findTasksExpiringSoon(now, windowEnd);

        if (tasksDueSoon.isEmpty()) {
            log.debug("Deadline reminder sweep: no tasks due within the next 24 hours.");
            return;
        }

        log.info("Deadline reminder sweep: found {} task(s) due within 24 hours.", tasksDueSoon.size());

        for (Task task : tasksDueSoon) {
            try {
                notificationService.sendDeadlineReminder(task);
                task.setReminderSent(true);
            } catch (Exception ex) {
                // Don't let one bad notification abort the whole sweep; log and continue.
                log.error("Failed to send deadline reminder for task #{}: {}", task.getId(), ex.getMessage(), ex);
            }
        }
    }
}
