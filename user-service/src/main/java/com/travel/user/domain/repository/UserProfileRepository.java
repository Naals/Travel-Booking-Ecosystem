package com.travel.user.domain.repository;

import com.travel.user.domain.aggregate.UserProfile;
import com.travel.user.domain.model.UserId;

import java.util.Optional;

public interface UserProfileRepository {
    UserProfile           save(UserProfile profile);
    Optional<UserProfile> findById(UserId id);
    boolean                existsById(UserId id);
}
