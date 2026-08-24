package com.travel.recommendation.application.usecase;

import com.travel.recommendation.domain.model.DestinationPopularity;
import com.travel.recommendation.domain.repository.DestinationPopularityRepository;
import com.travel.recommendation.domain.valueobject.DestinationKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecordPopularitySignalUseCase {

    private final DestinationPopularityRepository repository;

    @Transactional
    public void execute(DestinationKey destination) {
        DestinationPopularity popularity = repository.findByDestination(destination)
            .orElseGet(() -> DestinationPopularity.initial(destination));
        popularity.increment();
        repository.save(popularity);
    }
}
