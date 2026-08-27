package com.travel.analytics.infrastructure.persistence;

import com.travel.analytics.domain.repository.EventDeduplicationRepository;
import com.travel.analytics.infrastructure.persistence.repository.ProcessedEventJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventDeduplicationRepositoryAdapter implements EventDeduplicationRepository {

    private final ProcessedEventJpaRepository jpa;

    @Override
    public boolean markProcessedIfNew(String eventId) {
        return jpa.tryInsert(eventId) == 1;
    }
}
