package com.travel.vehicle.infrastructure.web.controller;

import com.travel.common.response.ApiResponse;
import com.travel.vehicle.application.dto.request.AddVehicleRequest;
import com.travel.vehicle.application.dto.response.VehicleResponse;
import com.travel.vehicle.application.usecase.AddVehicleToFleetUseCase;
import com.travel.vehicle.application.usecase.GetVehicleUseCase;
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
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
@Tag(name = "Vehicles", description = "Vehicle fleet and rental management")
public class VehicleController {

    private final AddVehicleToFleetUseCase addUseCase;
    private final GetVehicleUseCase        getUseCase;

    @PostMapping
    @Operation(summary = "Add a vehicle to the rental fleet")
    public ResponseEntity<ApiResponse<VehicleResponse>> addToFleet(
        @Valid @RequestBody AddVehicleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(addUseCase.execute(request)));
    }

    @GetMapping("/{vehicleId}")
    @Operation(summary = "Get vehicle by ID")
    public ResponseEntity<ApiResponse<VehicleResponse>> getById(
        @PathVariable String vehicleId) {
        return ResponseEntity.ok(ApiResponse.ok(getUseCase.execute(vehicleId)));
    }

    @GetMapping("/available")
    @Operation(summary = "Search available vehicles by location and rental period")
    public ResponseEntity<ApiResponse<List<VehicleResponse>>> searchAvailable(
        @RequestParam String locationCode,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate pickupDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate returnDate) {
        return ResponseEntity.ok(
            ApiResponse.ok(getUseCase.executeAvailable(locationCode, pickupDate, returnDate)));
    }

    @GetMapping
    @Operation(summary = "List all vehicles at a location")
    public ResponseEntity<ApiResponse<List<VehicleResponse>>> getByLocation(
        @RequestParam String locationCode) {
        return ResponseEntity.ok(
            ApiResponse.ok(getUseCase.executeByLocation(locationCode)));
    }
}
