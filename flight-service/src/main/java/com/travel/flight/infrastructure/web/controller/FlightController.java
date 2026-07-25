package com.travel.flight.infrastructure.web.controller;

import com.travel.common.response.ApiResponse;
import com.travel.flight.application.dto.request.AddSeatsRequest;
import com.travel.flight.application.dto.request.ScheduleFlightRequest;
import com.travel.flight.application.dto.response.FlightResponse;
import com.travel.flight.application.usecase.*;
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
@RequestMapping("/api/v1/flights")
@RequiredArgsConstructor
@Tag(name = "Flights", description = "Flight scheduling and seat management")
public class FlightController {

    private final ScheduleFlightUseCase scheduleUseCase;
    private final AddSeatsUseCase       addSeatsUseCase;
    private final GetFlightUseCase      getUseCase;

    @PostMapping
    @Operation(summary = "Schedule a new flight")
    public ResponseEntity<ApiResponse<FlightResponse>> schedule(
        @Valid @RequestBody ScheduleFlightRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(scheduleUseCase.execute(request)));
    }

    @PostMapping("/{flightId}/seats")
    @Operation(summary = "Add seats to a flight")
    public ResponseEntity<ApiResponse<FlightResponse>> addSeats(
        @PathVariable String flightId,
        @Valid @RequestBody AddSeatsRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(addSeatsUseCase.execute(flightId, request)));
    }

    @GetMapping("/{flightId}")
    @Operation(summary = "Get flight by ID with seat availability")
    public ResponseEntity<ApiResponse<FlightResponse>> getById(
        @PathVariable String flightId) {
        return ResponseEntity.ok(ApiResponse.ok(getUseCase.execute(flightId)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search flights by route and departure date")
    public ResponseEntity<ApiResponse<List<FlightResponse>>> search(
        @RequestParam String origin,
        @RequestParam String destination,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departureDate) {
        List<FlightResponse> results = departureDate != null
            ? getUseCase.executeSearch(origin, destination, departureDate)
            : getUseCase.executeByRoute(origin, destination);
        return ResponseEntity.ok(ApiResponse.ok(results));
    }
}
