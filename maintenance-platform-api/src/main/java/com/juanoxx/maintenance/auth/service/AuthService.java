package com.juanoxx.maintenance.auth.service;

import com.juanoxx.maintenance.auth.dto.AuthResponse;
import com.juanoxx.maintenance.auth.dto.LoginRequest;
import com.juanoxx.maintenance.auth.dto.RefreshTokenRequest;
import com.juanoxx.maintenance.auth.dto.RegisterRequest;
import com.juanoxx.maintenance.building.entity.Building;
import com.juanoxx.maintenance.building.repository.BuildingRepository;
import com.juanoxx.maintenance.common.exception.BusinessException;
import com.juanoxx.maintenance.common.exception.ResourceNotFoundException;
import com.juanoxx.maintenance.security.entity.RefreshToken;
import com.juanoxx.maintenance.security.config.JwtProperties;
import com.juanoxx.maintenance.security.model.AuthenticatedUser;
import com.juanoxx.maintenance.security.repository.RefreshTokenRepository;
import com.juanoxx.maintenance.security.service.JwtService;
import com.juanoxx.maintenance.user.entity.User;
import com.juanoxx.maintenance.user.entity.UserRole;
import com.juanoxx.maintenance.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final BuildingRepository buildingRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public AuthResponse register(RegisterRequest request, HttpServletRequest httpRequest) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new BusinessException(HttpStatus.CONFLICT, "Email is already registered");
        }

        boolean bootstrapAdmin = userRepository.countByRole(UserRole.ADMIN) == 0;
        UserRole role = bootstrapAdmin ? UserRole.ADMIN : UserRole.RESIDENT;

        Building building = null;
        if (request.buildingId() != null) {
            building = buildingRepository.findById(request.buildingId())
                    .orElseThrow(() -> new ResourceNotFoundException("Building not found"));
        }
        if (role == UserRole.RESIDENT && building == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Building is required for resident registration");
        }

        User user = new User();
        user.setFullName(request.fullName().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(role);
        user.setBuilding(building);
        user.setActive(true);
        User savedUser = userRepository.save(user);

        return issueTokens(savedUser, httpRequest);
    }

    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (BadCredentialsException ex) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        return issueTokens(user, httpRequest);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request, HttpServletRequest httpRequest) {
        String tokenHash = sha256(request.refreshToken());
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHashAndRevokedAtIsNull(tokenHash)
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        if (refreshToken.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Refresh token expired");
        }

        User user = refreshToken.getUser();
        refreshToken.setRevokedAt(OffsetDateTime.now());
        refreshTokenRepository.save(refreshToken);

        return issueTokens(user, httpRequest);
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        String tokenHash = sha256(request.refreshToken());
        refreshTokenRepository.findByTokenHashAndRevokedAtIsNull(tokenHash).ifPresent(token -> {
            token.setRevokedAt(OffsetDateTime.now());
            refreshTokenRepository.save(token);
        });
    }

    private AuthResponse issueTokens(User user, HttpServletRequest httpRequest) {
        AuthenticatedUser principal = AuthenticatedUser.builder()
                .id(user.getId())
                .email(user.getEmail())
                .password(user.getPasswordHash())
                .role(user.getRole())
                .buildingId(user.getBuilding() != null ? user.getBuilding().getId() : null)
                .active(user.isActive())
                .build();

        String accessToken = jwtService.generateAccessToken(principal);
        String refreshTokenRaw = UUID.randomUUID() + "." + UUID.randomUUID();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setId(UUID.randomUUID());
        refreshToken.setUser(user);
        refreshToken.setTokenHash(sha256(refreshTokenRaw));
        refreshToken.setCreatedAt(OffsetDateTime.now());
        refreshToken.setExpiresAt(OffsetDateTime.now().plusDays(jwtProperties.getRefreshTokenExpirationDays()));
        refreshToken.setIpAddress(httpRequest.getRemoteAddr());
        refreshToken.setUserAgent(safeSubstring(httpRequest.getHeader("User-Agent"), 400));
        refreshTokenRepository.save(refreshToken);

        return new AuthResponse(
                accessToken,
                refreshTokenRaw,
                new AuthResponse.UserSummary(
                        user.getId(),
                        user.getFullName(),
                        user.getEmail(),
                        user.getRole(),
                        user.getBuilding() != null ? user.getBuilding().getId() : null
                )
        );
    }

    private String sha256(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm not available", ex);
        }
    }

    private String safeSubstring(String value, int maxLen) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLen ? value : value.substring(0, maxLen);
    }
}
