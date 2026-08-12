package com.travel.review.domain;

import com.travel.common.exception.BusinessRuleViolationException;
import com.travel.review.domain.aggregate.Review;
import com.travel.review.domain.event.*;
import com.travel.review.domain.model.ReviewStatus;
import com.travel.review.domain.model.ReviewedResourceType;
import com.travel.review.domain.valueobject.Rating;
import com.travel.review.domain.valueobject.ReviewContent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Review aggregate")
class ReviewTest {

    static final String BOOKING_ID  = "booking-1";
    static final String USER_ID     = "user-1";
    static final String RESOURCE_ID = "hotel-1";
    static final Rating RATING      = Rating.of(4);
    static final ReviewContent CONTENT = ReviewContent.of(
        "Great stay", "The room was clean and the staff were friendly throughout.");

    @Nested
    @DisplayName("Creation and auto-moderation")
    class Creation {

        @Test @DisplayName("clean content is auto-approved, raises two events")
        void autoApproved() {
            Review r = Review.write(BOOKING_ID, USER_ID, RESOURCE_ID,
                ReviewedResourceType.HOTEL, RATING, CONTENT, false);

            assertThat(r.getStatus()).isEqualTo(ReviewStatus.APPROVED);
            assertThat(r.getDomainEvents()).hasSize(2);
            assertThat(r.getDomainEvents().get(0)).isInstanceOf(ReviewCreatedEvent.class);
            assertThat(r.getDomainEvents().get(1)).isInstanceOf(ResourceRatingUpdatedEvent.class);
        }

        @Test @DisplayName("flagged content requires manual review, no rating event yet")
        void requiresManualReview() {
            Review r = Review.write(BOOKING_ID, USER_ID, RESOURCE_ID,
                ReviewedResourceType.HOTEL, RATING, CONTENT, true);

            assertThat(r.getStatus()).isEqualTo(ReviewStatus.PENDING_MODERATION);
            assertThat(r.getDomainEvents()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Moderation")
    class Moderation {

        @Test @DisplayName("approve() from PENDING_MODERATION raises moderated + rating events")
        void approvePending() {
            Review r = Review.write(BOOKING_ID, USER_ID, RESOURCE_ID,
                ReviewedResourceType.HOTEL, RATING, CONTENT, true);
            r.clearDomainEvents();

            r.approve("moderator-1");

            assertThat(r.getStatus()).isEqualTo(ReviewStatus.APPROVED);
            assertThat(r.getModeratorId()).isEqualTo("moderator-1");
            assertThat(r.getDomainEvents()).hasSize(2);
            assertThat(r.getDomainEvents().get(0)).isInstanceOf(ReviewModeratedEvent.class);
            assertThat(r.getDomainEvents().get(1)).isInstanceOf(ResourceRatingUpdatedEvent.class);
        }

        @Test @DisplayName("reject() requires a reason")
        void rejectRequiresReason() {
            Review r = Review.write(BOOKING_ID, USER_ID, RESOURCE_ID,
                ReviewedResourceType.HOTEL, RATING, CONTENT, true);

            assertThatThrownBy(() -> r.reject("moderator-1", null))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("reason is required");
        }

        @Test @DisplayName("reject() from PENDING_MODERATION transitions correctly")
        void rejectPending() {
            Review r = Review.write(BOOKING_ID, USER_ID, RESOURCE_ID,
                ReviewedResourceType.HOTEL, RATING, CONTENT, true);

            r.reject("moderator-1", "Contains prohibited content");

            assertThat(r.getStatus()).isEqualTo(ReviewStatus.REJECTED);
            assertThat(r.getModerationReason()).isEqualTo("Contains prohibited content");
        }

        @Test @DisplayName("cannot moderate an already-approved review")
        void cannotModerateApproved() {
            Review r = Review.write(BOOKING_ID, USER_ID, RESOURCE_ID,
                ReviewedResourceType.HOTEL, RATING, CONTENT, false);

            assertThatThrownBy(() -> r.approve("moderator-1"))
                .isInstanceOf(BusinessRuleViolationException.class);
        }
    }

    @Nested
    @DisplayName("Flagging")
    class Flagging {

        @Test @DisplayName("flag() transitions APPROVED to FLAGGED")
        void flagApproved() {
            Review r = Review.write(BOOKING_ID, USER_ID, RESOURCE_ID,
                ReviewedResourceType.HOTEL, RATING, CONTENT, false);
            r.clearDomainEvents();

            r.flag("reporter-1", "Misleading information");

            assertThat(r.getStatus()).isEqualTo(ReviewStatus.FLAGGED);
            assertThat(r.getDomainEvents().get(0)).isInstanceOf(ReviewFlaggedEvent.class);
        }

        @Test @DisplayName("cannot flag a review that isn't approved")
        void cannotFlagPending() {
            Review r = Review.write(BOOKING_ID, USER_ID, RESOURCE_ID,
                ReviewedResourceType.HOTEL, RATING, CONTENT, true);

            assertThatThrownBy(() -> r.flag("reporter-1", "reason"))
                .isInstanceOf(BusinessRuleViolationException.class);
        }

        @Test @DisplayName("cannot flag your own review")
        void cannotFlagOwnReview() {
            Review r = Review.write(BOOKING_ID, USER_ID, RESOURCE_ID,
                ReviewedResourceType.HOTEL, RATING, CONTENT, false);

            assertThatThrownBy(() -> r.flag(USER_ID, "reason"))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("own review");
        }

        @Test @DisplayName("a flagged review can be re-moderated")
        void flaggedCanBeReModerated() {
            Review r = Review.write(BOOKING_ID, USER_ID, RESOURCE_ID,
                ReviewedResourceType.HOTEL, RATING, CONTENT, false);
            r.flag("reporter-1", "reason");

            assertThatCode(() -> r.approve("moderator-1")).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Rating value object")
    class RatingTests {

        @Test @DisplayName("rejects rating below 1")
        void tooLow() {
            assertThatThrownBy(() -> Rating.of(0))
                .isInstanceOf(com.travel.common.exception.DomainException.class);
        }

        @Test @DisplayName("rejects rating above 5")
        void tooHigh() {
            assertThatThrownBy(() -> Rating.of(6))
                .isInstanceOf(com.travel.common.exception.DomainException.class);
        }

        @Test @DisplayName("accepts boundary values")
        void boundaries() {
            assertThatCode(() -> Rating.of(1)).doesNotThrowAnyException();
            assertThatCode(() -> Rating.of(5)).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("ReviewContent value object")
    class ReviewContentTests {

        @Test @DisplayName("rejects blank title")
        void blankTitle() {
            assertThatThrownBy(() -> ReviewContent.of("", "A valid body over ten characters."))
                .isInstanceOf(com.travel.common.exception.DomainException.class);
        }

        @Test @DisplayName("rejects body under 10 characters")
        void shortBody() {
            assertThatThrownBy(() -> ReviewContent.of("Title", "Too short"))
                .isInstanceOf(com.travel.common.exception.DomainException.class);
        }

        @Test @DisplayName("trims whitespace from title and body")
        void trims() {
            ReviewContent c = ReviewContent.of("  Title  ", "  A sufficiently long body text.  ");
            assertThat(c.getTitle()).isEqualTo("Title");
            assertThat(c.getBody()).isEqualTo("A sufficiently long body text.");
        }
    }
}
