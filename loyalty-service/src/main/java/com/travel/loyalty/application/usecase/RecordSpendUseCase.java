package com.travel.loyalty.application.usecase;

import com.travel.loyalty.domain.model.SpendRecord;
import com.travel.loyalty.domain.repository.SpendRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/** Called by BookingEventConsumer.onBookingConfirmed(). */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecordSpendUseCase {

    private final SpendRecordRepository repository;

    @Transactional
    public void execute(String bookingId, String userId, BigDecimal amount, String currency) {
        if (repository.existsByBookingId(bookingId)) {
            log.debug("Spend record already exists for booking {} — skipping (idempotent)", bookingId);
            return;
        }
        repository.save(SpendRecord.of(bookingId, userId, amount, currency));
        log.info("Spend recorded: booking={} amount={} {}", bookingId, amount, currency);
    }
}
