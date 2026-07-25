package com.travel.flight.application.usecase;

import com.travel.flight.application.dto.request.ScheduleFlightRequest;
import com.travel.flight.application.dto.response.FlightResponse;
import com.travel.flight.domain.aggregate.Flight;
import com.travel.flight.domain.repository.FlightRepository;
import com.travel.flight.domain.valueobject.Route;
import com.travel.flight.infrastructure.messaging.producer.FlightEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleFlightUseCase {

    private final FlightRepository    repository;
    private final FlightEventPublisher eventPublisher;

    @Transactional
    public FlightResponse execute(ScheduleFlightRequest request) {
        log.info("Scheduling flight {} route {}-{}",
            request.flightNumber(),
            request.originCode(),
            request.destinationCode());

        Route route = Route.of(
            request.originCode(), request.destinationCode(),
            request.originCity(), request.destinationCity(),
            request.departureTime(), request.arrivalTime());

        Flight flight = Flight.schedule(
            request.airlineCode(), request.flightNumber(), route);

        Flight saved = repository.save(flight);

        // FlightScheduledEvent → search-service indexes this flight
        eventPublisher.publishEvents(saved.getDomainEvents());
        saved.clearDomainEvents();

        log.info("Flight scheduled: {}", saved.getId().getValue());
        return FlightResponse.from(saved);
    }
}
