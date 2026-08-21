package com.travel.loyalty.application.usecase;

import com.travel.common.exception.ResourceNotFoundException;
import com.travel.common.response.PagedResponse;
import com.travel.loyalty.application.dto.response.LoyaltyTransactionResponse;
import com.travel.loyalty.domain.aggregate.LoyaltyAccount;
import com.travel.loyalty.domain.repository.LoyaltyAccountRepository;
import com.travel.loyalty.domain.valueobject.LoyaltyAccountId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** In-memory pagination over the aggregate's own list — same shape as
 * GetTransactionHistoryUseCase in wallet-service (Day 18), for the
 * same reason: a single user's lifetime transaction count is bounded. */
@Service
@RequiredArgsConstructor
public class GetTransactionHistoryUseCase {

    private final LoyaltyAccountRepository repository;

    @Transactional(readOnly = true)
    public PagedResponse<LoyaltyTransactionResponse> execute(String userId, int page, int size) {
        LoyaltyAccount account = repository.findById(LoyaltyAccountId.of(userId))
            .orElseThrow(() -> new ResourceNotFoundException("LoyaltyAccount", userId));

        List<LoyaltyTransactionResponse> all = account.getTransactions().stream()
            .sorted((a, b) -> b.getOccurredAt().compareTo(a.getOccurredAt()))
            .map(LoyaltyTransactionResponse::from)
            .toList();

        int fromIndex = Math.min(page * size, all.size());
        int toIndex   = Math.min(fromIndex + size, all.size());

        return PagedResponse.of(all.subList(fromIndex, toIndex), page, size, all.size());
    }
}
