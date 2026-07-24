package com.travel.hotel.application.usecase;

import com.travel.hotel.application.dto.request.AddRoomRequest;
import com.travel.hotel.application.dto.response.HotelResponse;
import com.travel.hotel.domain.aggregate.Hotel;
import com.travel.hotel.domain.repository.HotelRepository;
import com.travel.hotel.domain.valueobject.*;
import com.travel.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddRoomUseCase {

    private final HotelRepository repository;

    @Transactional
    public HotelResponse execute(String hotelId, String managerId,
                                 AddRoomRequest request) {
        Hotel hotel = repository.findById(HotelId.of(hotelId))
            .orElseThrow(() -> new ResourceNotFoundException("Hotel", hotelId));

        if (!hotel.getManagerId().equals(managerId))
            throw new com.travel.common.exception.BusinessRuleViolationException(
                "Access denied to this hotel", "FORBIDDEN");

        hotel.addRoom(
            request.roomNumber(),
            RoomType.valueOf(request.roomType()),
            Money.of(request.ratePerNight(), request.currency()),
            request.maxOccupancy()
        );

        Hotel saved = repository.save(hotel);
        log.info("Room {} added to hotel {}", request.roomNumber(), hotelId);
        return HotelResponse.from(saved);
    }
}
