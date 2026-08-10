package com.taskflow.api.repository;

import com.taskflow.api.entity.Task;
import com.taskflow.api.entity.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Core filtering/sorting/pagination query behind GET /api/tasks.
     * status is optional (null = no filter); sorting (e.g. by deadline) comes from the Pageable itself.
     */
    @Query("""
            SELECT t FROM Task t
            WHERE (:status IS NULL OR t.status = :status)
              AND (:projectId IS NULL OR t.project.id = :projectId)
              AND (:assigneeId IS NULL OR t.assignee.id = :assigneeId)
            """)
    Page<Task> search(@Param("status") TaskStatus status,
                       @Param("projectId") Long projectId,
                       @Param("assigneeId") Long assigneeId,
                       Pageable pageable);

    Page<Task> findByProjectId(Long projectId, Pageable pageable);

    /**
     * Used by the reminder scheduler: tasks not yet DONE, deadline falling inside the given window,
     * and not already flagged as reminded. Backed by idx_tasks_deadline / idx_tasks_reminder_sent.
     */
    @Query("""
            SELECT t FROM Task t
            WHERE t.reminderSent = false
              AND t.status <> com.taskflow.api.entity.enums.TaskStatus.DONE
              AND t.deadline BETWEEN :now AND :windowEnd
            """)
    List<Task> findTasksExpiringSoon(@Param("now") Instant now, @Param("windowEnd") Instant windowEnd);
}
