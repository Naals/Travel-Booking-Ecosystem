package com.travel.flight.domain.repository;

import com.travel.flight.domain.aggregate.Flight;
import com.travel.flight.domain.valueobject.FlightId;
import com.travel.flight.domain.valueobject.FlightStatus;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FlightRepository {
    Flight           save(Flight flight);
    Optional<Flight> findById(FlightId id);
    List<Flight>     findByRoute(String originCode, String destinationCode);
    List<Flight>     findByRouteAndDate(String originCode, String destinationCode,
                                        LocalDate departureDate);
    List<Flight>     findByStatus(FlightStatus status);
    boolean          existsById(FlightId id);
}
