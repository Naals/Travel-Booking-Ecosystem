package com.travel.wallet.infrastructure.web.controller;

import com.travel.common.exception.BusinessRuleViolationException;
import com.travel.common.response.ApiResponse;
import com.travel.common.response.PagedResponse;
import com.travel.wallet.application.dto.request.AdjustWalletRequest;
import com.travel.wallet.application.dto.request.FreezeWalletRequest;
import com.travel.wallet.application.dto.request.TopUpWalletRequest;
import com.travel.wallet.application.dto.response.WalletResponse;
import com.travel.wallet.application.dto.response.WalletTransactionResponse;
import com.travel.wallet.application.usecase.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
@Tag(name = "Wallet", description = "User wallet balance, top-ups, and transaction history")
public class WalletController {

    private final GetWalletUseCase             getWalletUseCase;
    private final GetTransactionHistoryUseCase getHistoryUseCase;
    private final TopUpWalletUseCase           topUpUseCase;
    private final AdminAdjustWalletUseCase     adjustUseCase;
    private final FreezeWalletUseCase          freezeUseCase;
    private final UnfreezeWalletUseCase        unfreezeUseCase;

    @GetMapping("/me")
    @Operation(summary = "Get the authenticated user's wallet")
    public ResponseEntity<ApiResponse<WalletResponse>> getMyWallet(
        @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(ApiResponse.ok(getWalletUseCase.execute(userId)));
    }

    @GetMapping("/me/transactions")
    @Operation(summary = "List the authenticated user's transaction history, paginated")
    public ResponseEntity<ApiResponse<PagedResponse<WalletTransactionResponse>>> getMyTransactions(
        @RequestHeader("X-User-Id") String userId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(getHistoryUseCase.execute(userId, page, size)));
    }

    @PostMapping("/me/topup")
    @Operation(summary = "Top up the authenticated user's wallet")
    public ResponseEntity<ApiResponse<WalletTransactionResponse>> topUp(
        @RequestHeader("X-User-Id") String userId,
        @Valid @RequestBody TopUpWalletRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(topUpUseCase.execute(userId, request)));
    }

    @PostMapping("/{userId}/adjust")
    @Operation(summary = "Apply a manual credit or debit adjustment (SUPPORT_AGENT / ADMIN)")
    public ResponseEntity<ApiResponse<WalletTransactionResponse>> adjust(
        @PathVariable String userId,
        @RequestHeader("X-User-Roles") String roles,
        @Valid @RequestBody AdjustWalletRequest request) {
        assertStaff(roles);
        return ResponseEntity.ok(ApiResponse.ok(adjustUseCase.execute(userId, request)));
    }

    @PostMapping("/{userId}/freeze")
    @Operation(summary = "Freeze a wallet, blocking further credits and debits (SUPPORT_AGENT / ADMIN)")
    public ResponseEntity<ApiResponse<WalletResponse>> freeze(
        @PathVariable String userId,
        @RequestHeader("X-User-Roles") String roles,
        @Valid @RequestBody FreezeWalletRequest request) {
        assertStaff(roles);
        return ResponseEntity.ok(ApiResponse.ok(freezeUseCase.execute(userId, request)));
    }

    @PostMapping("/{userId}/unfreeze")
    @Operation(summary = "Unfreeze a wallet (SUPPORT_AGENT / ADMIN)")
    public ResponseEntity<ApiResponse<WalletResponse>> unfreeze(
        @PathVariable String userId,
        @RequestHeader("X-User-Roles") String roles) {
        assertStaff(roles);
        return ResponseEntity.ok(ApiResponse.ok(unfreezeUseCase.execute(userId)));
    }

    /** Same coarse role-check pattern as ReviewController (Day 16). */
    private void assertStaff(String roles) {
        if (roles == null || (!roles.contains("SUPPORT_AGENT") && !roles.contains("ADMIN"))) {
            throw new BusinessRuleViolationException(
                "This action requires SUPPORT_AGENT or ADMIN role", "FORBIDDEN");
        }
    }
}
