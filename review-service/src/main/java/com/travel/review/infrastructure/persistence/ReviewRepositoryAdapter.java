package com.travel.review.infrastructure.persistence;

import com.travel.review.domain.aggregate.Review;
import com.travel.review.domain.model.ReviewStatus;
import com.travel.review.domain.repository.ReviewRepository;
import com.travel.review.domain.valueobject.ReviewId;
import com.travel.review.infrastructure.persistence.mapper.ReviewMapper;
import com.travel.review.infrastructure.persistence.repository.ReviewMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ReviewRepositoryAdapter implements ReviewRepository {

    private final ReviewMongoRepository mongo;
    private final ReviewMapper          mapper;

    @Override
    public Review save(Review review) {
        return mapper.toDomain(mongo.save(mapper.toDocument(review)));
    }

    @Override
    public Optional<Review> findById(ReviewId id) {
        return mongo.findById(id.getValue()).map(mapper::toDomain);
    }

    @Override
    public List<Review> findByResourceIdAndStatus(String resourceId, ReviewStatus status) {
        return mongo.findByResourceIdAndStatus(resourceId, status.name())
            .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Review> findApprovedByResourceId(String resourceId, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return mongo.findByResourceIdAndStatus(resourceId, ReviewStatus.APPROVED.name(), pageable)
            .stream().map(mapper::toDomain).toList();
    }

    @Override
    public long countApprovedByResourceId(String resourceId) {
        return mongo.countByResourceIdAndStatus(resourceId, ReviewStatus.APPROVED.name());
    }

    @Override
    public List<Review> findPendingModeration(int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        return mongo.findByStatus(ReviewStatus.PENDING_MODERATION.name(), pageable)
            .stream().map(mapper::toDomain).toList();
    }

    @Override
    public long countPendingModeration() {
        return mongo.countByStatus(ReviewStatus.PENDING_MODERATION.name());
    }
}
