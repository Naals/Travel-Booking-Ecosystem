package com.travel.search.application.usecase;

import com.travel.search.domain.model.SearchDocument;
import com.travel.search.domain.repository.SearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Indexes a listing built from an inbound "listing created" event.
 * Kafka consumers construct the SearchDocument from event-specific
 * fields and delegate here — this class stays event-agnostic.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IndexListingUseCase {

    private final SearchRepository repository;

    public void execute(SearchDocument document) {
        repository.index(document);
        log.info("Indexed {} listing: {}", document.getListingType(), document.getId());
    }
}
