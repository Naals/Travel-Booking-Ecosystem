package com.travel.recommendation.domain.repository;

import com.travel.recommendation.domain.model.DestinationPopularity;
import com.travel.recommendation.domain.valueobject.DestinationKey;

import java.util.List;
import java.util.Optional;

public interface DestinationPopularityRepository {
    DestinationPopularity           save(DestinationPopularity popularity);
    Optional<DestinationPopularity> findByDestination(DestinationKey destination);
    List<DestinationPopularity>     findTopByCompletedTripCount(int limit);
}
