package com.travel.vehicle.application.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Summary of available vehicles grouped by category at a location.
 */
public record FleetAvailabilityResponse(
    String locationCode,
    String city,
    String pickupDate,
    String returnDate,
    Map<String, CategorySummary> availability
) {
    public record CategorySummary(
        int          count,
        BigDecimal   fromPrice,
        String       currency,
        List<String> features
    ) {}
}
