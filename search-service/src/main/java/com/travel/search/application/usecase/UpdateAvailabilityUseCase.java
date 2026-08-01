package com.travel.search.application.usecase;

import com.travel.search.domain.model.ListingType;
import com.travel.search.domain.repository.SearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Handles availability-related signals from inventory services.
 *
 * touchSignal() is used for events that only carry an ID (the common
 * case today — see ADR-007) and cannot be trusted to represent a
 * specific new state, since the same event class fires both when a
 * hold is placed and when it's released.
 *
 * markUnavailable() is used only where the event unambiguously carries
 * the new state (e.g. a flight transitioning to CANCELLED).
 */
@Service
@RequiredArgsConstructor
public class UpdateAvailabilityUseCase {

    private final SearchRepository repository;

    public void touchSignal(String listingId, ListingType type) {
        repository.touchAvailabilitySignal(listingId, type);
    }

    public void markUnavailable(String listingId, ListingType type) {
        repository.markUnavailable(listingId, type);
    }
}
