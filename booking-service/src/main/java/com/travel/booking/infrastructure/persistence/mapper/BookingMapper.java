package com.travel.booking.infrastructure.persistence.mapper;

import com.travel.booking.domain.aggregate.Booking;
import com.travel.booking.domain.valueobject.*;
import com.travel.booking.infrastructure.persistence.entity.BookingJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class BookingMapper {

    public BookingJpaEntity toEntity(Booking b) {
        return BookingJpaEntity.builder()
            .id(b.getId().getValue())
            .userId(b.getUserId())
            .bookingType(b.getBookingType())
            .resourceId(b.getResourceId())
            .resourceName(b.getResourceName())
            .status(b.getStatus())
            .checkInDate(b.getCheckInDate())
            .checkOutDate(b.getCheckOutDate())
            .guestCount(b.getGuestCount())
            .totalAmount(b.getTotalAmount().getAmount())
            .currency(b.getTotalAmount().getCurrency())
            .paymentId(b.getPaymentId())
            .cancellationReason(b.getCancellationReason())
            .createdAt(b.getCreatedAt())
            .updatedAt(b.getUpdatedAt())
            .build();
    }

    public Booking toDomain(BookingJpaEntity e) {
        return Booking.reconstitute(
            BookingId.of(e.getId()),
            e.getUserId(),
            e.getBookingType(),
            e.getResourceId(),
            e.getResourceName(),
            e.getStatus(),
            e.getCheckInDate(),
            e.getCheckOutDate(),
            e.getGuestCount(),
            Money.of(e.getTotalAmount(), e.getCurrency()),
            e.getPaymentId(),
            e.getCancellationReason(),
            e.getCreatedAt(),
            e.getUpdatedAt()
        );
    }
}
