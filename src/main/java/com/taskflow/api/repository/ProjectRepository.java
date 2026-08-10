package com.taskflow.api.repository;

import com.taskflow.api.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    Page<Project> findByOwnerId(Long ownerId, Pageable pageable);

    boolean existsByIdAndOwnerId(Long id, Long ownerId);
}
