package com.travel.flight.application.usecase;

import com.travel.flight.application.dto.response.FlightResponse;
import com.travel.flight.domain.repository.FlightRepository;
import com.travel.flight.domain.valueobject.FlightId;
import com.travel.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetFlightUseCase {

    private final FlightRepository repository;

    @Transactional(readOnly = true)
    public FlightResponse execute(String flightId) {
        return repository.findById(FlightId.of(flightId))
            .map(FlightResponse::from)
            .orElseThrow(() -> new ResourceNotFoundException("Flight", flightId));
    }

    @Transactional(readOnly = true)
    public List<FlightResponse> executeSearch(String origin, String destination,
                                              LocalDate departureDate) {
        return repository.findByRouteAndDate(origin, destination, departureDate)
            .stream()
            .map(FlightResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<FlightResponse> executeByRoute(String origin, String destination) {
        return repository.findByRoute(origin, destination)
            .stream()
            .map(FlightResponse::from)
            .toList();
    }
}
