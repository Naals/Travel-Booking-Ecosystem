package com.travel.identity.infrastructure.web.controller;

import com.travel.common.response.ApiResponse;
import com.travel.identity.application.dto.request.LoginRequest;
import com.travel.identity.application.dto.request.RefreshTokenRequest;
import com.travel.identity.application.dto.request.RegisterRequest;
import com.travel.identity.application.dto.response.AuthResponse;
import com.travel.identity.application.usecase.LoginUserUseCase;
import com.travel.identity.application.usecase.RefreshTokenUseCase;
import com.travel.identity.application.usecase.RegisterUserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Registration, login, and token management")
public class AuthController {

    private final RegisterUserUseCase registerUseCase;
    private final LoginUserUseCase    loginUseCase;
    private final RefreshTokenUseCase refreshUseCase;

    @PostMapping("/register")
    @Operation(summary = "Register a new user account")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
        @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(registerUseCase.execute(request)));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate with email and password")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
        @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(loginUseCase.execute(request)));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Exchange refresh token for new access token")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
        @Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(refreshUseCase.execute(request)));
    }

    @PostMapping("/logout")
    @Operation(summary = "Invalidate current session")
    public ResponseEntity<ApiResponse<Void>> logout(
        @RequestHeader("Authorization") String authHeader) {
        // Token blacklisting via Redis — implemented in next iteration
        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully", null));
    }
}
