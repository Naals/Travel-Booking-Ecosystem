package com.travel.flight.application.dto.request;

import jakarta.validation.constraints.*;
import java.time.ZonedDateTime;

public record ScheduleFlightRequest(

    @NotBlank(message = "Airline code is required")
    @Size(min = 2, max = 3, message = "Airline code must be 2-3 characters (IATA)")
    String airlineCode,

    @NotBlank(message = "Flight number is required")
    String flightNumber,

    @NotBlank @Size(min = 3, max = 3, message = "Origin must be IATA 3-letter code")
    String originCode,

    @NotBlank @Size(min = 3, max = 3, message = "Destination must be IATA 3-letter code")
    String destinationCode,

    @NotBlank String originCity,
    @NotBlank String destinationCity,

    @NotNull ZonedDateTime departureTime,
    @NotNull ZonedDateTime arrivalTime
) {}
