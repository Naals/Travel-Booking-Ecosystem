package com.travel.recommendation.infrastructure.persistence;

import com.travel.recommendation.domain.model.UserAffinity;
import com.travel.recommendation.domain.repository.UserAffinityRepository;
import com.travel.recommendation.domain.valueobject.DestinationKey;
import com.travel.recommendation.infrastructure.persistence.entity.UserAffinityJpaEntity;
import com.travel.recommendation.infrastructure.persistence.repository.UserAffinityJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserAffinityRepositoryAdapter implements UserAffinityRepository {

    private final UserAffinityJpaRepository jpa;

    @Override
    public UserAffinity save(UserAffinity a) {
        var existing = jpa.findByUserIdAndCityAndCountry(
            a.getUserId(), a.getDestination().getCity(), a.getDestination().getCountry());

        var entity = existing
            .map(e -> { e.setScore(a.getScore()); e.setLastSignalAt(a.getLastSignalAt()); return e; })
            .orElseGet(() -> UserAffinityJpaEntity.builder()
                .userId(a.getUserId())
                .city(a.getDestination().getCity())
                .country(a.getDestination().getCountry())
                .score(a.getScore())
                .lastSignalAt(a.getLastSignalAt())
                .build());

        var saved = jpa.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<UserAffinity> findByUserIdAndDestination(String userId, DestinationKey destination) {
        return jpa.findByUserIdAndCityAndCountry(userId, destination.getCity(), destination.getCountry())
            .map(this::toDomain);
    }

    @Override
    public List<UserAffinity> findByUserId(String userId) {
        return jpa.findByUserId(userId).stream().map(this::toDomain).toList();
    }

    private UserAffinity toDomain(UserAffinityJpaEntity e) {
        return UserAffinity.reconstitute(
            e.getUserId(), DestinationKey.of(e.getCity(), e.getCountry()),
            e.getScore(), e.getLastSignalAt());
    }
}
