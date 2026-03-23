package com.mktplace.controller;

import com.mktplace.dto.AuthDtos.*;
import com.mktplace.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService) { this.authService = authService; }
    @PostMapping("/register") public AuthResponse register(@Valid @RequestBody RegisterRequest request) { return authService.register(request); }
    @PostMapping("/login") public AuthResponse login(@Valid @RequestBody LoginRequest request) { return authService.login(request); }
    @PostMapping("/refresh") public AuthResponse refresh(@RequestBody RefreshRequest request) { return authService.refresh(request.refreshToken()); }
}
