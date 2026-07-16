package com.travel.identity.application.usecase;

import com.travel.common.exception.BusinessRuleViolationException;
import com.travel.identity.application.dto.request.LoginRequest;
import com.travel.identity.application.dto.response.AuthResponse;
import com.travel.identity.domain.model.Email;
import com.travel.identity.domain.model.User;
import com.travel.identity.domain.repository.UserRepository;
import com.travel.identity.domain.service.JwtTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginUserUseCase {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    @Transactional
    public AuthResponse execute(LoginRequest request) {
        Email email = Email.of(request.email());

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new BusinessRuleViolationException(
                "Invalid email or password", "INVALID_CREDENTIALS"));

        user.assertCanAuthenticate();

        if (!passwordEncoder.matches(request.password(), user.getPassword().getHash())) {
            user.recordFailedLoginAttempt();
            userRepository.save(user);
            log.warn("Failed login attempt for: {}", email.getValue());
            throw new BusinessRuleViolationException(
                "Invalid email or password", "INVALID_CREDENTIALS");
        }

        user.resetFailedLoginAttempts();
        userRepository.save(user);

        log.info("User authenticated: {}", user.getId().getValue());

        return new AuthResponse(
            jwtTokenService.generateAccessToken(user),
            jwtTokenService.generateRefreshToken(user),
            jwtTokenService.getAccessTokenExpiry(),
            user.getId().getValue(),
            user.getEmail().getValue(),
            user.getFullName().getFullName()
        );
    }
}
