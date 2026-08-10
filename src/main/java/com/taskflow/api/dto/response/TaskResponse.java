package com.taskflow.api.dto.response;

import com.taskflow.api.entity.Task;
import com.taskflow.api.entity.enums.TaskStatus;

import java.time.Instant;

public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskStatus status,
        Instant deadline,
        Long projectId,
        String projectName,
        Long assigneeId,
        String assigneeName
) {
    public static TaskResponse from(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getDeadline(),
                task.getProject().getId(),
                task.getProject().getName(),
                task.getAssignee() != null ? task.getAssignee().getId() : null,
                task.getAssignee() != null ? task.getAssignee().getFullName() : null
        );
    }
}
