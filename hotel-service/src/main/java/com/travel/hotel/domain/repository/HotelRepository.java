package com.travel.hotel.domain.repository;

import com.travel.hotel.domain.aggregate.Hotel;
import com.travel.hotel.domain.valueobject.HotelId;
import com.travel.hotel.domain.valueobject.HotelStatus;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HotelRepository {
    Hotel           save(Hotel hotel);
    Optional<Hotel> findById(HotelId id);
    List<Hotel>     findByManagerId(String managerId);
    List<Hotel>     findByStatus(HotelStatus status);
    List<Hotel>     findByCity(String city);
    boolean         existsById(HotelId id);
}
