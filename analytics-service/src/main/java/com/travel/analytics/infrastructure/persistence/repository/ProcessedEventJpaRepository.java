package com.travel.analytics.infrastructure.persistence.repository;

import com.travel.analytics.infrastructure.persistence.entity.ProcessedEventJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessedEventJpaRepository extends JpaRepository<ProcessedEventJpaEntity, String> {

    /**
     * Postgres-native insert-if-absent — a third distinct idempotency
     * mechanism in this platform, after MongoDB's findAndModify
     * (review-service, Day 16) and a conditional UPDATE
     * (loyalty-service, Day 19). ON CONFLICT DO NOTHING is the natural
     * fit for "insert exactly once" on Postgres specifically, rather
     * than reusing either prior pattern out of habit.
     *
     * Returns 1 if the row was inserted (event is new), 0 if it
     * already existed (duplicate delivery).
     */
    @Modifying
    @Query(value = "INSERT INTO processed_events (event_id, processed_at) " +
        "VALUES (:eventId, now()) ON CONFLICT (event_id) DO NOTHING",
        nativeQuery = true)
    int tryInsert(String eventId);
}
