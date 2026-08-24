package com.travel.recommendation.application.usecase;

import com.travel.recommendation.domain.model.AffinitySignalType;
import com.travel.recommendation.domain.model.UserAffinity;
import com.travel.recommendation.domain.repository.UserAffinityRepository;
import com.travel.recommendation.domain.valueobject.DestinationKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecordAffinitySignalUseCase {

    private final UserAffinityRepository repository;

    @Transactional
    public void execute(String userId, DestinationKey destination, AffinitySignalType signalType) {
        UserAffinity affinity = repository.findByUserIdAndDestination(userId, destination)
            .orElseGet(() -> UserAffinity.initial(userId, destination));
        affinity.recordSignal(signalType);
        repository.save(affinity);
    }
}
