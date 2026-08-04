package com.travel.user.application.usecase;

import com.travel.common.response.PagedResponse;
import com.travel.user.application.dto.response.TravelHistoryEntryResponse;
import com.travel.user.domain.model.UserId;
import com.travel.user.domain.repository.TravelHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * First real use of common-lib's PagedResponse (Day 3) — every other
 * list endpoint in the platform so far has returned a full unpaged
 * List; travel history is the first collection expected to grow
 * large enough per-user to warrant it.
 */
@Service
@RequiredArgsConstructor
public class GetTravelHistoryUseCase {

    private final TravelHistoryRepository repository;

    @Transactional(readOnly = true)
    public PagedResponse<TravelHistoryEntryResponse> execute(String userId, int page, int size) {
        UserId id = UserId.of(userId);

        var entries = repository.findByUserId(id, page, size).stream()
            .map(TravelHistoryEntryResponse::from)
            .toList();

        long total = repository.countByUserId(id);

        return PagedResponse.of(entries, page, size, total);
    }
}
