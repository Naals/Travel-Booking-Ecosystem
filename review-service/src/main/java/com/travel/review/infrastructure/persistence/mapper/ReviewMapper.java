package com.travel.review.infrastructure.persistence.mapper;

import com.travel.review.domain.aggregate.Review;
import com.travel.review.domain.model.ReviewStatus;
import com.travel.review.domain.model.ReviewedResourceType;
import com.travel.review.domain.valueobject.Rating;
import com.travel.review.domain.valueobject.ReviewContent;
import com.travel.review.domain.valueobject.ReviewId;
import com.travel.review.infrastructure.persistence.document.ReviewDocument;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    public ReviewDocument toDocument(Review r) {
        return ReviewDocument.builder()
            .id(r.getId().getValue())
            .bookingId(r.getBookingId())
            .userId(r.getUserId())
            .resourceId(r.getResourceId())
            .resourceType(r.getResourceType().name())
            .rating(r.getRating().getStars())
            .title(r.getContent().getTitle())
            .body(r.getContent().getBody())
            .status(r.getStatus().name())
            .moderatorId(r.getModeratorId())
            .moderationReason(r.getModerationReason())
            .createdAt(r.getCreatedAt())
            .updatedAt(r.getUpdatedAt())
            .build();
    }

    public Review toDomain(ReviewDocument d) {
        return Review.reconstitute(
            ReviewId.of(d.getId()),
            d.getBookingId(), d.getUserId(), d.getResourceId(),
            ReviewedResourceType.valueOf(d.getResourceType()),
            Rating.of(d.getRating()),
            ReviewContent.of(d.getTitle(), d.getBody()),
            ReviewStatus.valueOf(d.getStatus()),
            d.getModeratorId(), d.getModerationReason(),
            d.getCreatedAt(), d.getUpdatedAt()
        );
    }
}
