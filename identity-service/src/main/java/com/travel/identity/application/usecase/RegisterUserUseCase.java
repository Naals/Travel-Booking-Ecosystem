package com.travel.identity.application.usecase;

import com.travel.common.exception.BusinessRuleViolationException;
import com.travel.identity.application.dto.request.RegisterRequest;
import com.travel.identity.application.dto.response.AuthResponse;
import com.travel.identity.domain.model.*;
import com.travel.identity.domain.repository.UserRepository;
import com.travel.identity.domain.service.JwtTokenService;
import com.travel.identity.infrastructure.messaging.UserEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterUserUseCase {

    private final UserRepository     userRepository;
    private final PasswordEncoder    passwordEncoder;
    private final JwtTokenService    jwtTokenService;
    private final UserEventPublisher eventPublisher;

    @Transactional
    public AuthResponse execute(RegisterRequest request) {
        log.info("Registering user: {}", request.email());

        Email email = Email.of(request.email());

        if (userRepository.existsByEmail(email))
            throw new BusinessRuleViolationException(
                "Email address is already registered", "EMAIL_ALREADY_EXISTS");

        HashedPassword password = HashedPassword.ofHash(
            passwordEncoder.encode(request.password()));
        FullName fullName = FullName.of(request.firstName(), request.lastName());

        User user = User.register(email, password, fullName);
        User saved = userRepository.save(user);

        // Write events to outbox — published to Kafka by OutboxEventPoller
        eventPublisher.publishEvents(saved.getDomainEvents());
        saved.clearDomainEvents();

        log.info("User registered successfully: {}", saved.getId().getValue());

        return new AuthResponse(
            jwtTokenService.generateAccessToken(saved),
            jwtTokenService.generateRefreshToken(saved),
            jwtTokenService.getAccessTokenExpiry(),
            saved.getId().getValue(),
            saved.getEmail().getValue(),
            saved.getFullName().getFullName()
        );
    }
}
