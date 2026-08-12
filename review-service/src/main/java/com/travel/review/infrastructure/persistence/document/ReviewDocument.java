package com.travel.review.infrastructure.persistence.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "reviews")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReviewDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String bookingId;

    private String userId;

    @Indexed
    private String resourceId;

    private String resourceType;
    private int    rating;
    private String title;
    private String body;

    @Indexed
    private String status;

    private String moderatorId;
    private String moderationReason;

    private Instant createdAt;
    private Instant updatedAt;
}
