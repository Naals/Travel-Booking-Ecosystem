package com.travel.booking.application.usecase;

import com.travel.booking.application.dto.response.BookingResponse;
import com.travel.booking.domain.aggregate.Booking;
import com.travel.booking.domain.repository.BookingRepository;
import com.travel.booking.domain.valueobject.BookingId;
import com.travel.common.exception.BusinessRuleViolationException;
import com.travel.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetBookingUseCase {

    private final BookingRepository repository;

    @Transactional(readOnly = true)
    public BookingResponse execute(String bookingId, String requestingUserId) {
        Booking booking = repository.findById(BookingId.of(bookingId))
            .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

        if (!booking.getUserId().equals(requestingUserId))
            throw new BusinessRuleViolationException(
                "Access denied to this booking", "FORBIDDEN");

        return BookingResponse.from(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> executeForUser(String userId) {
        return repository.findByUserId(userId)
            .stream()
            .map(BookingResponse::from)
            .toList();
    }
}
