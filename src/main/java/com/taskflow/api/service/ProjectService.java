package com.taskflow.api.service;

import com.taskflow.api.dto.request.ProjectRequest;
import com.taskflow.api.dto.response.ProjectResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProjectService {

    ProjectResponse create(ProjectRequest request, String ownerEmail);

    ProjectResponse getById(Long id);

    Page<ProjectResponse> getAll(Pageable pageable);

    ProjectResponse update(Long id, ProjectRequest request);

    void delete(Long id);
}
