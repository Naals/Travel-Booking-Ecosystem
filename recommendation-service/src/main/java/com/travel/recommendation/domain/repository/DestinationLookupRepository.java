package com.travel.recommendation.domain.repository;

import com.travel.recommendation.domain.valueobject.DestinationKey;
import java.util.Optional;

/**
 * Internal correlation cache: resourceKey (propertyId / hotelId /
 * locationCode) → the destination it belongs to. Populated by
 * InventoryDestinationConsumer, read by AffinitySignalConsumer when a
 * booking completes. Not exposed via REST. See ADR-012 for why FLIGHT
 * resourceKeys are never populated here.
 */
public interface DestinationLookupRepository {
    void                    upsert(String resourceKey, DestinationKey destination);
    Optional<DestinationKey> findByResourceKey(String resourceKey);
}
