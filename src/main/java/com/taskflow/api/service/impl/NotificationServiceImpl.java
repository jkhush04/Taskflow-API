package com.taskflow.api.service.impl;

import com.taskflow.api.entity.Task;
import com.taskflow.api.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Stub implementation: logs the reminder instead of sending real email.
 * Replace with a JavaMailSender / SES / SendGrid client for production use -
 * the scheduler and interface contract stay unchanged.
 */
@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    @Override
    public void sendDeadlineReminder(Task task) {
        String recipient = task.getAssignee() != null ? task.getAssignee().getEmail() : "unassigned";
        log.info("[REMINDER EMAIL STUB] To: {} | Task #{} '{}' in project '{}' is due at {}",
                recipient, task.getId(), task.getTitle(), task.getProject().getName(), task.getDeadline());
    }
}
