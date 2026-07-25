package com.travel.flight.application.dto.response;

import com.travel.flight.domain.aggregate.Flight;
import com.travel.flight.domain.valueobject.SeatClass;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Map;

public record FlightResponse(
    String         flightId,
    String         airlineCode,
    String         flightNumber,
    String         originCode,
    String         destinationCode,
    String         originCity,
    String         destinationCity,
    ZonedDateTime  departureTime,
    ZonedDateTime  arrivalTime,
    long           durationMinutes,
    String         status,
    String         delayReason,
    int            totalSeats,
    Map<String, SeatClassSummary> seatAvailability
) {
    public static FlightResponse from(Flight f) {
        Map<String, SeatClassSummary> availability = Map.of(
            SeatClass.ECONOMY.name(),    buildSummary(f, SeatClass.ECONOMY),
            SeatClass.BUSINESS.name(),   buildSummary(f, SeatClass.BUSINESS),
            SeatClass.FIRST_CLASS.name(),buildSummary(f, SeatClass.FIRST_CLASS)
        );

        return new FlightResponse(
            f.getId().getValue(),
            f.getAirlineCode(),
            f.getFlightNumber(),
            f.getRoute().getOriginCode(),
            f.getRoute().getDestinationCode(),
            f.getRoute().getOriginCity(),
            f.getRoute().getDestinationCity(),
            f.getRoute().getDepartureTime(),
            f.getRoute().getArrivalTime(),
            f.getRoute().flightDuration().toMinutes(),
            f.getStatus().name(),
            f.getDelayReason(),
            f.getSeats().size(),
            availability
        );
    }

    private static SeatClassSummary buildSummary(Flight f, SeatClass sc) {
        long total     = f.getSeats().stream().filter(s -> s.getSeatClass() == sc).count();
        long available = f.availableSeatCount(sc);
        BigDecimal minPrice = f.getSeats().stream()
            .filter(s -> s.getSeatClass() == sc)
            .map(s -> s.getPrice().getAmount())
            .min(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);
        return new SeatClassSummary(total, available, minPrice);
    }

    public record SeatClassSummary(long total, long available, BigDecimal fromPrice) {}
}
