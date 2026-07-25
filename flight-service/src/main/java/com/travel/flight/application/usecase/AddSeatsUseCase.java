package com.travel.flight.application.usecase;

import com.travel.flight.application.dto.request.AddSeatsRequest;
import com.travel.flight.application.dto.response.FlightResponse;
import com.travel.flight.domain.aggregate.Flight;
import com.travel.flight.domain.repository.FlightRepository;
import com.travel.flight.domain.valueobject.FlightId;
import com.travel.flight.domain.valueobject.Money;
import com.travel.flight.domain.valueobject.SeatClass;
import com.travel.flight.infrastructure.messaging.producer.FlightEventPublisher;
import com.travel.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddSeatsUseCase {

    private final FlightRepository    repository;
    private final FlightEventPublisher eventPublisher;

    @Transactional
    public FlightResponse execute(String flightId, AddSeatsRequest request) {
        Flight flight = repository.findById(FlightId.of(flightId))
            .orElseThrow(() -> new ResourceNotFoundException("Flight", flightId));

        List<Flight.SeatConfiguration> configs = request.seats().stream()
            .map(s -> new Flight.SeatConfiguration(
                s.seatNumber(),
                SeatClass.valueOf(s.seatClass()),
                Money.of(s.price(), s.currency())))
            .toList();

        flight.loadSeats(configs);
        Flight saved = repository.save(flight);

        eventPublisher.publishEvents(saved.getDomainEvents());
        saved.clearDomainEvents();

        log.info("Added {} seats to flight {}", request.seats().size(), flightId);
        return FlightResponse.from(saved);
    }
}
