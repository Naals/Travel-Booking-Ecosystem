package com.travel.review.domain.valueobject;

import com.travel.shared.domain.ValueObject;
import com.travel.common.exception.DomainException;
import java.util.Objects;

/**
 * Title and body bundled into one value object, following the same
 * pattern as identity-service's FullName (Day 6) — the two fields
 * are always set and validated together.
 */
public final class ReviewContent implements ValueObject {

    private static final int MIN_BODY_LENGTH  = 10;
    private static final int MAX_TITLE_LENGTH = 100;
    private static final int MAX_BODY_LENGTH  = 3000;

    private final String title;
    private final String body;

    private ReviewContent(String title, String body) {
        if (title == null || title.isBlank())
            throw new DomainException("Review title must not be empty", "INVALID_REVIEW_CONTENT");
        if (title.trim().length() > MAX_TITLE_LENGTH)
            throw new DomainException(
                "Review title must not exceed " + MAX_TITLE_LENGTH + " characters",
                "INVALID_REVIEW_CONTENT");
        if (body == null || body.trim().length() < MIN_BODY_LENGTH)
            throw new DomainException(
                "Review body must be at least " + MIN_BODY_LENGTH + " characters",
                "INVALID_REVIEW_CONTENT");
        if (body.trim().length() > MAX_BODY_LENGTH)
            throw new DomainException(
                "Review body must not exceed " + MAX_BODY_LENGTH + " characters",
                "INVALID_REVIEW_CONTENT");
        this.title = title.trim();
        this.body  = body.trim();
    }

    public static ReviewContent of(String title, String body) {
        return new ReviewContent(title, body);
    }

    public String getTitle() { return title; }
    public String getBody()  { return body; }

    @Override public boolean equals(Object o) {
        return o instanceof ReviewContent c
            && title.equals(c.title) && body.equals(c.body);
    }
    @Override public int hashCode() { return Objects.hash(title, body); }
}
