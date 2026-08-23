package com.travel.loyalty.infrastructure.persistence.repository;

import com.travel.loyalty.infrastructure.persistence.entity.SpendRecordJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SpendRecordJpaRepository extends JpaRepository<SpendRecordJpaEntity, String> {

    /**
     * Conditional UPDATE guarding the race — the row count returned by
     * @Modifying is what makes this atomic: two concurrent calls for
     * the same bookingId can only ever have one of them return 1.
     * The relational counterpart to MongoDB's findAndModify (Day 16).
     */
    @Modifying
    @Query("UPDATE SpendRecordJpaEntity s SET s.consumed = true " +
        "WHERE s.bookingId = :bookingId AND s.consumed = false")
    int markConsumed(String bookingId);
}
