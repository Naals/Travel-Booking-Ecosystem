package com.travel.hotel.infrastructure.web.controller;

import com.travel.common.response.ApiResponse;
import com.travel.hotel.application.dto.request.AddRoomRequest;
import com.travel.hotel.application.dto.request.CreateHotelRequest;
import com.travel.hotel.application.dto.response.HotelResponse;
import com.travel.hotel.application.usecase.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hotels")
@RequiredArgsConstructor
@Tag(name = "Hotels", description = "Hotel and room management")
public class HotelController {

    private final CreateHotelUseCase   createUseCase;
    private final ActivateHotelUseCase activateUseCase;
    private final AddRoomUseCase       addRoomUseCase;
    private final GetHotelUseCase      getUseCase;

    @PostMapping
    @Operation(summary = "Create a new hotel")
    public ResponseEntity<ApiResponse<HotelResponse>> create(
        @RequestHeader("X-User-Id") String managerId,
        @Valid @RequestBody CreateHotelRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(createUseCase.execute(managerId, request)));
    }

    @PostMapping("/{hotelId}/activate")
    @Operation(summary = "Activate a hotel (DRAFT → ACTIVE)")
    public ResponseEntity<ApiResponse<HotelResponse>> activate(
        @PathVariable String hotelId,
        @RequestHeader("X-User-Id") String managerId) {
        return ResponseEntity.ok(
            ApiResponse.ok(activateUseCase.execute(hotelId, managerId)));
    }

    @PostMapping("/{hotelId}/rooms")
    @Operation(summary = "Add a room to a hotel")
    public ResponseEntity<ApiResponse<HotelResponse>> addRoom(
        @PathVariable String hotelId,
        @RequestHeader("X-User-Id") String managerId,
        @Valid @RequestBody AddRoomRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(addRoomUseCase.execute(hotelId, managerId, request)));
    }

    @GetMapping("/{hotelId}")
    @Operation(summary = "Get hotel by ID")
    public ResponseEntity<ApiResponse<HotelResponse>> getById(
        @PathVariable String hotelId) {
        return ResponseEntity.ok(ApiResponse.ok(getUseCase.execute(hotelId)));
    }

    @GetMapping("/my")
    @Operation(summary = "List hotels managed by the authenticated manager")
    public ResponseEntity<ApiResponse<List<HotelResponse>>> getMyHotels(
        @RequestHeader("X-User-Id") String managerId) {
        return ResponseEntity.ok(ApiResponse.ok(getUseCase.executeForManager(managerId)));
    }

    @GetMapping
    @Operation(summary = "Search hotels by city")
    public ResponseEntity<ApiResponse<List<HotelResponse>>> getByCity(
        @RequestParam String city) {
        return ResponseEntity.ok(ApiResponse.ok(getUseCase.executeByCity(city)));
    }
}
