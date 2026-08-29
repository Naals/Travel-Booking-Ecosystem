package com.travel.audit.infrastructure.persistence.repository;

import com.travel.audit.infrastructure.persistence.entity.AuditChainHeadJpaEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuditChainHeadJpaRepository extends JpaRepository<AuditChainHeadJpaEntity, String> {

    /**
     * PESSIMISTIC_WRITE translates to Postgres's SELECT ... FOR UPDATE
     * — every concurrent caller blocks here until the current holder's
     * transaction commits or rolls back. This is the entire
     * serialization mechanism behind the hash chain's correctness;
     * see ADR-015 for the throughput tradeoff this deliberately accepts.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT h FROM AuditChainHeadJpaEntity h WHERE h.id = :id")
    Optional<AuditChainHeadJpaEntity> lockById(@Param("id") String id);
}
