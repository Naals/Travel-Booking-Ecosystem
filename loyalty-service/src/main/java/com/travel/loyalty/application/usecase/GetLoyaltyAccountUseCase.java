package com.travel.loyalty.application.usecase;

import com.travel.loyalty.application.dto.response.LoyaltyAccountResponse;
import com.travel.loyalty.domain.repository.LoyaltyAccountRepository;
import com.travel.loyalty.domain.valueobject.LoyaltyAccountId;
import com.travel.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetLoyaltyAccountUseCase {

    private final LoyaltyAccountRepository repository;

    @Transactional(readOnly = true)
    public LoyaltyAccountResponse execute(String userId) {
        return repository.findById(LoyaltyAccountId.of(userId))
            .map(LoyaltyAccountResponse::from)
            .orElseThrow(() -> new ResourceNotFoundException("LoyaltyAccount", userId));
    }
}
