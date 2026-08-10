package com.taskflow.api.entity.enums;

/**
 * The three roles supported by TaskFlow's RBAC model.
 * ADMIN    - full system access, user & project management
 * MANAGER  - can create/manage projects and assign tasks
 * MEMBER   - can view assigned projects and update own tasks
 */
public enum RoleName {
    ROLE_ADMIN,
    ROLE_MANAGER,
    ROLE_MEMBER
}
