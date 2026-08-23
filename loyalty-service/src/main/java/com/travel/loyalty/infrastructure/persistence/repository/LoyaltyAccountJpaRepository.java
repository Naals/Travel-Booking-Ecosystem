package com.travel.loyalty.infrastructure.persistence.repository;

import com.travel.loyalty.infrastructure.persistence.entity.LoyaltyAccountJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoyaltyAccountJpaRepository extends JpaRepository<LoyaltyAccountJpaEntity, String> {
}
