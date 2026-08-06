package com.travel.review.domain.valueobject;

import com.travel.shared.domain.ValueObject;
import com.travel.common.exception.DomainException;
import java.util.Objects;

public final class Rating implements ValueObject {

    private static final int MIN = 1;
    private static final int MAX = 5;

    private final int stars;

    private Rating(int stars) {
        if (stars < MIN || stars > MAX)
            throw new DomainException(
                "Rating must be between " + MIN + " and " + MAX + " stars", "INVALID_RATING");
        this.stars = stars;
    }

    public static Rating of(int stars) { return new Rating(stars); }

    public int getStars() { return stars; }

    @Override public boolean equals(Object o) {
        return o instanceof Rating r && stars == r.stars;
    }
    @Override public int    hashCode() { return Objects.hash(stars); }
    @Override public String toString() { return stars + "★"; }
}
