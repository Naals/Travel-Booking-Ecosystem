package com.travel.review.domain;

import com.travel.review.domain.service.ContentModerationPolicy;
import com.travel.review.domain.valueobject.ReviewContent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ContentModerationPolicy")
class ContentModerationPolicyTest {

    ContentModerationPolicy policy = new ContentModerationPolicy();

    @Test
    @DisplayName("clean content does not require manual review")
    void cleanContent() {
        ReviewContent content = ReviewContent.of(
            "Wonderful stay", "We had a great time, the staff were very welcoming.");
        assertThat(policy.requiresManualReview(content)).isFalse();
    }

    @Test
    @DisplayName("denylisted keyword triggers manual review")
    void flaggedContent() {
        ReviewContent content = ReviewContent.of(
            "Beware", "This listing looked like a scam to me, avoid booking here.");
        assertThat(policy.requiresManualReview(content)).isTrue();
    }

    @Test
    @DisplayName("keyword match is case-insensitive")
    void caseInsensitive() {
        ReviewContent content = ReviewContent.of(
            "Warning", "I think this was a SCAM operation targeting tourists.");
        assertThat(policy.requiresManualReview(content)).isTrue();
    }
}
