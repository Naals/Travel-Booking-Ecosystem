package com.travel.user.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
    @NotBlank(message = "Display name is required")
    @Size(min = 2, max = 50)
    String displayName,

    @Size(max = 500, message = "Bio must not exceed 500 characters")
    String bio,

    String avatarUrl
) {}
