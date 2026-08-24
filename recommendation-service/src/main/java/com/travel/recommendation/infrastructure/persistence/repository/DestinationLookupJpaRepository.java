package com.travel.recommendation.infrastructure.persistence.repository;

import com.travel.recommendation.infrastructure.persistence.entity.DestinationLookupJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DestinationLookupJpaRepository extends JpaRepository<DestinationLookupJpaEntity, String> {
}
