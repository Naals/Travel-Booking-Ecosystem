package com.travel.booking.application.usecase;

import com.travel.booking.application.dto.response.BookingResponse;
import com.travel.booking.domain.aggregate.Booking;
import com.travel.booking.domain.repository.BookingRepository;
import com.travel.booking.domain.valueobject.BookingId;
import com.travel.booking.infrastructure.messaging.producer.BookingEventPublisher;
import com.travel.common.exception.BusinessRuleViolationException;
import com.travel.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CancelBookingUseCase {

    private final BookingRepository     repository;
    private final BookingEventPublisher eventPublisher;

    @Transactional
    public BookingResponse execute(String bookingId, String userId, String reason) {
        Booking booking = repository.findById(BookingId.of(bookingId))
            .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

        if (!booking.getUserId().equals(userId))
            throw new BusinessRuleViolationException(
                "Access denied to this booking", "FORBIDDEN");

        booking.cancel(reason);
        Booking saved = repository.save(booking);

        eventPublisher.publishEvents(saved.getDomainEvents());
        saved.clearDomainEvents();

        log.info("Booking {} cancelled by user {}", bookingId, userId);
        return BookingResponse.from(saved);
    }
}
