package com.travel.user.infrastructure.persistence;

import com.travel.user.domain.model.TravelHistoryEntry;
import com.travel.user.domain.model.UserId;
import com.travel.user.domain.repository.TravelHistoryRepository;
import com.travel.user.infrastructure.persistence.mapper.TravelHistoryMapper;
import com.travel.user.infrastructure.persistence.repository.TravelHistoryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TravelHistoryRepositoryAdapter implements TravelHistoryRepository {

    private final TravelHistoryJpaRepository jpa;
    private final TravelHistoryMapper        mapper;

    @Override
    public void save(TravelHistoryEntry entry) {
        jpa.save(mapper.toEntity(entry));
    }

    @Override
    public boolean existsByUserIdAndBookingId(UserId userId, String bookingId) {
        return jpa.existsByUserIdAndBookingId(userId.getValue(), bookingId);
    }

    @Override
    public List<TravelHistoryEntry> findByUserId(UserId userId, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "completedAt"));
        return jpa.findByUserId(userId.getValue(), pageable)
            .stream().map(mapper::toDomain).toList();
    }

    @Override
    public long countByUserId(UserId userId) {
        return jpa.countByUserId(userId.getValue());
    }
}
