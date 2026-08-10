package com.taskflow.api.dto.request;

import com.taskflow.api.entity.enums.TaskStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record TaskRequest(

        @NotBlank
        @Size(max = 200)
        String title,

        @Size(max = 2000)
        String description,

        TaskStatus status,

        @NotNull
        @Future(message = "Deadline must be in the future")
        Instant deadline,

        @NotNull(message = "projectId is required")
        Long projectId,

        Long assigneeId
) {
}
