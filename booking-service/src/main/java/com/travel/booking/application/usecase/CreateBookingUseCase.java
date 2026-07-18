package com.travel.booking.application.usecase;

import com.travel.booking.application.dto.request.CreateBookingRequest;
import com.travel.booking.application.dto.response.BookingResponse;
import com.travel.booking.domain.aggregate.Booking;
import com.travel.booking.domain.repository.BookingRepository;
import com.travel.booking.domain.valueobject.BookingType;
import com.travel.booking.domain.valueobject.Money;
import com.travel.booking.infrastructure.messaging.producer.BookingEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates a booking and fires the saga start event (BookingCreated).
 * Inventory services consume that event and place a resource hold.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreateBookingUseCase {

    private final BookingRepository    repository;
    private final BookingEventPublisher eventPublisher;

    @Transactional
    public BookingResponse execute(String userId, CreateBookingRequest request) {
        log.info("Creating booking for user={} resource={}", userId, request.resourceId());

        Booking booking = Booking.create(
            userId,
            BookingType.valueOf(request.bookingType()),
            request.resourceId(),
            request.resourceName(),
            request.checkInDate(),
            request.checkOutDate(),
            request.guestCount(),
            Money.of(request.totalAmount(), request.currency())
        );

        Booking saved = repository.save(booking);

        // BookingCreatedEvent → inventory services via Kafka
        eventPublisher.publishEvents(saved.getDomainEvents());
        saved.clearDomainEvents();

        log.info("Booking created: {} saga initiated", saved.getId().getValue());
        return BookingResponse.from(saved);
    }
}
