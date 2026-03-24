package com.juanoxx.maintenance.security.service;

import com.juanoxx.maintenance.common.exception.ResourceNotFoundException;
import com.juanoxx.maintenance.security.model.AuthenticatedUser;
import com.juanoxx.maintenance.user.entity.User;
import com.juanoxx.maintenance.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public AuthenticatedUser loadUserByUsername(String username) {
        User user = userRepository.findByEmailIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return AuthenticatedUser.builder()
                .id(user.getId())
                .buildingId(user.getBuilding() != null ? user.getBuilding().getId() : null)
                .email(user.getEmail())
                .password(user.getPasswordHash())
                .role(user.getRole())
                .active(user.isActive())
                .build();
    }
}
