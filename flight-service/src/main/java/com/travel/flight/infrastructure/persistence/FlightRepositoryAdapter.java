package com.travel.flight.infrastructure.persistence;

import com.travel.flight.domain.aggregate.Flight;
import com.travel.flight.domain.repository.FlightRepository;
import com.travel.flight.domain.valueobject.FlightId;
import com.travel.flight.domain.valueobject.FlightStatus;
import com.travel.flight.infrastructure.persistence.mapper.FlightMapper;
import com.travel.flight.infrastructure.persistence.repository.FlightJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FlightRepositoryAdapter implements FlightRepository {

    private final FlightJpaRepository jpa;
    private final FlightMapper        mapper;

    @Override public Flight           save(Flight f)               { return mapper.toDomain(jpa.save(mapper.toEntity(f))); }
    @Override public Optional<Flight> findById(FlightId id)        { return jpa.findById(id.getValue()).map(mapper::toDomain); }
    @Override public List<Flight>     findByRoute(String o, String d) {
        return jpa.findByOriginCodeAndDestinationCode(o, d).stream().map(mapper::toDomain).toList();
    }
    @Override public List<Flight>     findByRouteAndDate(String o, String d, LocalDate date) {
        return jpa.findByRouteAndDate(o, d, date).stream().map(mapper::toDomain).toList();
    }
    @Override public List<Flight>     findByStatus(FlightStatus s)  { return jpa.findByStatus(s).stream().map(mapper::toDomain).toList(); }
    @Override public boolean          existsById(FlightId id)        { return jpa.existsById(id.getValue()); }
}
