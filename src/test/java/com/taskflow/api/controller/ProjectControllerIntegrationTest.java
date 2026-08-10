package com.taskflow.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.api.dto.request.ProjectRequest;
import com.taskflow.api.dto.request.RegisterRequest;
import com.taskflow.api.entity.enums.ProjectStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full-stack integration test: real Spring context, real Spring Security filter chain,
 * real JWT issuance/validation, backed by an in-memory H2 database (see application-test.yml)
 * instead of PostgreSQL so the suite runs without external infrastructure.
 *
 * Verifies the role-based access control on POST /api/projects end-to-end:
 * MEMBER accounts are rejected (403) while MANAGER accounts succeed (201), and unauthenticated
 * calls are rejected (401).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProjectControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createProject_shouldReturn401_whenNoTokenProvided() throws Exception {
        ProjectRequest request = new ProjectRequest("Unauthorized Project", "desc",
                ProjectStatus.PLANNED, LocalDate.now(), LocalDate.now().plusDays(30));

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createProject_shouldReturn403_whenCallerIsMemberRole() throws Exception {
        String memberToken = registerAndGetToken("member@taskflow.dev", "ROLE_MEMBER");

        ProjectRequest request = new ProjectRequest("Forbidden Project", "desc",
                ProjectStatus.PLANNED, LocalDate.now(), LocalDate.now().plusDays(30));

        mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createProject_shouldReturn201_whenCallerIsManagerRole() throws Exception {
        String managerToken = registerAndGetToken("manager@taskflow.dev", "ROLE_MANAGER");

        ProjectRequest request = new ProjectRequest("Approved Project", "desc",
                ProjectStatus.PLANNED, LocalDate.now(), LocalDate.now().plusDays(30));

        mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Approved Project"))
                .andExpect(jsonPath("$.status").value("PLANNED"));
    }

    @Test
    void getProjects_shouldBeAccessible_toAnyAuthenticatedRole() throws Exception {
        String memberToken = registerAndGetToken("reader@taskflow.dev", "ROLE_MEMBER");

        mockMvc.perform(get("/api/projects")
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isOk());
    }

    /** Registers a fresh user with the given role via the real /api/auth/register endpoint and returns its JWT. */
    private String registerAndGetToken(String email, String role) throws Exception {
        RegisterRequest registerRequest = new RegisterRequest("Test User", email, "SecurePass123!", role);

        String responseBody = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(responseBody).get("token").asText();
    }
}
