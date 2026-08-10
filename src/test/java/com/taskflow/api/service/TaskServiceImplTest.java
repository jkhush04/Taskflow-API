package com.taskflow.api.service;

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
import com.taskflow.api.service.impl.TaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pure unit tests for TaskServiceImpl. All collaborators are mocked with Mockito so these
 * run fast, with no Spring context and no database.
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    private Project project;
    private User assignee;
    private Task task;

    @BeforeEach
    void setUp() {
        project = Project.builder().id(1L).name("TaskFlow Launch").build();
        assignee = User.builder().id(2L).fullName("Jane Doe").email("jane@taskflow.dev").build();

        task = Task.builder()
                .id(10L)
                .title("Write API docs")
                .description("Document all endpoints")
                .status(TaskStatus.TODO)
                .deadline(Instant.now().plus(3, ChronoUnit.DAYS))
                .project(project)
                .assignee(assignee)
                .build();
    }

    @Test
    void create_shouldPersistAndReturnMappedResponse_whenProjectAndAssigneeExist() {
        TaskRequest request = new TaskRequest(
                "Write API docs", "Document all endpoints", TaskStatus.TODO,
                Instant.now().plus(3, ChronoUnit.DAYS), 1L, 2L);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findById(2L)).thenReturn(Optional.of(assignee));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        TaskResponse response = taskService.create(request);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.title()).isEqualTo("Write API docs");
        assertThat(response.projectId()).isEqualTo(1L);
        assertThat(response.assigneeId()).isEqualTo(2L);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void create_shouldThrowResourceNotFound_whenProjectDoesNotExist() {
        TaskRequest request = new TaskRequest(
                "Write API docs", "desc", TaskStatus.TODO, Instant.now().plus(1, ChronoUnit.DAYS), 99L, null);

        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Project not found");

        verify(taskRepository, never()).save(any());
    }

    @Test
    void getById_shouldThrowResourceNotFound_whenTaskMissing() {
        when(taskRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getById(404L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("404");
    }

    @Test
    void updateStatus_shouldMutateAndReturnUpdatedStatus() {
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));

        TaskResponse response = taskService.updateStatus(10L, new TaskStatusUpdateRequest(TaskStatus.IN_PROGRESS));

        assertThat(response.status()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @Test
    void delete_shouldRemoveTask_whenItExists() {
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));

        taskService.delete(10L);

        verify(taskRepository, times(1)).delete(task);
    }

    @Test
    void delete_shouldThrow_whenTaskDoesNotExist() {
        when(taskRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.delete(10L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(taskRepository, never()).delete(any());
    }
}
