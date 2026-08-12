package com.travel.review.infrastructure.persistence.repository;

import com.travel.review.infrastructure.persistence.document.ReviewDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewMongoRepository extends MongoRepository<ReviewDocument, String> {
    List<ReviewDocument> findByResourceIdAndStatus(String resourceId, String status);
    Page<ReviewDocument> findByResourceIdAndStatus(String resourceId, String status, Pageable pageable);
    long                  countByResourceIdAndStatus(String resourceId, String status);
    Page<ReviewDocument>  findByStatus(String status, Pageable pageable);
    long                  countByStatus(String status);
}
