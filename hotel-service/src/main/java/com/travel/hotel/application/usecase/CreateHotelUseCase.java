package com.travel.hotel.application.usecase;

import com.travel.hotel.application.dto.request.CreateHotelRequest;
import com.travel.hotel.application.dto.response.HotelResponse;
import com.travel.hotel.domain.aggregate.Hotel;
import com.travel.hotel.domain.repository.HotelRepository;
import com.travel.hotel.domain.valueobject.Address;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateHotelUseCase {

    private final HotelRepository repository;

    @Transactional
    public HotelResponse execute(String managerId, CreateHotelRequest request) {
        log.info("Creating hotel for manager={} name={}", managerId, request.name());

        Hotel hotel = Hotel.create(
            managerId,
            request.name(),
            request.description(),
            Address.of(request.street(), request.city(),
                request.country(), request.latitude(),
                request.longitude()),
            request.starRating()
        );

        Hotel saved = repository.save(hotel);
        log.info("Hotel created: {}", saved.getId().getValue());
        return HotelResponse.from(saved);
    }
}
