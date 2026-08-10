package com.taskflow.api.dto.response;

import com.taskflow.api.entity.Project;
import com.taskflow.api.entity.enums.ProjectStatus;

import java.time.LocalDate;

public record ProjectResponse(
        Long id,
        String name,
        String description,
        ProjectStatus status,
        LocalDate startDate,
        LocalDate endDate,
        Long ownerId,
        String ownerName,
        int taskCount
) {
    public static ProjectResponse from(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getStatus(),
                project.getStartDate(),
                project.getEndDate(),
                project.getOwner().getId(),
                project.getOwner().getFullName(),
                project.getTasks() == null ? 0 : project.getTasks().size()
        );
    }
}
