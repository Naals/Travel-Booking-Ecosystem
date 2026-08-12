package com.travel.review.infrastructure.persistence.repository;

import com.travel.review.infrastructure.persistence.document.RatingSummaryDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RatingSummaryMongoRepository extends MongoRepository<RatingSummaryDocument, String> {
}
