package com.juanoxx.maintenance.security.service;

import com.juanoxx.maintenance.security.config.JwtProperties;
import com.juanoxx.maintenance.security.model.AuthenticatedUser;
import com.juanoxx.maintenance.user.entity.UserRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class JwtServiceTest {

    @Test
    void shouldGenerateTokenWhenBuildingIdIsNull() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret("VGhpc0lzQVN0cm9uZ0RldmVsb3BtZW50U2VjcmV0S2V5VGhhdE11c3RCZUF0TGVhc3QzMkJ5dGVz");
        jwtProperties.setAccessTokenExpirationMinutes(30);
        jwtProperties.setRefreshTokenExpirationDays(14);
        JwtService jwtService = new JwtService(jwtProperties);

        AuthenticatedUser admin = AuthenticatedUser.builder()
                .id(1L)
                .email("admin@demo.com")
                .password("secret")
                .role(UserRole.ADMIN)
                .buildingId(null)
                .active(true)
                .build();

        String token = assertDoesNotThrow(() -> jwtService.generateAccessToken(admin));
        assertEquals("admin@demo.com", jwtService.extractUsername(token));
    }
}
