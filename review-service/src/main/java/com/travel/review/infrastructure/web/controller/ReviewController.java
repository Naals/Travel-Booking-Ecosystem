package com.travel.review.infrastructure.web.controller;

import com.travel.common.exception.BusinessRuleViolationException;
import com.travel.common.response.ApiResponse;
import com.travel.common.response.PagedResponse;
import com.travel.review.application.dto.request.CreateReviewRequest;
import com.travel.review.application.dto.request.FlagReviewRequest;
import com.travel.review.application.dto.request.ModerateReviewRequest;
import com.travel.review.application.dto.response.RatingSummaryResponse;
import com.travel.review.application.dto.response.ReviewResponse;
import com.travel.review.application.usecase.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Reviews, ratings, and moderation")
public class ReviewController {

    private final CreateReviewUseCase          createUseCase;
    private final ModerateReviewUseCase        moderateUseCase;
    private final FlagReviewUseCase            flagUseCase;
    private final GetReviewsForResourceUseCase getForResourceUseCase;
    private final GetModerationQueueUseCase    getModerationQueueUseCase;
    private final GetRatingSummaryUseCase      getRatingSummaryUseCase;

    @PostMapping
    @Operation(summary = "Write a review for a completed booking")
    public ResponseEntity<ApiResponse<ReviewResponse>> create(
        @RequestHeader("X-User-Id") String userId,
        @Valid @RequestBody CreateReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(createUseCase.execute(userId, request)));
    }

    @GetMapping("/resource/{resourceId}")
    @Operation(summary = "List approved reviews for a resource, paginated")
    public ResponseEntity<ApiResponse<PagedResponse<ReviewResponse>>> getForResource(
        @PathVariable String resourceId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(getForResourceUseCase.execute(resourceId, page, size)));
    }

    @GetMapping("/resource/{resourceId}/rating")
    @Operation(summary = "Get the aggregate rating summary for a resource")
    public ResponseEntity<ApiResponse<RatingSummaryResponse>> getRatingSummary(
        @PathVariable String resourceId) {
        return ResponseEntity.ok(ApiResponse.ok(getRatingSummaryUseCase.execute(resourceId)));
    }

    @PostMapping("/{reviewId}/flag")
    @Operation(summary = "Report an approved review for re-moderation")
    public ResponseEntity<ApiResponse<ReviewResponse>> flag(
        @PathVariable String reviewId,
        @RequestHeader("X-User-Id") String reporterId,
        @Valid @RequestBody FlagReviewRequest request) {
        return ResponseEntity.ok(
            ApiResponse.ok(flagUseCase.execute(reviewId, reporterId, request.reason())));
    }

    @GetMapping("/moderation-queue")
    @Operation(summary = "List reviews pending moderation (SUPPORT_AGENT / ADMIN)")
    public ResponseEntity<ApiResponse<PagedResponse<ReviewResponse>>> getModerationQueue(
        @RequestHeader("X-User-Roles") String roles,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
        assertModerator(roles);
        return ResponseEntity.ok(ApiResponse.ok(getModerationQueueUseCase.execute(page, size)));
    }

    @PostMapping("/{reviewId}/moderate")
    @Operation(summary = "Approve or reject a pending/flagged review (SUPPORT_AGENT / ADMIN)")
    public ResponseEntity<ApiResponse<ReviewResponse>> moderate(
        @PathVariable String reviewId,
        @RequestHeader("X-User-Id") String moderatorId,
        @RequestHeader("X-User-Roles") String roles,
        @Valid @RequestBody ModerateReviewRequest request) {
        assertModerator(roles);
        ReviewResponse response = "APPROVE".equalsIgnoreCase(request.decision())
            ? moderateUseCase.approve(reviewId, moderatorId)
            : moderateUseCase.reject(reviewId, moderatorId, request.reason());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Coarse role check against the X-User-Roles header the gateway
     * forwards (api-gateway's JwtAuthenticationFilter, Day 5), reusing
     * the SUPPORT_AGENT/ADMIN roles identity-service defined on Day 6.
     * A dedicated @PreAuthorize method-security setup is a reasonable
     * follow-up once more admin-facing endpoints exist platform-wide —
     * not worth its own security config for a single controller today.
     */
    private void assertModerator(String roles) {
        if (roles == null || (!roles.contains("SUPPORT_AGENT") && !roles.contains("ADMIN"))) {
            throw new BusinessRuleViolationException(
                "Moderation requires SUPPORT_AGENT or ADMIN role", "FORBIDDEN");
        }
    }
}
