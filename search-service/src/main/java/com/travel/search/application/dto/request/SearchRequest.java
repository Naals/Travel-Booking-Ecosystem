package com.travel.search.application.dto.request;

import java.math.BigDecimal;

/**
 * REST-facing search request. Kept as simple types (String, not enums)
 * so invalid values surface as a friendly 400 from SearchCriteria's
 * validation rather than a Spring binding error.
 */
public record SearchRequest(
    String     keyword,
    String     type,          // ListingType name, nullable = all types
    String     city,
    BigDecimal priceMin,
    BigDecimal priceMax,
    Double     minRating,
    boolean    onlyAvailable,
    Double     lat,
    Double     lng,
    Double     radiusKm,
    String     sortBy,        // SortOption name
    int        page,
    int        size
) {
    public SearchRequest {
        if (page < 0) page = 0;
        if (size <= 0) size = 20;
    }
}
