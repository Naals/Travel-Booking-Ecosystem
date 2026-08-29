package com.travel.audit.infrastructure.persistence;

import com.travel.audit.domain.model.AuditLogEntry;
import com.travel.audit.domain.repository.AuditLogRepository;
import com.travel.audit.infrastructure.persistence.mapper.AuditLogEntryMapper;
import com.travel.audit.infrastructure.persistence.repository.AuditLogEntryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AuditLogRepositoryAdapter implements AuditLogRepository {

    private final AuditLogEntryJpaRepository jpa;
    private final AuditLogEntryMapper        mapper;

    @Override
    public void insert(AuditLogEntry entry) {
        jpa.save(mapper.toEntity(entry));
    }

    @Override
    public boolean existsBySourceEventId(String sourceEventId) {
        return jpa.existsBySourceEventId(sourceEventId);
    }

    @Override
    public List<AuditLogEntry> findBySubjectId(String subjectId, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "sequenceNumber"));
        return jpa.findBySubjectId(subjectId, pageable).stream().map(mapper::toDomain).toList();
    }

    @Override public long countBySubjectId(String subjectId) { return jpa.countBySubjectId(subjectId); }

    @Override
    public List<AuditLogEntry> findByUserId(String userId, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "sequenceNumber"));
        return jpa.findByUserId(userId, pageable).stream().map(mapper::toDomain).toList();
    }

    @Override public long countByUserId(String userId) { return jpa.countByUserId(userId); }

    @Override
    public List<AuditLogEntry> findAllOrderedBySequence() {
        return jpa.findAllByOrderBySequenceNumberAsc().stream().map(mapper::toDomain).toList();
    }
}
