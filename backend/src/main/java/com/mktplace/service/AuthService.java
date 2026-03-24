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
import static com.mktplace.validation.InputSanitizer.clean;

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
        DocumentType documentType = request.documentType() == null ? DocumentType.CPF : request.documentType();
        if (role == Role.SELLER && documentType == DocumentType.CNPJ) throw new BusinessException("A plataforma é focada em dev individual: apenas pessoas físicas com CPF podem ser sellers.", HttpStatus.BAD_REQUEST);
        validateBrazilianDocument(documentType, request.documentNumber());
        String sanitizedDocument = digits(request.documentNumber());
        User user = userRepository.save(User.builder()
                .name(clean(request.name()))
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(role)
                .roles(Set.of(role))
                .documentType(documentType)
                .documentNumber(sanitizedDocument)
                .postalCode(digits(request.postalCode()))
                .street(clean(request.street()))
                .streetNumber(clean(request.streetNumber()))
                .complement(clean(request.complement()))
                .neighborhood(clean(request.neighborhood()))
                .city(clean(request.city()))
                .state(clean(request.state()))
                .companyName(clean(request.companyName()))
                .latitude(request.latitude())
                .longitude(request.longitude())
                .createdAt(Instant.now())
                .active(true)
                .blocked(false)
                .build());
        return issueTokens(user);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        User user = userRepository.findByEmail(request.email()).orElseThrow();
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);
        return issueTokens(user);
    }

    public AuthResponse refresh(String refreshToken) {
        var saved = refreshTokenRepository.findByTokenAndRevokedFalse(refreshToken).filter(token -> token.getExpiresAt().isAfter(Instant.now())).orElseThrow(() -> new BusinessException("Refresh token inválido", HttpStatus.UNAUTHORIZED));
        return issueTokens(saved.getUser());
    }

    private void validateBrazilianDocument(DocumentType type, String number) {
        boolean valid = type == DocumentType.CPF ? isValidCpf(number) : isValidCnpj(number);
        if (!valid) throw new BusinessException("CPF/CNPJ inválido", HttpStatus.BAD_REQUEST);
    }

    private AuthResponse issueTokens(User user) {
        String access = jwtService.generateAccessToken(user.getEmail());
        String refresh = jwtService.generateRefreshToken(user.getEmail());
        refreshTokenRepository.save(RefreshToken.builder().token(refresh).user(user).expiresAt(Instant.now().plus(30, ChronoUnit.DAYS)).revoked(false).build());
        return new AuthResponse(access, refresh, toUserResponse(user));
    }

    public static UserResponse toUserResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getDocumentType(), user.getDocumentNumber(), user.getPostalCode(), user.getStreet(), user.getStreetNumber(), user.getComplement(), user.getNeighborhood(), user.getCity(), user.getState(), user.getCompanyName(), user.getLatitude(), user.getLongitude(), user.isActive(), user.isBlocked());
    }
}
