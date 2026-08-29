package com.travel.audit.infrastructure.persistence.repository;

import com.travel.audit.infrastructure.persistence.entity.AuditLogEntryJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogEntryJpaRepository extends JpaRepository<AuditLogEntryJpaEntity, String> {
    boolean existsBySourceEventId(String sourceEventId);
    Page<AuditLogEntryJpaEntity> findBySubjectId(String subjectId, Pageable pageable);
    long                          countBySubjectId(String subjectId);
    Page<AuditLogEntryJpaEntity> findByUserId(String userId, Pageable pageable);
    long                          countByUserId(String userId);
    List<AuditLogEntryJpaEntity>  findAllByOrderBySequenceNumberAsc();
}
