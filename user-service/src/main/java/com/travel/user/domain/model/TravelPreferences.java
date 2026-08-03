package com.travel.user.domain.model;

import com.travel.shared.domain.ValueObject;
import com.travel.common.exception.DomainException;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Travel-related settings applied when the user books through any
 * inventory service — preferred currency for price display, preferred
 * language, flight seat preference, and dietary restrictions relevant
 * to hotel/flight catering.
 *
 * This is a settings record, not an enforcement mechanism — it does
 * not reach into other services to auto-apply preferences (e.g.
 * auto-selecting a window seat). Client applications read and act on it.
 */
public final class TravelPreferences implements ValueObject {

    private static final Pattern CURRENCY_PATTERN = Pattern.compile("^[A-Z]{3}$");
    private static final Pattern LANGUAGE_PATTERN = Pattern.compile("^[a-z]{2}$");

    private final String                  preferredCurrency;
    private final String                  preferredLanguage;
    private final SeatPreference          seatPreference;
    private final Set<DietaryRestriction> dietaryRestrictions;

    private TravelPreferences(String preferredCurrency, String preferredLanguage,
                              SeatPreference seatPreference,
                              Set<DietaryRestriction> dietaryRestrictions) {
        if (preferredCurrency == null || !CURRENCY_PATTERN.matcher(preferredCurrency).matches())
            throw new DomainException(
                "Preferred currency must be a 3-letter uppercase ISO 4217 code",
                "INVALID_PREFERENCES");
        if (preferredLanguage == null || !LANGUAGE_PATTERN.matcher(preferredLanguage).matches())
            throw new DomainException(
                "Preferred language must be a 2-letter lowercase ISO 639-1 code",
                "INVALID_PREFERENCES");
        this.preferredCurrency   = preferredCurrency;
        this.preferredLanguage   = preferredLanguage;
        this.seatPreference      = Objects.requireNonNull(seatPreference);
        this.dietaryRestrictions = dietaryRestrictions != null
            ? EnumSet.copyOf(dietaryRestrictions) : EnumSet.noneOf(DietaryRestriction.class);
    }

    public static TravelPreferences of(String preferredCurrency, String preferredLanguage,
                                       SeatPreference seatPreference,
                                       Set<DietaryRestriction> dietaryRestrictions) {
        return new TravelPreferences(preferredCurrency, preferredLanguage,
            seatPreference, dietaryRestrictions);
    }

    /** Sensible defaults applied when a profile is first created. */
    public static TravelPreferences defaults() {
        return new TravelPreferences("USD", "en", SeatPreference.NO_PREFERENCE,
            EnumSet.noneOf(DietaryRestriction.class));
    }

    public String                  getPreferredCurrency()   { return preferredCurrency; }
    public String                  getPreferredLanguage()   { return preferredLanguage; }
    public SeatPreference          getSeatPreference()      { return seatPreference; }
    public Set<DietaryRestriction> getDietaryRestrictions() { return Collections.unmodifiableSet(dietaryRestrictions); }

    @Override public boolean equals(Object o) {
        return o instanceof TravelPreferences t
            && preferredCurrency.equals(t.preferredCurrency)
            && preferredLanguage.equals(t.preferredLanguage)
            && seatPreference == t.seatPreference
            && dietaryRestrictions.equals(t.dietaryRestrictions);
    }
    @Override public int hashCode() {
        return Objects.hash(preferredCurrency, preferredLanguage, seatPreference, dietaryRestrictions);
    }
}
