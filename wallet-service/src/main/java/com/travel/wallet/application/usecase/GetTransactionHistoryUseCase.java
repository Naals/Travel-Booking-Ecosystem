package com.travel.wallet.application.usecase;

import com.travel.common.exception.ResourceNotFoundException;
import com.travel.common.response.PagedResponse;
import com.travel.wallet.application.dto.response.WalletTransactionResponse;
import com.travel.wallet.domain.aggregate.Wallet;
import com.travel.wallet.domain.repository.WalletRepository;
import com.travel.wallet.domain.valueobject.WalletId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Paginates in memory over the wallet's own transaction list rather
 * than a dedicated paged repository query — acceptable because a
 * single user's lifetime transaction count is bounded and small,
 * unlike review-service's cross-user moderation queue (Day 16), which
 * pages at the repository/query level instead.
 */
@Service
@RequiredArgsConstructor
public class GetTransactionHistoryUseCase {

    private final WalletRepository repository;

    @Transactional(readOnly = true)
    public PagedResponse<WalletTransactionResponse> execute(String userId, int page, int size) {
        Wallet wallet = repository.findById(WalletId.of(userId))
            .orElseThrow(() -> new ResourceNotFoundException("Wallet", userId));

        List<WalletTransactionResponse> all = wallet.getTransactions().stream()
            .sorted((a, b) -> b.getOccurredAt().compareTo(a.getOccurredAt()))
            .map(WalletTransactionResponse::from)
            .toList();

        int fromIndex = Math.min(page * size, all.size());
        int toIndex   = Math.min(fromIndex + size, all.size());

        return PagedResponse.of(all.subList(fromIndex, toIndex), page, size, all.size());
    }
}
