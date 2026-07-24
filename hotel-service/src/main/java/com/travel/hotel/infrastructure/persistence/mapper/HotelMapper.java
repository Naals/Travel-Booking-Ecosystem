package com.travel.hotel.infrastructure.persistence.mapper;

import com.travel.hotel.domain.aggregate.Hotel;
import com.travel.hotel.domain.model.Room;
import com.travel.hotel.domain.model.RoomReservation;
import com.travel.hotel.domain.valueobject.*;
import com.travel.hotel.infrastructure.persistence.entity.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HotelMapper {

    public HotelJpaEntity toEntity(Hotel hotel) {
        HotelJpaEntity entity = HotelJpaEntity.builder()
            .id(hotel.getId().getValue())
            .managerId(hotel.getManagerId())
            .name(hotel.getName())
            .description(hotel.getDescription())
            .street(hotel.getAddress().getStreet())
            .city(hotel.getAddress().getCity())
            .country(hotel.getAddress().getCountry())
            .latitude(hotel.getAddress().getLatitude())
            .longitude(hotel.getAddress().getLongitude())
            .starRating(hotel.getStarRating())
            .status(hotel.getStatus())
            .createdAt(hotel.getCreatedAt())
            .updatedAt(hotel.getUpdatedAt())
            .build();

        hotel.getRooms().forEach(room -> {
            RoomJpaEntity roomEntity = toRoomEntity(room, entity);
            entity.getRooms().add(roomEntity);
        });

        return entity;
    }

    private RoomJpaEntity toRoomEntity(Room room, HotelJpaEntity hotel) {
        RoomJpaEntity entity = RoomJpaEntity.builder()
            .id(room.getId().getValue())
            .hotel(hotel)
            .roomNumber(room.getRoomNumber())
            .roomType(room.getRoomType())
            .status(room.getStatus())
            .ratePerNight(room.getRatePerNight().getAmount())
            .currency(room.getRatePerNight().getCurrency())
            .maxOccupancy(room.getMaxOccupancy())
            .build();

        room.getReservations().forEach(res -> {
            RoomReservationJpaEntity resEntity = RoomReservationJpaEntity.builder()
                .room(entity)
                .bookingId(res.getBookingId())
                .userId(res.getUserId())
                .checkInDate(res.getDateRange().getStart())
                .checkOutDate(res.getDateRange().getEnd())
                .confirmed(res.isConfirmed())
                .build();
            entity.getReservations().add(resEntity);
        });

        return entity;
    }

    public Hotel toDomain(HotelJpaEntity e) {
        List<Room> rooms = e.getRooms().stream()
            .map(this::toRoomDomain)
            .toList();

        return Hotel.reconstitute(
            HotelId.of(e.getId()),
            e.getManagerId(),
            e.getName(),
            e.getDescription(),
            Address.of(e.getStreet(), e.getCity(), e.getCountry(),
                e.getLatitude(), e.getLongitude()),
            e.getStarRating(),
            e.getStatus(),
            rooms,
            e.getCreatedAt(),
            e.getUpdatedAt()
        );
    }

    private Room toRoomDomain(RoomJpaEntity e) {
        Room room = new Room(
            RoomId.of(e.getId()),
            e.getHotel().getId(),
            e.getRoomNumber(),
            e.getRoomType(),
            Money.of(e.getRatePerNight(), e.getCurrency()),
            e.getMaxOccupancy()
        );

        e.getReservations().forEach(res -> {
            RoomReservation reservation = new RoomReservation(
                res.getBookingId(), res.getUserId(),
                DateRange.of(res.getCheckInDate(), res.getCheckOutDate()));
            if (res.isConfirmed()) reservation.confirm();
            room.getReservations(); // unmodifiable — use reflection or reconstitute
        });

        return room;
    }
}
