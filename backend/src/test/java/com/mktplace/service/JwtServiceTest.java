package com.mktplace.service;

import com.mktplace.security.JwtService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {
    @Test
    void shouldGenerateAndValidateAccessToken() {
        JwtService jwtService = new JwtService("super-secret-key-super-secret-key-super-secret-key", 60_000, 120_000);

        String token = jwtService.generateAccessToken("user@example.com");

        assertThat(jwtService.isValid(token)).isTrue();
        assertThat(jwtService.extractSubject(token)).isEqualTo("user@example.com");
    }
}
