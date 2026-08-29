package com.travel.audit.infrastructure.web.controller;

import com.travel.common.exception.BusinessRuleViolationException;
import com.travel.common.response.ApiResponse;
import com.travel.common.response.PagedResponse;
import com.travel.audit.application.dto.response.AuditLogEntryResponse;
import com.travel.audit.application.dto.response.ChainIntegrityResponse;
import com.travel.audit.application.usecase.GetAuditTrailUseCase;
import com.travel.audit.application.usecase.VerifyChainIntegrityUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Two authorization tiers on one controller — a first in this
 * platform. Every prior staff-only controller (review Day 16, wallet
 * Day 18, loyalty Day 19, fraud Day 21, analytics Day 22) gated its
 * entire surface uniformly. Here, viewing a trail is a routine support
 * action (SUPPORT_AGENT-or-ADMIN, the same bar those services used),
 * but verifying the chain's cryptographic integrity is a systems/
 * compliance operation with no customer-support use case — gated
 * ADMIN-only, per endpoint rather than for the whole controller.
 */
@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
@Tag(name = "Audit", description = "Compliance audit trail and hash-chain integrity verification (staff only)")
public class AuditController {

    private final GetAuditTrailUseCase        trailUseCase;
    private final VerifyChainIntegrityUseCase verifyUseCase;

    @GetMapping("/subject/{subjectId}")
    @Operation(summary = "Audit trail for a single subject — a booking, payment, or user (SUPPORT_AGENT / ADMIN)")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogEntryResponse>>> getBySubject(
        @PathVariable String subjectId,
        @RequestHeader("X-User-Roles") String roles,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
        assertStaff(roles);
        return ResponseEntity.ok(ApiResponse.ok(trailUseCase.executeBySubject(subjectId, page, size)));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Audit trail across every subject touching this user (SUPPORT_AGENT / ADMIN)")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogEntryResponse>>> getByUser(
        @PathVariable String userId,
        @RequestHeader("X-User-Roles") String roles,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
        assertStaff(roles);
        return ResponseEntity.ok(ApiResponse.ok(trailUseCase.executeByUser(userId, page, size)));
    }

    @GetMapping("/integrity")
    @Operation(summary = "Verify the entire audit chain's cryptographic integrity (ADMIN only)")
    public ResponseEntity<ApiResponse<ChainIntegrityResponse>> verifyIntegrity(
        @RequestHeader("X-User-Roles") String roles) {
        assertAdmin(roles);
        return ResponseEntity.ok(ApiResponse.ok(verifyUseCase.execute()));
    }

    private void assertStaff(String roles) {
        if (roles == null || (!roles.contains("SUPPORT_AGENT") && !roles.contains("ADMIN"))) {
            throw new BusinessRuleViolationException(
                "This action requires SUPPORT_AGENT or ADMIN role", "FORBIDDEN");
        }
    }

    private void assertAdmin(String roles) {
        if (roles == null || !roles.contains("ADMIN")) {
            throw new BusinessRuleViolationException("This action requires ADMIN role", "FORBIDDEN");
        }
    }
}
