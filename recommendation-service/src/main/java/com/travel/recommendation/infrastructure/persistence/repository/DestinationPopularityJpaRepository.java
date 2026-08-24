package com.travel.recommendation.infrastructure.persistence.repository;

import com.travel.recommendation.infrastructure.persistence.entity.DestinationPopularityJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DestinationPopularityJpaRepository extends JpaRepository<DestinationPopularityJpaEntity, Long> {
    Optional<DestinationPopularityJpaEntity> findByCityAndCountry(String city, String country);

    /** Pageable carries the runtime limit — Spring Data method-name
     *  derivation can't express a dynamic "top N" directly. */
    List<DestinationPopularityJpaEntity> findAllByOrderByCompletedTripCountDesc(Pageable pageable);
}
