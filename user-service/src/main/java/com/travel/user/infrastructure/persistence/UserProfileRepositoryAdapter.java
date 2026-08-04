package com.travel.user.infrastructure.persistence;

import com.travel.user.domain.aggregate.UserProfile;
import com.travel.user.domain.model.UserId;
import com.travel.user.domain.repository.UserProfileRepository;
import com.travel.user.infrastructure.persistence.mapper.UserProfileMapper;
import com.travel.user.infrastructure.persistence.repository.UserProfileJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserProfileRepositoryAdapter implements UserProfileRepository {

    private final UserProfileJpaRepository jpa;
    private final UserProfileMapper        mapper;

    @Override public UserProfile           save(UserProfile p)   { return mapper.toDomain(jpa.save(mapper.toEntity(p))); }
    @Override public Optional<UserProfile> findById(UserId id)   { return jpa.findById(id.getValue()).map(mapper::toDomain); }
    @Override public boolean               existsById(UserId id) { return jpa.existsById(id.getValue()); }
}
