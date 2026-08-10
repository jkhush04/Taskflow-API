package com.taskflow.api.controller;

import com.taskflow.api.dto.request.TaskRequest;
import com.taskflow.api.dto.request.TaskStatusUpdateRequest;
import com.taskflow.api.dto.response.TaskResponse;
import com.taskflow.api.entity.enums.TaskStatus;
import com.taskflow.api.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getById(id));
    }

    /**
     * GET /api/tasks?status=IN_PROGRESS&projectId=1&assigneeId=5&page=0&size=20&sort=deadline,asc
     * Filtering (status/project/assignee), sorting (e.g. by deadline) and pagination are all
     * driven through query parameters and Spring's Pageable binding.
     */
    @GetMapping
    public ResponseEntity<Page<TaskResponse>> search(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long assigneeId,
            @PageableDefault(size = 20, sort = "deadline") Pageable pageable) {
        return ResponseEntity.ok(taskService.search(status, projectId, assigneeId, pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> update(@PathVariable Long id,
                                                 @Valid @RequestBody TaskRequest request) {
        return ResponseEntity.ok(taskService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskResponse> updateStatus(@PathVariable Long id,
                                                        @Valid @RequestBody TaskStatusUpdateRequest request) {
        return ResponseEntity.ok(taskService.updateStatus(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
