package com.taskflow.api.service;

import com.taskflow.api.dto.request.TaskRequest;
import com.taskflow.api.dto.request.TaskStatusUpdateRequest;
import com.taskflow.api.dto.response.TaskResponse;
import com.taskflow.api.entity.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskService {

    TaskResponse create(TaskRequest request);

    TaskResponse getById(Long id);

    /** Filtering by status/project/assignee, sorting by deadline (or any field) and pagination all flow through Pageable. */
    Page<TaskResponse> search(TaskStatus status, Long projectId, Long assigneeId, Pageable pageable);

    TaskResponse update(Long id, TaskRequest request);

    TaskResponse updateStatus(Long id, TaskStatusUpdateRequest request);

    void delete(Long id);
}
