package com.travel.review.domain.service;

import com.travel.review.domain.valueobject.ReviewContent;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;

/**
 * Deterministic, keyword-based first-pass check applied at review
 * creation. Deliberately simple — a denylist, not a classifier.
 * Full anomaly/fraud detection belongs to fraud-service (Tier 4, not
 * yet built); this exists only to keep obviously abusive text out of
 * the APPROVED state without a human in the loop, while routing
 * genuinely borderline content to SUPPORT_AGENT/ADMIN review.
 */
@Component
public class ContentModerationPolicy {

    private static final Set<String> DENYLIST = Set.of(
        // Intentionally short and illustrative, not exhaustive — a
        // real denylist would be externally configured and
        // regularly updated, not hardcoded in domain code.
        "scam", "fraudulent", "fake reviews"
    );

    public boolean requiresManualReview(ReviewContent content) {
        String haystack = (content.getTitle() + " " + content.getBody())
            .toLowerCase(Locale.ROOT);
        return DENYLIST.stream().anyMatch(haystack::contains);
    }
}
