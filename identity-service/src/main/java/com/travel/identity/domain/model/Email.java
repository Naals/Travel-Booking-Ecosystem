package com.travel.identity.domain.model;

import com.travel.shared.domain.ValueObject;
import com.travel.common.exception.DomainException;
import java.util.Objects;
import java.util.regex.Pattern;

public final class Email implements ValueObject {

    private static final Pattern PATTERN =
        Pattern.compile("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$");

    private final String value;

    private Email(String value) {
        if (value == null || value.isBlank())
            throw new DomainException("Email must not be empty", "INVALID_EMAIL");
        String normalized = value.trim().toLowerCase();
        if (!PATTERN.matcher(normalized).matches())
            throw new DomainException("Invalid email format: " + value, "INVALID_EMAIL");
        this.value = normalized;
    }

    public static Email of(String value) { return new Email(value); }

    public String getValue() { return value; }

    @Override
    public boolean equals(Object o) {
        return o instanceof Email e && Objects.equals(value, e.value);
    }

    @Override public int hashCode() { return Objects.hash(value); }
    @Override public String toString() { return value; }
}
