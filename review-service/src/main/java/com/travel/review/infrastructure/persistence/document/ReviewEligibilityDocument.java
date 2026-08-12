package com.travel.review.infrastructure.persistence.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * _id = bookingId — the natural key. One eligibility record per
 * completed booking, never per (booking, user) since a booking has
 * exactly one owning user.
 */
@Document(collection = "review_eligibility")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReviewEligibilityDocument {

    @Id
    private String id;

    private String  bookingId;
    private String  userId;
    private String  resourceId;
    private String  resourceType;
    private boolean reviewed;
    private Instant completedAt;
}
