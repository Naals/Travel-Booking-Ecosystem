package com.travel.audit.application.usecase;

import com.travel.audit.application.dto.response.AuditLogEntryResponse;
import com.travel.audit.domain.repository.AuditLogRepository;
import com.travel.common.response.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Paginates at the database level via Spring Data's Pageable, not in
 * memory the way wallet-service's and loyalty-service's transaction
 * history use cases do (Days 18, 19) — a deliberate difference. Those
 * are naturally bounded per user; an audit trail is the least bounded
 * list in this platform, since a long-lived booking or a heavily
 * active user can accumulate entries indefinitely, so an in-memory
 * approach that was fine for one user's wallet transactions would be
 * the wrong choice here.
 */
@Service
@RequiredArgsConstructor
public class GetAuditTrailUseCase {

    private final AuditLogRepository repository;

    /** "Show me everything that happened to this booking, payment, or user-as-subject." */
    @Transactional(readOnly = true)
    public PagedResponse<AuditLogEntryResponse> executeBySubject(String subjectId, int page, int size) {
        var entries = repository.findBySubjectId(subjectId, page, size).stream()
            .map(AuditLogEntryResponse::from).toList();
        long total = repository.countBySubjectId(subjectId);
        return PagedResponse.of(entries, page, size, total);
    }

    /** "Show me everything this user has done or had done to them, across every subject." */
    @Transactional(readOnly = true)
    public PagedResponse<AuditLogEntryResponse> executeByUser(String userId, int page, int size) {
        var entries = repository.findByUserId(userId, page, size).stream()
            .map(AuditLogEntryResponse::from).toList();
        long total = repository.countByUserId(userId);
        return PagedResponse.of(entries, page, size, total);
    }
}
