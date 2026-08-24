package com.travel.recommendation.domain.repository;

import com.travel.recommendation.domain.model.UserAffinity;
import com.travel.recommendation.domain.valueobject.DestinationKey;

import java.util.List;
import java.util.Optional;

public interface UserAffinityRepository {
    UserAffinity           save(UserAffinity affinity);
    Optional<UserAffinity> findByUserIdAndDestination(String userId, DestinationKey destination);
    List<UserAffinity>     findByUserId(String userId);
}
