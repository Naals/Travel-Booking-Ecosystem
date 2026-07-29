package com.travel.search.domain.repository;

import com.travel.search.domain.model.ListingType;
import com.travel.search.domain.model.SearchDocument;
import com.travel.search.domain.model.SearchResultPage;
import com.travel.search.domain.valueobject.SearchCriteria;

/**
 * Search repository port (domain interface).
 * Implemented by SearchRepositoryAdapter against Elasticsearch.
 */
public interface SearchRepository {

    /**
     * Indexes (creates or fully replaces) a listing document.
     * Idempotent by design — indexing by the same id twice simply
     * overwrites, which is why saga-style idempotency guards are not
     * needed here the way they are in the transactional services.
     */
    void index(SearchDocument document);

    /**
     * Records that an availability-related event was received for this
     * listing, without asserting a specific new state (see ADR-007).
     * Updates lastAvailabilityEventAt as a staleness/reconciliation signal.
     */
    void touchAvailabilitySignal(String id, ListingType type);

    /**
     * Marks a listing as unavailable outright — used only for events
     * that carry unambiguous state (e.g. a flight status transitioning
     * to CANCELLED).
     */
    void markUnavailable(String id, ListingType type);

    void delete(String id, ListingType type);

    SearchResultPage<SearchDocument> search(SearchCriteria criteria);
}
