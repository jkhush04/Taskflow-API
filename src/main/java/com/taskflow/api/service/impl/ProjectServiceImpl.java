package com.taskflow.api.service.impl;

import com.taskflow.api.dto.request.ProjectRequest;
import com.taskflow.api.dto.response.ProjectResponse;
import com.taskflow.api.entity.Project;
import com.taskflow.api.entity.User;
import com.taskflow.api.entity.enums.ProjectStatus;
import com.taskflow.api.exception.ResourceNotFoundException;
import com.taskflow.api.repository.ProjectRepository;
import com.taskflow.api.repository.UserRepository;
import com.taskflow.api.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ProjectResponse create(ProjectRequest request, String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + ownerEmail));

        Project project = Project.builder()
                .name(request.name())
                .description(request.description())
                .status(request.status() != null ? request.status() : ProjectStatus.PLANNED)
                .startDate(request.startDate())
                .endDate(request.endDate())
                .owner(owner)
                .build();

        return ProjectResponse.from(projectRepository.save(project));
    }

    @Override
    public ProjectResponse getById(Long id) {
        return ProjectResponse.from(findOrThrow(id));
    }

    @Override
    public Page<ProjectResponse> getAll(Pageable pageable) {
        return projectRepository.findAll(pageable).map(ProjectResponse::from);
    }

    @Override
    @Transactional
    public ProjectResponse update(Long id, ProjectRequest request) {
        Project project = findOrThrow(id);

        project.setName(request.name());
        project.setDescription(request.description());
        if (request.status() != null) {
            project.setStatus(request.status());
        }
        project.setStartDate(request.startDate());
        project.setEndDate(request.endDate());

        return ProjectResponse.from(project);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Project project = findOrThrow(id);
        projectRepository.delete(project);
    }

    private Project findOrThrow(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
    }
}
