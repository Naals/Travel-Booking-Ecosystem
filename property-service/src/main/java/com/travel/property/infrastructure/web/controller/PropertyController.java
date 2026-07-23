package com.travel.property.infrastructure.web.controller;

import com.travel.common.response.ApiResponse;
import com.travel.property.application.dto.request.CreatePropertyRequest;
import com.travel.property.application.dto.response.PropertyResponse;
import com.travel.property.application.usecase.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/properties")
@RequiredArgsConstructor
@Tag(name = "Properties", description = "Property listing management")
public class PropertyController {

    private final CreatePropertyUseCase  createUseCase;
    private final PublishPropertyUseCase publishUseCase;
    private final GetPropertyUseCase     getUseCase;
    private final CheckAvailabilityUseCase availabilityUseCase;

    @PostMapping
    @Operation(summary = "Create a new property listing")
    public ResponseEntity<ApiResponse<PropertyResponse>> create(
        @RequestHeader("X-User-Id") String hostId,
        @Valid @RequestBody CreatePropertyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(createUseCase.execute(hostId, request)));
    }

    @PostMapping("/{propertyId}/publish")
    @Operation(summary = "Publish a property (DRAFT → ACTIVE)")
    public ResponseEntity<ApiResponse<PropertyResponse>> publish(
        @PathVariable String propertyId,
        @RequestHeader("X-User-Id") String hostId) {
        return ResponseEntity.ok(
            ApiResponse.ok(publishUseCase.execute(propertyId, hostId)));
    }

    @GetMapping("/{propertyId}")
    @Operation(summary = "Get property by ID")
    public ResponseEntity<ApiResponse<PropertyResponse>> getById(
        @PathVariable String propertyId) {
        return ResponseEntity.ok(
            ApiResponse.ok(getUseCase.execute(propertyId)));
    }

    @GetMapping("/my")
    @Operation(summary = "List all properties for the authenticated host")
    public ResponseEntity<ApiResponse<List<PropertyResponse>>> getMyProperties(
        @RequestHeader("X-User-Id") String hostId) {
        return ResponseEntity.ok(
            ApiResponse.ok(getUseCase.executeForHost(hostId)));
    }

    @GetMapping("/{propertyId}/availability")
    @Operation(summary = "Check if property is available for given dates")
    public ResponseEntity<ApiResponse<Boolean>> checkAvailability(
        @PathVariable String propertyId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {
        boolean available = availabilityUseCase.execute(propertyId, checkIn, checkOut);
        return ResponseEntity.ok(ApiResponse.ok(available));
    }
}
