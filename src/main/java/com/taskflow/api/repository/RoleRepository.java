package com.taskflow.api.repository;

import com.taskflow.api.entity.Role;
import com.taskflow.api.entity.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleName name);
}
