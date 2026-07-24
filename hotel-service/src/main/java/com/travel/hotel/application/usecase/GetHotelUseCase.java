package com.travel.hotel.application.usecase;

import com.travel.hotel.application.dto.response.HotelResponse;
import com.travel.hotel.domain.repository.HotelRepository;
import com.travel.hotel.domain.valueobject.HotelId;
import com.travel.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetHotelUseCase {

    private final HotelRepository repository;

    @Transactional(readOnly = true)
    public HotelResponse execute(String hotelId) {
        return repository.findById(HotelId.of(hotelId))
            .map(HotelResponse::from)
            .orElseThrow(() -> new ResourceNotFoundException("Hotel", hotelId));
    }

    @Transactional(readOnly = true)
    public List<HotelResponse> executeForManager(String managerId) {
        return repository.findByManagerId(managerId).stream()
            .map(HotelResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<HotelResponse> executeByCity(String city) {
        return repository.findByCity(city).stream()
            .map(HotelResponse::from).toList();
    }
}
