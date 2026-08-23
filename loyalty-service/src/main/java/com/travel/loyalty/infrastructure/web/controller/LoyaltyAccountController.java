package com.travel.loyalty.infrastructure.web.controller;

import com.travel.common.exception.BusinessRuleViolationException;
import com.travel.common.response.ApiResponse;
import com.travel.common.response.PagedResponse;
import com.travel.loyalty.application.dto.request.AdminAdjustPointsRequest;
import com.travel.loyalty.application.dto.request.RedeemPointsRequest;
import com.travel.loyalty.application.dto.response.LoyaltyAccountResponse;
import com.travel.loyalty.application.dto.response.LoyaltyTransactionResponse;
import com.travel.loyalty.application.usecase.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/loyalty")
@RequiredArgsConstructor
@Tag(name = "Loyalty", description = "Reward points, redemption, and membership tiers")
public class LoyaltyAccountController {

    private final GetLoyaltyAccountUseCase     getAccountUseCase;
    private final GetTransactionHistoryUseCase getHistoryUseCase;
    private final RedeemPointsUseCase          redeemUseCase;
    private final AdminAdjustPointsUseCase     adjustUseCase;

    @GetMapping("/me")
    @Operation(summary = "Get the authenticated user's loyalty account")
    public ResponseEntity<ApiResponse<LoyaltyAccountResponse>> getMyAccount(
        @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(ApiResponse.ok(getAccountUseCase.execute(userId)));
    }

    @GetMapping("/me/transactions")
    @Operation(summary = "List the authenticated user's points transaction history, paginated")
    public ResponseEntity<ApiResponse<PagedResponse<LoyaltyTransactionResponse>>> getMyTransactions(
        @RequestHeader("X-User-Id") String userId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(getHistoryUseCase.execute(userId, page, size)));
    }

    @PostMapping("/me/redeem")
    @Operation(summary = "Redeem points from the authenticated user's balance")
    public ResponseEntity<ApiResponse<LoyaltyTransactionResponse>> redeem(
        @RequestHeader("X-User-Id") String userId,
        @Valid @RequestBody RedeemPointsRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(redeemUseCase.execute(userId, request)));
    }

    @PostMapping("/{userId}/adjust")
    @Operation(summary = "Apply a manual points credit or debit (SUPPORT_AGENT / ADMIN)")
    public ResponseEntity<ApiResponse<LoyaltyTransactionResponse>> adjust(
        @PathVariable String userId,
        @RequestHeader("X-User-Roles") String roles,
        @Valid @RequestBody AdminAdjustPointsRequest request) {
        assertStaff(roles);
        return ResponseEntity.ok(ApiResponse.ok(adjustUseCase.execute(userId, request)));
    }

    /** Same coarse role-check pattern as WalletController (Day 18) and ReviewController (Day 16). */
    private void assertStaff(String roles) {
        if (roles == null || (!roles.contains("SUPPORT_AGENT") && !roles.contains("ADMIN"))) {
            throw new BusinessRuleViolationException(
                "This action requires SUPPORT_AGENT or ADMIN role", "FORBIDDEN");
        }
    }
}
