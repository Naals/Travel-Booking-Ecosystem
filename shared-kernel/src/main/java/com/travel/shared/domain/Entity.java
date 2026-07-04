package com.travel.shared.domain;

import java.util.Objects;

/**
 * Base class for all DDD Entities across the travel platform.
 *
 * Equality is identity-based — two entities with the same ID are the same
 * entity regardless of attribute state. This is a fundamental DDD invariant:
 * never compare entities by value, only by identity.
 *
 * @param <ID> the type of the entity's identity (e.g. UserId, BookingId)
 */
public abstract class Entity<ID> {

    private final ID id;

    protected Entity(ID id) {
        this.id = Objects.requireNonNull(id, "Entity ID must not be null");
    }

    public ID getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity<?> entity = (Entity<?>) o;
        return Objects.equals(id, entity.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[id=" + id + "]";
    }
}
