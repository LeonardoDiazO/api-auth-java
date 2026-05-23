package com.universal.auth.service.impl;

import com.universal.auth.domain.entities.ApplicationEntity;
import com.universal.auth.domain.entities.Role;
import com.universal.auth.domain.entities.User;
import com.universal.auth.domain.entities.UserRole;
import com.universal.auth.dto.request.AssignRoleRequest;
import com.universal.auth.dto.request.ChangePasswordRequest;
import com.universal.auth.dto.request.CreateUserRequest;
import com.universal.auth.dto.request.UpdateUserRequest;
import com.universal.auth.dto.response.RoleResponse;
import com.universal.auth.dto.response.UserResponse;
import com.universal.auth.exception.*;
import com.universal.auth.repository.ApplicationRepository;
import com.universal.auth.repository.RoleRepository;
import com.universal.auth.repository.UserRepository;
import com.universal.auth.repository.UserRoleRepository;
import com.universal.auth.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final ApplicationRepository applicationRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        ApplicationEntity app = applicationRepository.findById(request.getAppId())
                .orElseThrow(() -> new ApplicationNotFoundException(request.getAppId()));

        if (userRepository.existsByUsernameAndApplication_AppId(request.getUsername(), request.getAppId())) {
            throw new DuplicateUsernameException(request.getUsername());
        }
        if (userRepository.existsByEmailAndApplication_AppId(request.getEmail(), request.getAppId())) {
            throw new DuplicateEmailException(request.getEmail());
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        user.setIsEmailVerified(false);
        user.setCreatedAt(LocalDateTime.now());
        user.setLastPasswordChange(LocalDateTime.now());
        user.setFailedLoginAttempts(0);
        user.setApplication(app);

        return mapToResponse(userRepository.save(user));
    }

    @Override
    public UserResponse getUserById(Long id) {
        return mapToResponse(userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id)));
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserResponse> getUsersByApp(Long appId) {
        return userRepository.findByApplication_AppId(appId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<UserResponse> getUsers(Long appId, Pageable pageable) {
        Page<User> page = appId != null
                ? userRepository.findByApplication_AppId(appId, pageable)
                : userRepository.findAll(pageable);
        return page.map(this::mapToResponse);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (request.getEmail() != null) {
            userRepository.findByEmailAndApplication_AppId(request.getEmail(), user.getApplication().getAppId())
                    .ifPresent(existing -> {
                        if (!existing.getUserId().equals(id)) {
                            throw new DuplicateEmailException(request.getEmail());
                        }
                    });
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }

        return mapToResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        userRepository.delete(userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id)));
    }

    @Override
    @Transactional
    public UserResponse assignRole(Long userId, AssignRoleRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new RoleNotFoundException(request.getRoleId()));
        ApplicationEntity application = applicationRepository.findById(request.getAppId())
                .orElseThrow(() -> new ApplicationNotFoundException(request.getAppId()));

        if (userRoleRepository.existsByUserAndRoleAndApplication(user, role, application)) {
            throw new DuplicateRoleAssignmentException(userId, request.getRoleId(), request.getAppId());
        }

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        userRole.setApplication(application);
        userRoleRepository.save(userRole);

        return mapToResponse(user);
    }

    @Override
    @Transactional
    public UserResponse removeRole(Long userId, Long roleId, Long appId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException(roleId));
        ApplicationEntity application = applicationRepository.findById(appId)
                .orElseThrow(() -> new ApplicationNotFoundException(appId));

        userRoleRepository.deleteByUserAndRoleAndApplication(user, role, application);
        return mapToResponse(user);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new InvalidPasswordException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setLastPasswordChange(LocalDateTime.now());
        user.setFailedLoginAttempts(0);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public UserResponse activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        user.setIsActive(true);
        return mapToResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        user.setIsActive(false);
        return mapToResponse(userRepository.save(user));
    }

    private UserResponse mapToResponse(User user) {
        List<UserRole> userRoles = userRoleRepository.findByUser(user);

        List<RoleResponse> roles = userRoles.stream()
                .map(ur -> RoleResponse.builder()
                        .roleId(ur.getRole().getRoleId())
                        .roleName(ur.getRole().getRoleName())
                        .appId(ur.getApplication() != null ? ur.getApplication().getAppId() : null)
                        .appName(ur.getApplication() != null ? ur.getApplication().getAppName() : null)
                        .build())
                .collect(Collectors.toList());

        return UserResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .fullName(user.getFullName())
                .isActive(user.getIsActive())
                .isEmailVerified(user.getIsEmailVerified())
                .appId(user.getApplication() != null ? user.getApplication().getAppId() : null)
                .appName(user.getApplication() != null ? user.getApplication().getAppName() : null)
                .createdAt(user.getCreatedAt())
                .lastPasswordChange(user.getLastPasswordChange())
                .roles(roles)
                .build();
    }
}
