package com.travel.identity.domain.model;

import com.travel.shared.domain.ValueObject;
import com.travel.common.exception.DomainException;
import java.util.Objects;

public final class FullName implements ValueObject {

    private final String firstName;
    private final String lastName;

    private FullName(String firstName, String lastName) {
        if (firstName == null || firstName.isBlank())
            throw new DomainException("First name must not be empty", "INVALID_NAME");
        if (lastName == null || lastName.isBlank())
            throw new DomainException("Last name must not be empty", "INVALID_NAME");
        this.firstName = firstName.trim();
        this.lastName  = lastName.trim();
    }

    public static FullName of(String firstName, String lastName) {
        return new FullName(firstName, lastName);
    }

    public String getFirstName() { return firstName; }
    public String getLastName()  { return lastName; }
    public String getFullName()  { return firstName + " " + lastName; }

    @Override
    public boolean equals(Object o) {
        return o instanceof FullName fn
            && Objects.equals(firstName, fn.firstName)
            && Objects.equals(lastName, fn.lastName);
    }

    @Override public int hashCode() { return Objects.hash(firstName, lastName); }
}
