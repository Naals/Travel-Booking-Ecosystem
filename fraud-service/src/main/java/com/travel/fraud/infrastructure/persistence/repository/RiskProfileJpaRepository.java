package com.travel.fraud.infrastructure.persistence.repository;

import com.travel.fraud.infrastructure.persistence.entity.RiskProfileJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RiskProfileJpaRepository extends JpaRepository<RiskProfileJpaEntity, String> {
}
