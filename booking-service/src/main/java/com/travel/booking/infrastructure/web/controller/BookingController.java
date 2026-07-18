package com.travel.booking.infrastructure.web.controller;

import com.travel.booking.application.dto.request.CreateBookingRequest;
import com.travel.booking.application.dto.response.BookingResponse;
import com.travel.booking.application.usecase.CancelBookingUseCase;
import com.travel.booking.application.usecase.CreateBookingUseCase;
import com.travel.booking.application.usecase.GetBookingUseCase;
import com.travel.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Booking lifecycle management")
public class BookingController {

    private final CreateBookingUseCase createUseCase;
    private final GetBookingUseCase    getUseCase;
    private final CancelBookingUseCase cancelUseCase;

    @PostMapping
    @Operation(summary = "Create a booking and start the reservation saga")
    public ResponseEntity<ApiResponse<BookingResponse>> create(
        @RequestHeader("X-User-Id") String userId,
        @Valid @RequestBody CreateBookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(createUseCase.execute(userId, request)));
    }

    @GetMapping("/{bookingId}")
    @Operation(summary = "Get booking by ID")
    public ResponseEntity<ApiResponse<BookingResponse>> getById(
        @PathVariable String bookingId,
        @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(
            ApiResponse.ok(getUseCase.execute(bookingId, userId)));
    }

    @GetMapping("/my")
    @Operation(summary = "List all bookings for the authenticated user")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getMyBookings(
        @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(
            ApiResponse.ok(getUseCase.executeForUser(userId)));
    }

    @PostMapping("/{bookingId}/cancel")
    @Operation(summary = "Cancel a booking")
    public ResponseEntity<ApiResponse<BookingResponse>> cancel(
        @PathVariable String bookingId,
        @RequestHeader("X-User-Id") String userId,
        @RequestParam(defaultValue = "Cancelled by user") String reason) {
        return ResponseEntity.ok(
            ApiResponse.ok(cancelUseCase.execute(bookingId, userId, reason)));
    }
}
