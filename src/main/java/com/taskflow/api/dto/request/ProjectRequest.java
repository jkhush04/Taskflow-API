package com.taskflow.api.dto.request;

import com.taskflow.api.entity.enums.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ProjectRequest(

        @NotBlank
        @Size(max = 150)
        String name,

        @Size(max = 2000)
        String description,

        ProjectStatus status,

        LocalDate startDate,

        LocalDate endDate
) {
}
