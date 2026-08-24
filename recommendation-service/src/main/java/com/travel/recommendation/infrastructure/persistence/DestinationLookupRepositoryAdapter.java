package com.travel.recommendation.infrastructure.persistence;

import com.travel.recommendation.domain.repository.DestinationLookupRepository;
import com.travel.recommendation.domain.valueobject.DestinationKey;
import com.travel.recommendation.infrastructure.persistence.entity.DestinationLookupJpaEntity;
import com.travel.recommendation.infrastructure.persistence.repository.DestinationLookupJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DestinationLookupRepositoryAdapter implements DestinationLookupRepository {

    private final DestinationLookupJpaRepository jpa;

    @Override
    public void upsert(String resourceKey, DestinationKey destination) {
        var entity = jpa.findById(resourceKey)
            .map(e -> { e.setCity(destination.getCity()); e.setCountry(destination.getCountry()); e.setUpdatedAt(Instant.now()); return e; })
            .orElseGet(() -> DestinationLookupJpaEntity.builder()
                .resourceKey(resourceKey)
                .city(destination.getCity())
                .country(destination.getCountry())
                .updatedAt(Instant.now())
                .build());
        jpa.save(entity);
    }

    @Override
    public Optional<DestinationKey> findByResourceKey(String resourceKey) {
        return jpa.findById(resourceKey).map(e -> DestinationKey.of(e.getCity(), e.getCountry()));
    }
}
