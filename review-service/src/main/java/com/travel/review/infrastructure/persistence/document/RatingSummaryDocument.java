package com.travel.review.infrastructure.persistence.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "rating_summaries")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RatingSummaryDocument {

    @Id
    private String resourceId; // natural key — one summary per resource

    private String  resourceType;
    private double  averageRating;
    private long    reviewCount;
    private Instant updatedAt;
}
