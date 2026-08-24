package com.travel.recommendation.infrastructure.persistence.repository;

import com.travel.recommendation.infrastructure.persistence.entity.UserAffinityJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAffinityJpaRepository extends JpaRepository<UserAffinityJpaEntity, Long> {
    Optional<UserAffinityJpaEntity> findByUserIdAndCityAndCountry(String userId, String city, String country);
    List<UserAffinityJpaEntity>     findByUserId(String userId);
}
