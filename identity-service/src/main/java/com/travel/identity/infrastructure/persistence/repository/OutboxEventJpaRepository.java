package com.travel.identity.infrastructure.persistence.repository;

import com.travel.identity.infrastructure.persistence.entity.OutboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OutboxEventJpaRepository extends JpaRepository<OutboxEventEntity, String> {

    @Query("SELECT o FROM OutboxEventEntity o " +
        "WHERE o.processed = false AND o.retryCount < :maxRetries " +
        "ORDER BY o.createdAt ASC")
    List<OutboxEventEntity> findPendingEvents(int maxRetries);

    @Modifying
    @Query("UPDATE OutboxEventEntity o " +
        "SET o.processed = true, o.processedAt = CURRENT_TIMESTAMP " +
        "WHERE o.id = :id")
    void markAsProcessed(String id);
}
