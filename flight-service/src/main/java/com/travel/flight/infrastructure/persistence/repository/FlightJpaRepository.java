package com.travel.flight.infrastructure.persistence.repository;

import com.travel.flight.domain.valueobject.FlightStatus;
import com.travel.flight.infrastructure.persistence.entity.FlightJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FlightJpaRepository extends JpaRepository<FlightJpaEntity, String> {

    List<FlightJpaEntity> findByOriginCodeAndDestinationCode(
        String originCode, String destinationCode);

    @Query("SELECT f FROM FlightJpaEntity f " +
        "WHERE f.originCode = :origin " +
        "AND f.destinationCode = :destination " +
        "AND CAST(f.departureTime AS date) = :departureDate")
    List<FlightJpaEntity> findByRouteAndDate(
        String origin, String destination, LocalDate departureDate);

    List<FlightJpaEntity> findByStatus(FlightStatus status);
}
