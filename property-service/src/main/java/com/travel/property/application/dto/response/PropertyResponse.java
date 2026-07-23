package com.travel.property.application.dto.response;

import com.travel.property.domain.aggregate.Property;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

public record PropertyResponse(
    String     propertyId,
    String     hostId,
    String     title,
    String     description,
    String     propertyType,
    String     status,
    String     street,
    String     city,
    String     state,
    String     country,
    double     latitude,
    double     longitude,
    BigDecimal nightlyRate,
    String     currency,
    int        maxGuests,
    int        bedrooms,
    int        bathrooms,
    Set<String> amenities,
    Instant    createdAt
) {
    public static PropertyResponse from(Property p) {
        return new PropertyResponse(
            p.getId().getValue(),
            p.getHostId(),
            p.getTitle(),
            p.getDescription(),
            p.getType().name(),
            p.getStatus().name(),
            p.getAddress().getStreet(),
            p.getAddress().getCity(),
            p.getAddress().getState(),
            p.getAddress().getCountry(),
            p.getAddress().getLatitude(),
            p.getAddress().getLongitude(),
            p.getNightlyRate().getAmount(),
            p.getNightlyRate().getCurrency(),
            p.getMaxGuests(),
            p.getBedrooms(),
            p.getBathrooms(),
            p.getAmenities().stream().map(Enum::name).collect(Collectors.toSet()),
            p.getCreatedAt()
        );
    }
}
