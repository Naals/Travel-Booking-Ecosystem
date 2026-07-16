package com.travel.identity.application.usecase;

import com.travel.common.exception.BusinessRuleViolationException;
import com.travel.identity.application.dto.request.RefreshTokenRequest;
import com.travel.identity.application.dto.response.AuthResponse;
import com.travel.identity.domain.model.UserId;
import com.travel.identity.domain.model.User;
import com.travel.identity.domain.repository.UserRepository;
import com.travel.identity.domain.service.JwtTokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefreshTokenUseCase {

    private final UserRepository  userRepository;
    private final JwtTokenService jwtTokenService;

    @Transactional(readOnly = true)
    public AuthResponse execute(RefreshTokenRequest request) {
        try {
            Claims claims = jwtTokenService.validateAndExtractClaims(request.refreshToken());

            if (!jwtTokenService.isRefreshToken(claims))
                throw new BusinessRuleViolationException("Invalid token type", "INVALID_TOKEN");

            User user = userRepository.findById(UserId.of(claims.getSubject()))
                .orElseThrow(() -> new BusinessRuleViolationException(
                    "User not found", "USER_NOT_FOUND"));

            user.assertCanAuthenticate();

            return new AuthResponse(
                jwtTokenService.generateAccessToken(user),
                jwtTokenService.generateRefreshToken(user),
                jwtTokenService.getAccessTokenExpiry(),
                user.getId().getValue(),
                user.getEmail().getValue(),
                user.getFullName().getFullName()
            );
        } catch (JwtException ex) {
            throw new BusinessRuleViolationException(
                "Invalid or expired refresh token", "INVALID_TOKEN");
        }
    }
}
