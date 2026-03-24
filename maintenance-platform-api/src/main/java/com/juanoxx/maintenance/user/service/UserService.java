package com.juanoxx.maintenance.user.service;

import com.juanoxx.maintenance.building.entity.Building;
import com.juanoxx.maintenance.building.repository.BuildingRepository;
import com.juanoxx.maintenance.common.exception.BusinessException;
import com.juanoxx.maintenance.common.exception.ResourceNotFoundException;
import com.juanoxx.maintenance.user.entity.User;
import com.juanoxx.maintenance.user.entity.UserRole;
import com.juanoxx.maintenance.user.dto.UserCreateRequest;
import com.juanoxx.maintenance.user.dto.UserResponse;
import com.juanoxx.maintenance.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BuildingRepository buildingRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new BusinessException(HttpStatus.CONFLICT, "Email already exists");
        }

        Building building = null;
        if (request.buildingId() != null) {
            building = buildingRepository.findById(request.buildingId())
                    .orElseThrow(() -> new ResourceNotFoundException("Building not found"));
        }
        if (request.role() == UserRole.RESIDENT && building == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Resident must have an associated building");
        }

        User user = new User();
        user.setFullName(request.fullName().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        user.setBuilding(building);
        user.setActive(true);
        return toResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public User getRequiredById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getBuilding() != null ? user.getBuilding().getId() : null,
                user.isActive()
        );
    }
}
