package com.travel.identity.infrastructure.persistence;

import com.travel.identity.domain.model.Email;
import com.travel.identity.domain.model.User;
import com.travel.identity.domain.model.UserId;
import com.travel.identity.domain.repository.UserRepository;
import com.travel.identity.infrastructure.persistence.mapper.UserMapper;
import com.travel.identity.infrastructure.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Optional;

/**
 * Adapter: implements the domain UserRepository port using Spring Data JPA.
 * The domain layer depends on UserRepository (port), never on this class.
 */
@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpa;
    private final UserMapper        mapper;

    @Override public User           save(User u)          { return mapper.toDomain(jpa.save(mapper.toEntity(u))); }
    @Override public Optional<User> findById(UserId id)   { return jpa.findById(id.getValue()).map(mapper::toDomain); }
    @Override public Optional<User> findByEmail(Email e)  { return jpa.findByEmail(e.getValue()).map(mapper::toDomain); }
    @Override public boolean        existsByEmail(Email e) { return jpa.existsByEmail(e.getValue()); }
    @Override public void           delete(UserId id)     { jpa.deleteById(id.getValue()); }
}
