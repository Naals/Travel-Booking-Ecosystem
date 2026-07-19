package com.travel.payment.application.usecase;

import com.travel.payment.application.dto.response.PaymentResponse;
import com.travel.payment.domain.repository.PaymentRepository;
import com.travel.payment.domain.valueobject.PaymentId;
import com.travel.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetPaymentUseCase {

    private final PaymentRepository repository;

    @Transactional(readOnly = true)
    public PaymentResponse execute(String paymentId) {
        return repository.findById(PaymentId.of(paymentId))
            .map(PaymentResponse::from)
            .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));
    }

    @Transactional(readOnly = true)
    public PaymentResponse executeByBooking(String bookingId) {
        return repository.findByBookingId(bookingId)
            .map(PaymentResponse::from)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Payment not found for booking: " + bookingId));
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> executeForUser(String userId) {
        return repository.findByUserId(userId)
            .stream()
            .map(PaymentResponse::from)
            .toList();
    }
}
