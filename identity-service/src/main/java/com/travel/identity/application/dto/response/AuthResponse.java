package com.travel.identity.application.dto.response;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    long   expiresIn,
    String userId,
    String email,
    String fullName
) {
    public String tokenType() { return "Bearer"; }
}
