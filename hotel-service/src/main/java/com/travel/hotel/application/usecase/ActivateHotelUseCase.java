package com.travel.hotel.application.usecase;

import com.travel.hotel.application.dto.response.HotelResponse;
import com.travel.hotel.domain.aggregate.Hotel;
import com.travel.hotel.domain.repository.HotelRepository;
import com.travel.hotel.domain.valueobject.HotelId;
import com.travel.hotel.infrastructure.messaging.producer.HotelEventPublisher;
import com.travel.common.exception.BusinessRuleViolationException;
import com.travel.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivateHotelUseCase {

    private final HotelRepository    repository;
    private final HotelEventPublisher eventPublisher;

    @Transactional
    public HotelResponse execute(String hotelId, String managerId) {
        Hotel hotel = repository.findById(HotelId.of(hotelId))
            .orElseThrow(() -> new ResourceNotFoundException("Hotel", hotelId));

        if (!hotel.getManagerId().equals(managerId))
            throw new BusinessRuleViolationException(
                "Access denied to this hotel", "FORBIDDEN");

        hotel.activate();
        Hotel saved = repository.save(hotel);

        // HotelCreatedEvent → search-service indexes this hotel
        eventPublisher.publishEvents(saved.getDomainEvents());
        saved.clearDomainEvents();

        log.info("Hotel activated: {}", hotelId);
        return HotelResponse.from(saved);
    }
}
