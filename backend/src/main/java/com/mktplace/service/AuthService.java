package com.mktplace.service;

import com.mktplace.dto.AuthDtos.*;
import com.mktplace.enums.DocumentType;
import com.mktplace.enums.Role;
import com.mktplace.exception.BusinessException;
import com.mktplace.model.RefreshToken;
import com.mktplace.model.User;
import com.mktplace.repository.RefreshTokenRepository;
import com.mktplace.repository.UserRepository;
import com.mktplace.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import static com.mktplace.validation.DocumentValidator.*;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtService jwtService, RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public AuthResponse register(RegisterRequest request) {
        userRepository.findByEmail(request.email()).ifPresent(u -> { throw new BusinessException("Email já cadastrado", HttpStatus.CONFLICT); });
        Role role = request.role() == null ? Role.BUYER : request.role();
        validateBrazilianDocument(request.documentType(), request.documentNumber());
        String sanitizedDocument = digits(request.documentNumber());
        User user = userRepository.save(User.builder().name(request.name()).email(request.email()).password(passwordEncoder.encode(request.password())).role(role).roles(Set.of(role)).documentType(request.documentType() == null ? DocumentType.CPF : request.documentType()).documentNumber(sanitizedDocument).createdAt(Instant.now()).build());
        return issueTokens(user);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        User user = userRepository.findByEmail(request.email()).orElseThrow();
        return issueTokens(user);
    }

    public AuthResponse refresh(String refreshToken) {
        var saved = refreshTokenRepository.findByTokenAndRevokedFalse(refreshToken).filter(token -> token.getExpiresAt().isAfter(Instant.now())).orElseThrow(() -> new BusinessException("Refresh token inválido", HttpStatus.UNAUTHORIZED));
        return issueTokens(saved.getUser());
    }

    private void validateBrazilianDocument(DocumentType type, String number) {
        DocumentType resolved = type == null ? DocumentType.CPF : type;
        boolean valid = resolved == DocumentType.CPF ? isValidCpf(number) : isValidCnpj(number);
        if (!valid) throw new BusinessException("CPF/CNPJ inválido", HttpStatus.BAD_REQUEST);
    }

    private AuthResponse issueTokens(User user) {
        String access = jwtService.generateAccessToken(user.getEmail());
        String refresh = jwtService.generateRefreshToken(user.getEmail());
        refreshTokenRepository.save(RefreshToken.builder().token(refresh).user(user).expiresAt(Instant.now().plus(30, ChronoUnit.DAYS)).revoked(false).build());
        return new AuthResponse(access, refresh, new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getDocumentType(), user.getDocumentNumber()));
    }
}
