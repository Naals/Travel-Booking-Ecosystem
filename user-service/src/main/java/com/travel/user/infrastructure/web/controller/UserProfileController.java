package com.travel.user.infrastructure.web.controller;

import com.travel.common.response.ApiResponse;
import com.travel.common.response.PagedResponse;
import com.travel.user.application.dto.request.AddSavedLocationRequest;
import com.travel.user.application.dto.request.UpdateProfileRequest;
import com.travel.user.application.dto.request.UpdateTravelPreferencesRequest;
import com.travel.user.application.dto.response.TravelHistoryEntryResponse;
import com.travel.user.application.dto.response.UserProfileResponse;
import com.travel.user.application.usecase.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Profiles", description = "Profile, preferences, saved locations, and travel history")
public class UserProfileController {

    private final GetUserProfileUseCase         getProfileUseCase;
    private final UpdateProfileUseCase           updateProfileUseCase;
    private final UpdateTravelPreferencesUseCase updatePreferencesUseCase;
    private final AddSavedLocationUseCase        addSavedLocationUseCase;
    private final RemoveSavedLocationUseCase     removeSavedLocationUseCase;
    private final GetTravelHistoryUseCase        getTravelHistoryUseCase;

    @GetMapping("/me")
    @Operation(summary = "Get the authenticated user's profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(
        @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(ApiResponse.ok(getProfileUseCase.execute(userId)));
    }

    @PatchMapping("/me")
    @Operation(summary = "Update display name, bio, and avatar")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
        @RequestHeader("X-User-Id") String userId,
        @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(updateProfileUseCase.execute(userId, request)));
    }

    @PutMapping("/me/preferences")
    @Operation(summary = "Update travel preferences")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updatePreferences(
        @RequestHeader("X-User-Id") String userId,
        @Valid @RequestBody UpdateTravelPreferencesRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(updatePreferencesUseCase.execute(userId, request)));
    }

    @PostMapping("/me/saved-locations")
    @Operation(summary = "Add a saved location")
    public ResponseEntity<ApiResponse<UserProfileResponse>> addSavedLocation(
        @RequestHeader("X-User-Id") String userId,
        @Valid @RequestBody AddSavedLocationRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(addSavedLocationUseCase.execute(userId, request)));
    }

    @DeleteMapping("/me/saved-locations/{savedLocationId}")
    @Operation(summary = "Remove a saved location")
    public ResponseEntity<ApiResponse<UserProfileResponse>> removeSavedLocation(
        @RequestHeader("X-User-Id") String userId,
        @PathVariable String savedLocationId) {
        return ResponseEntity.ok(
            ApiResponse.ok(removeSavedLocationUseCase.execute(userId, savedLocationId)));
    }

    @GetMapping("/me/travel-history")
    @Operation(summary = "List completed trips, paginated")
    public ResponseEntity<ApiResponse<PagedResponse<TravelHistoryEntryResponse>>> getTravelHistory(
        @RequestHeader("X-User-Id") String userId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
            ApiResponse.ok(getTravelHistoryUseCase.execute(userId, page, size)));
    }
}
