package com.travel.hotel.infrastructure.persistence;

import com.travel.hotel.domain.aggregate.Hotel;
import com.travel.hotel.domain.repository.HotelRepository;
import com.travel.hotel.domain.valueobject.HotelId;
import com.travel.hotel.domain.valueobject.HotelStatus;
import com.travel.hotel.infrastructure.persistence.mapper.HotelMapper;
import com.travel.hotel.infrastructure.persistence.repository.HotelJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class HotelRepositoryAdapter implements HotelRepository {

    private final HotelJpaRepository jpa;
    private final HotelMapper        mapper;

    @Override public Hotel           save(Hotel h)              { return mapper.toDomain(jpa.save(mapper.toEntity(h))); }
    @Override public Optional<Hotel> findById(HotelId id)       { return jpa.findById(id.getValue()).map(mapper::toDomain); }
    @Override public List<Hotel>     findByManagerId(String mid){ return jpa.findByManagerId(mid).stream().map(mapper::toDomain).toList(); }
    @Override public List<Hotel>     findByStatus(HotelStatus s){ return jpa.findByStatus(s).stream().map(mapper::toDomain).toList(); }
    @Override public List<Hotel>     findByCity(String city)    { return jpa.findByCityIgnoreCase(city).stream().map(mapper::toDomain).toList(); }
    @Override public boolean         existsById(HotelId id)     { return jpa.existsById(id.getValue()); }
}
