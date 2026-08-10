package com.taskflow.api.dto.request;

import com.taskflow.api.entity.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record TaskStatusUpdateRequest(
        @NotNull TaskStatus status
) {
}
