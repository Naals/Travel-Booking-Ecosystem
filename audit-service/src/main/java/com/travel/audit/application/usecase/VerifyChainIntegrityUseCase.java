package com.travel.audit.application.usecase;

import com.travel.audit.application.dto.response.ChainIntegrityResponse;
import com.travel.audit.domain.repository.AuditLogRepository;
import com.travel.audit.domain.service.ChainIntegrityVerifier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VerifyChainIntegrityUseCase {

    private final AuditLogRepository repository;

    @Transactional(readOnly = true)
    public ChainIntegrityResponse execute() {
        var report = ChainIntegrityVerifier.verify(repository.findAllOrderedBySequence());
        return ChainIntegrityResponse.from(report);
    }
}
