package com.travel.loyalty.application.usecase;

import com.travel.loyalty.domain.aggregate.LoyaltyAccount;
import com.travel.loyalty.domain.model.LoyaltyTransactionType;
import com.travel.loyalty.domain.model.SpendRecord;
import com.travel.loyalty.domain.repository.LoyaltyAccountRepository;
import com.travel.loyalty.domain.repository.SpendRecordRepository;
import com.travel.loyalty.domain.service.PointsCalculationPolicy;
import com.travel.loyalty.domain.valueobject.LoyaltyAccountId;
import com.travel.loyalty.domain.valueobject.Points;
import com.travel.loyalty.infrastructure.messaging.producer.LoyaltyEventPublisher;
import com.travel.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Called by BookingEventConsumer.onBookingCompleted(). */
@Slf4j
@Service
@RequiredArgsConstructor
public class AwardPointsForCompletedBookingUseCase {

    private final SpendRecordRepository     spendRecordRepository;
    private final LoyaltyAccountRepository  accountRepository;
    private final PointsCalculationPolicy   pointsPolicy;
    private final LoyaltyEventPublisher     eventPublisher;

    @Transactional
    public void execute(String bookingId) {
        // Atomic consume — see SpendRecordRepository.tryConsume()'s Javadoc.
        var recordOpt = spendRecordRepository.tryConsume(bookingId);
        if (recordOpt.isEmpty()) {
            log.debug("No unconsumed spend record for booking {} — already awarded, " +
                "cancelled before confirmation, or a non-earning booking type", bookingId);
            return;
        }
        SpendRecord record = recordOpt.get();

        LoyaltyAccount account = accountRepository.findById(LoyaltyAccountId.of(record.getUserId()))
            .orElseThrow(() -> new ResourceNotFoundException("LoyaltyAccount", record.getUserId()));

        Points points = pointsPolicy.calculatePoints(record.getAmount());
        account.earnPoints(points, LoyaltyTransactionType.EARNED, bookingId,
            "Earned from completed booking " + bookingId);

        LoyaltyAccount saved = accountRepository.save(account);

        eventPublisher.publishEvents(saved.getDomainEvents());
        saved.clearDomainEvents();

        log.info("Points awarded: user={} booking={} points={} newBalance={} tier={}",
            record.getUserId(), bookingId, points.getValue(),
            saved.getBalance().getValue(), saved.getTier());
    }
}
