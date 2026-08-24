package com.travel.recommendation.infrastructure.persistence;

import com.travel.recommendation.domain.model.DestinationPopularity;
import com.travel.recommendation.domain.repository.DestinationPopularityRepository;
import com.travel.recommendation.domain.valueobject.DestinationKey;
import com.travel.recommendation.infrastructure.persistence.entity.DestinationPopularityJpaEntity;
import com.travel.recommendation.infrastructure.persistence.repository.DestinationPopularityJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DestinationPopularityRepositoryAdapter implements DestinationPopularityRepository {

    private final DestinationPopularityJpaRepository jpa;

    @Override
    public DestinationPopularity save(DestinationPopularity p) {
        var existing = jpa.findByCityAndCountry(p.getDestination().getCity(), p.getDestination().getCountry());

        var entity = existing
            .map(e -> { e.setCompletedTripCount(p.getCompletedTripCount()); e.setLastUpdatedAt(p.getLastUpdatedAt()); return e; })
            .orElseGet(() -> DestinationPopularityJpaEntity.builder()
                .city(p.getDestination().getCity())
                .country(p.getDestination().getCountry())
                .completedTripCount(p.getCompletedTripCount())
                .lastUpdatedAt(p.getLastUpdatedAt())
                .build());

        return toDomain(jpa.save(entity));
    }

    @Override
    public Optional<DestinationPopularity> findByDestination(DestinationKey destination) {
        return jpa.findByCityAndCountry(destination.getCity(), destination.getCountry()).map(this::toDomain);
    }

    @Override
    public List<DestinationPopularity> findTopByCompletedTripCount(int limit) {
        return jpa.findAllByOrderByCompletedTripCountDesc(PageRequest.of(0, limit))
            .stream().map(this::toDomain).toList();
    }

    private DestinationPopularity toDomain(DestinationPopularityJpaEntity e) {
        return DestinationPopularity.reconstitute(
            DestinationKey.of(e.getCity(), e.getCountry()),
            e.getCompletedTripCount(), e.getLastUpdatedAt());
    }
}
