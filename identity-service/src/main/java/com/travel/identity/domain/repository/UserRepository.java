package com.travel.identity.domain.repository;

import com.travel.identity.domain.model.Email;
import com.travel.identity.domain.model.User;
import com.travel.identity.domain.model.UserId;

import java.util.Optional;

/**
 * User repository port (domain interface).
 *
 * Defined in the domain layer — implemented in infrastructure.
 * Domain code depends on this interface, never on Spring Data or JPA directly.
 * This is the Dependency Inversion Principle applied to persistence.
 */
public interface UserRepository {
    User             save(User user);
    Optional<User>   findById(UserId id);
    Optional<User>   findByEmail(Email email);
    boolean          existsByEmail(Email email);
    void             delete(UserId id);
}
