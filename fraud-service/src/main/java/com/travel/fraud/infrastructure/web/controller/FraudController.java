package com.travel.fraud.infrastructure.web.controller;

import com.travel.common.exception.BusinessRuleViolationException;
import com.travel.common.response.ApiResponse;
import com.travel.fraud.application.dto.response.RiskProfileResponse;
import com.travel.fraud.application.usecase.ClearRiskFlagUseCase;
import com.travel.fraud.application.usecase.GetRiskProfileUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Staff-only — unlike wallet/loyalty (Days 18–19), there is no "me"
 * self-service endpoint here. Exposing rule outcomes to the flagged
 * user themselves would help a bad actor learn exactly what trips the
 * detector.
 */
@RestController
@RequestMapping("/api/v1/fraud")
@RequiredArgsConstructor
@Tag(name = "Fraud", description = "Risk profiles and manual flag clearing (staff only)")
public class FraudController {

    private final GetRiskProfileUseCase getProfileUseCase;
    private final ClearRiskFlagUseCase  clearFlagUseCase;

    @GetMapping("/{userId}")
    @Operation(summary = "Get a user's risk profile (SUPPORT_AGENT / ADMIN)")
    public ResponseEntity<ApiResponse<RiskProfileResponse>> getProfile(
        @PathVariable String userId,
        @RequestHeader("X-User-Roles") String roles) {
        assertStaff(roles);
        return ResponseEntity.ok(ApiResponse.ok(getProfileUseCase.execute(userId)));
    }

    @PostMapping("/{userId}/clear")
    @Operation(summary = "Clear a flagged risk profile after investigation (SUPPORT_AGENT / ADMIN)")
    public ResponseEntity<ApiResponse<RiskProfileResponse>> clearFlag(
        @PathVariable String userId,
        @RequestHeader("X-User-Id") String staffId,
        @RequestHeader("X-User-Roles") String roles) {
        assertStaff(roles);
        return ResponseEntity.ok(ApiResponse.ok(clearFlagUseCase.execute(userId, staffId)));
    }

    private void assertStaff(String roles) {
        if (roles == null || (!roles.contains("SUPPORT_AGENT") && !roles.contains("ADMIN"))) {
            throw new BusinessRuleViolationException(
                "This action requires SUPPORT_AGENT or ADMIN role", "FORBIDDEN");
        }
    }
}
