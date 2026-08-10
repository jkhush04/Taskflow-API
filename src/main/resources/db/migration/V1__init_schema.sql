-- ============================================================
-- TaskFlow API - Initial Schema
-- Normalized tables: roles, users, user_roles, projects, tasks
-- ============================================================

CREATE TABLE roles (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE users (
    id           BIGSERIAL PRIMARY KEY,
    full_name    VARCHAR(100)  NOT NULL,
    email        VARCHAR(150)  NOT NULL UNIQUE,
    password     VARCHAR(255)  NOT NULL,
    enabled      BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ   NOT NULL DEFAULT now()
);

-- Explicit index on the lookup column used at login/authentication time.
CREATE UNIQUE INDEX idx_users_email ON users (email);

-- Join table for the many-to-many User <-> Role relationship.
CREATE TABLE user_roles (
    user_id  BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id  BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE INDEX idx_user_roles_user_id ON user_roles (user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles (role_id);

CREATE TABLE projects (
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(150)  NOT NULL,
    description  VARCHAR(2000),
    status       VARCHAR(20)   NOT NULL DEFAULT 'PLANNED',
    start_date   DATE,
    end_date     DATE,
    owner_id     BIGINT        NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    created_at   TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ   NOT NULL DEFAULT now()
);

-- Foreign-key and status indexes: owner lookups and status-filtered dashboards are hot paths.
CREATE INDEX idx_projects_owner_id ON projects (owner_id);
CREATE INDEX idx_projects_status   ON projects (status);

CREATE TABLE tasks (
    id             BIGSERIAL PRIMARY KEY,
    title          VARCHAR(200)  NOT NULL,
    description    VARCHAR(2000),
    status         VARCHAR(20)   NOT NULL DEFAULT 'TODO',
    deadline       TIMESTAMPTZ   NOT NULL,
    project_id     BIGINT        NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    assignee_id    BIGINT        REFERENCES users(id) ON DELETE SET NULL,
    reminder_sent  BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ   NOT NULL DEFAULT now()
);

-- Foreign-key indexes for join performance.
CREATE INDEX idx_tasks_project_id  ON tasks (project_id);
CREATE INDEX idx_tasks_assignee_id ON tasks (assignee_id);
CREATE INDEX idx_tasks_status      ON tasks (status);

-- Deadline is explicitly indexed: it drives both the paginated/sorted task list endpoint
-- and the hourly reminder-scheduler sweep (WHERE deadline BETWEEN now AND now+24h).
CREATE INDEX idx_tasks_deadline ON tasks (deadline);

-- Supports the scheduler's "not yet reminded" filter without a full table scan.
CREATE INDEX idx_tasks_reminder_sent ON tasks (reminder_sent);

-- Seed the three fixed RBAC roles.
INSERT INTO roles (name) VALUES ('ROLE_ADMIN'), ('ROLE_MANAGER'), ('ROLE_MEMBER');
