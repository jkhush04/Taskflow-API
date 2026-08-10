package com.taskflow.api.service.impl;

import com.taskflow.api.dto.request.TaskRequest;
import com.taskflow.api.dto.request.TaskStatusUpdateRequest;
import com.taskflow.api.dto.response.TaskResponse;
import com.taskflow.api.entity.Project;
import com.taskflow.api.entity.Task;
import com.taskflow.api.entity.User;
import com.taskflow.api.entity.enums.TaskStatus;
import com.taskflow.api.exception.ResourceNotFoundException;
import com.taskflow.api.repository.ProjectRepository;
import com.taskflow.api.repository.TaskRepository;
import com.taskflow.api.repository.UserRepository;
import com.taskflow.api.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public TaskResponse create(TaskRequest request) {
        Project project = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + request.projectId()));

        User assignee = null;
        if (request.assigneeId() != null) {
            assignee = userRepository.findById(request.assigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee not found with id: " + request.assigneeId()));
        }

        Task task = Task.builder()
                .title(request.title())
                .description(request.description())
                .status(request.status() != null ? request.status() : TaskStatus.TODO)
                .deadline(request.deadline())
                .project(project)
                .assignee(assignee)
                .build();

        return TaskResponse.from(taskRepository.save(task));
    }

    @Override
    public TaskResponse getById(Long id) {
        return TaskResponse.from(findOrThrow(id));
    }

    @Override
    public Page<TaskResponse> search(TaskStatus status, Long projectId, Long assigneeId, Pageable pageable) {
        return taskRepository.search(status, projectId, assigneeId, pageable).map(TaskResponse::from);
    }

    @Override
    @Transactional
    public TaskResponse update(Long id, TaskRequest request) {
        Task task = findOrThrow(id);

        Project project = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + request.projectId()));

        User assignee = null;
        if (request.assigneeId() != null) {
            assignee = userRepository.findById(request.assigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee not found with id: " + request.assigneeId()));
        }

        task.setTitle(request.title());
        task.setDescription(request.description());
        if (request.status() != null) {
            task.setStatus(request.status());
        }
        task.setDeadline(request.deadline());
        task.setProject(project);
        task.setAssignee(assignee);
        // Deadline changed -> allow the reminder scheduler to re-evaluate this task.
        task.setReminderSent(false);

        return TaskResponse.from(task);
    }

    @Override
    @Transactional
    public TaskResponse updateStatus(Long id, TaskStatusUpdateRequest request) {
        Task task = findOrThrow(id);
        task.setStatus(request.status());
        return TaskResponse.from(task);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Task task = findOrThrow(id);
        taskRepository.delete(task);
    }

    private Task findOrThrow(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
    }
}
