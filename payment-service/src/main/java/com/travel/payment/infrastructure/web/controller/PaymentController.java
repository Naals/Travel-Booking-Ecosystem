package com.travel.payment.infrastructure.web.controller;

import com.travel.common.response.ApiResponse;
import com.travel.payment.application.dto.response.PaymentResponse;
import com.travel.payment.application.usecase.GetPaymentUseCase;
import com.travel.payment.application.usecase.RefundPaymentUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment lifecycle and refund management")
public class PaymentController {

    private final GetPaymentUseCase    getUseCase;
    private final RefundPaymentUseCase refundUseCase;

    @GetMapping("/{paymentId}")
    @Operation(summary = "Get payment by ID")
    public ResponseEntity<ApiResponse<PaymentResponse>> getById(
        @PathVariable String paymentId) {
        return ResponseEntity.ok(ApiResponse.ok(getUseCase.execute(paymentId)));
    }

    @GetMapping("/booking/{bookingId}")
    @Operation(summary = "Get payment by booking ID")
    public ResponseEntity<ApiResponse<PaymentResponse>> getByBooking(
        @PathVariable String bookingId) {
        return ResponseEntity.ok(
            ApiResponse.ok(getUseCase.executeByBooking(bookingId)));
    }

    @GetMapping("/my")
    @Operation(summary = "List all payments for the authenticated user")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getMyPayments(
        @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(
            ApiResponse.ok(getUseCase.executeForUser(userId)));
    }

    @PostMapping("/{paymentId}/refund")
    @Operation(summary = "Issue a refund for a completed payment")
    public ResponseEntity<ApiResponse<Void>> refund(
        @PathVariable String paymentId,
        @RequestHeader("X-User-Id") String userId) {
        refundUseCase.execute(paymentId);
        return ResponseEntity.ok(
            ApiResponse.ok("Refund initiated successfully", null));
    }
}
