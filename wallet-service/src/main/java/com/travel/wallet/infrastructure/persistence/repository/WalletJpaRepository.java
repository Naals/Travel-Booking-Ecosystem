package com.travel.wallet.infrastructure.persistence.repository;

import com.travel.wallet.infrastructure.persistence.entity.WalletJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletJpaRepository extends JpaRepository<WalletJpaEntity, String> {
}
