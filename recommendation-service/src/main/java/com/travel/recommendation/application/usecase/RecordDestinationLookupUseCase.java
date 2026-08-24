package com.travel.recommendation.application.usecase;

import com.travel.recommendation.domain.repository.DestinationLookupRepository;
import com.travel.recommendation.domain.valueobject.DestinationKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecordDestinationLookupUseCase {

    private final DestinationLookupRepository repository;

    @Transactional
    public void execute(String resourceKey, DestinationKey destination) {
        repository.upsert(resourceKey, destination);
    }
}
