package com.travel.analytics.infrastructure.web.controller;

import com.travel.common.exception.BusinessRuleViolationException;
import com.travel.common.response.ApiResponse;
import com.travel.analytics.application.dto.response.BookingFunnelResponse;
import com.travel.analytics.application.dto.response.RevenueSummaryResponse;
import com.travel.analytics.application.usecase.GetBookingFunnelUseCase;
import com.travel.analytics.application.usecase.GetRevenueSummaryUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * ADMIN-only — unlike fraud/wallet/review's SUPPORT_AGENT-or-ADMIN
 * gate (Days 16, 18, 21), financial and funnel metrics are ADMIN-only
 * here. No ANALYST role exists in identity-service's RBAC (Role enum,
 * Day 6, untouched since) — introducing one is future work, not
 * folded into today's scope rather than reusing SUPPORT_AGENT loosely.
 */
@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Business metrics, funnel, and revenue reporting (ADMIN only)")
public class AnalyticsController {

    private final GetBookingFunnelUseCase   funnelUseCase;
    private final GetRevenueSummaryUseCase  revenueUseCase;

    @GetMapping("/bookings/funnel")
    @Operation(summary = "Booking funnel counts and conversion rates over a date range")
    public ResponseEntity<ApiResponse<BookingFunnelResponse>> getFunnel(
        @RequestHeader("X-User-Roles") String roles,
        @RequestParam(required = false) String bookingType,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        assertAdmin(roles);
        return ResponseEntity.ok(ApiResponse.ok(funnelUseCase.execute(bookingType, from, to)));
    }

    @GetMapping("/revenue")
    @Operation(summary = "Gross, refunded, and net revenue over a date range for one currency")
    public ResponseEntity<ApiResponse<RevenueSummaryResponse>> getRevenue(
        @RequestHeader("X-User-Roles") String roles,
        @RequestParam String currency,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        assertAdmin(roles);
        return ResponseEntity.ok(ApiResponse.ok(revenueUseCase.execute(currency, from, to)));
    }

    private void assertAdmin(String roles) {
        if (roles == null || !roles.contains("ADMIN")) {
            throw new BusinessRuleViolationException("This action requires ADMIN role", "FORBIDDEN");
        }
    }
}
