package com.taskflow.api.service;

import com.taskflow.api.entity.Task;

public interface NotificationService {

    /**
     * Sends (or, in this stub, logs) a deadline-reminder notification for the given task
     * to its assignee. Swap the implementation for a real mail/SMS/Slack integration later
     * without touching the scheduler that calls it.
     */
    void sendDeadlineReminder(Task task);
}
