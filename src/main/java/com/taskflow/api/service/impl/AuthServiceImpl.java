package com.taskflow.api.service.impl;

import com.taskflow.api.dto.request.LoginRequest;
import com.taskflow.api.dto.request.RegisterRequest;
import com.taskflow.api.dto.response.AuthResponse;
import com.taskflow.api.entity.Role;
import com.taskflow.api.entity.User;
import com.taskflow.api.entity.enums.RoleName;
import com.taskflow.api.exception.DuplicateResourceException;
import com.taskflow.api.exception.ResourceNotFoundException;
import com.taskflow.api.repository.RoleRepository;
import com.taskflow.api.repository.UserRepository;
import com.taskflow.api.security.JwtTokenProvider;
import com.taskflow.api.security.UserPrincipal;
import com.taskflow.api.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("An account with this email already exists");
        }

        RoleName requestedRole = parseRole(request.role());
        Role role = roleRepository.findByName(requestedRole)
                .orElseThrow(() -> new ResourceNotFoundException("Role not seeded: " + requestedRole));

        Set<Role> roles = new HashSet<>();
        roles.add(role);

        User user = User.builder()
                .fullName(request.fullName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .enabled(true)
                .roles(roles)
                .build();

        User saved = userRepository.save(user);

        UserPrincipal principal = new UserPrincipal(saved);
        String token = jwtTokenProvider.generateToken(principal);

        return AuthResponse.of(token, saved.getId(), saved.getEmail(), roleNames(saved));
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        String token = jwtTokenProvider.generateToken(principal);

        Set<String> roles = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        return AuthResponse.of(token, principal.getId(), principal.getUsername(), roles);
    }

    private RoleName parseRole(String rawRole) {
        if (rawRole == null || rawRole.isBlank()) {
            return RoleName.ROLE_MEMBER;
        }
        try {
            return RoleName.valueOf(rawRole.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unknown role: " + rawRole);
        }
    }

    private Set<String> roleNames(User user) {
        return user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toSet());
    }
}
