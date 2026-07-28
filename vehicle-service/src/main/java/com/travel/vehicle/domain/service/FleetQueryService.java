package com.travel.vehicle.domain.service;

import com.travel.vehicle.domain.aggregate.Vehicle;
import com.travel.vehicle.domain.repository.VehicleRepository;
import com.travel.vehicle.domain.valueobject.DateRange;
import com.travel.vehicle.domain.valueobject.VehicleCategory;
import com.travel.common.exception.BusinessRuleViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Domain service for fleet availability queries.
 *
 * Crosses multiple Vehicle aggregates to find the first available
 * vehicle of a requested category at a given location.
 * Called by the saga consumer to resolve which specific vehicle
 * to assign to a VEHICLE type booking.
 */
@Service
@RequiredArgsConstructor
public class FleetQueryService {

    private final VehicleRepository repository;

    /**
     * Finds the first available vehicle of the requested category
     * at the given pickup location for the requested rental period.
     */
    public Vehicle findFirstAvailable(VehicleCategory category,
                                      String locationCode,
                                      LocalDate pickupDate,
                                      LocalDate returnDate) {
        DateRange period = DateRange.of(pickupDate, returnDate);

        return repository.findByCategoryAndLocationCode(category, locationCode)
            .stream()
            .filter(v -> v.isAvailableFor(period))
            .findFirst()
            .orElseThrow(() -> new BusinessRuleViolationException(
                "No " + category.name() + " vehicles available at " +
                    locationCode + " for the requested dates",
                "NO_VEHICLES_AVAILABLE"));
    }

    /**
     * Lists all available vehicles at a location for a given rental period.
     * Used by the search endpoint to show options to customers.
     */
    public List<Vehicle> findAllAvailable(String locationCode,
                                          LocalDate pickupDate,
                                          LocalDate returnDate) {
        DateRange period = DateRange.of(pickupDate, returnDate);
        return repository.findByLocationCode(locationCode)
            .stream()
            .filter(v -> v.isAvailableFor(period))
            .toList();
    }
}
