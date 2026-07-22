package com.travel.property.domain.valueobject;

import com.travel.shared.domain.ValueObject;
import com.travel.common.exception.DomainException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Immutable date range value object.
 * Used for availability windows and reservation periods.
 * start is inclusive, end is exclusive (standard half-open interval).
 */
public final class DateRange implements ValueObject {

    private final LocalDate start;
    private final LocalDate end;

    private DateRange(LocalDate start, LocalDate end) {
        if (start == null || end == null)
            throw new DomainException("DateRange start and end must not be null", "INVALID_DATE_RANGE");
        if (!start.isBefore(end))
            throw new DomainException("DateRange start must be before end", "INVALID_DATE_RANGE");
        this.start = start;
        this.end   = end;
    }

    public static DateRange of(LocalDate start, LocalDate end) {
        return new DateRange(start, end);
    }

    public LocalDate getStart() { return start; }
    public LocalDate getEnd()   { return end; }

    /** Number of nights between start and end. */
    public long nights() {
        return ChronoUnit.DAYS.between(start, end);
    }

    /**
     * Returns true if this range overlaps with other.
     * Two ranges overlap when one starts before the other ends.
     */
    public boolean overlaps(DateRange other) {
        return start.isBefore(other.end) && end.isAfter(other.start);
    }

    public boolean contains(LocalDate date) {
        return !date.isBefore(start) && date.isBefore(end);
    }

    @Override public boolean equals(Object o) {
        return o instanceof DateRange dr
            && start.equals(dr.start) && end.equals(dr.end);
    }
    @Override public int    hashCode() { return Objects.hash(start, end); }
    @Override public String toString() { return start + " to " + end; }
}
