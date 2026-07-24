package com.travel.hotel.application.dto.response;

import com.travel.hotel.domain.aggregate.Hotel;

import java.time.Instant;
import java.util.List;

public record HotelResponse(
    String       hotelId,
    String       managerId,
    String       name,
    String       description,
    String       city,
    String       country,
    double       latitude,
    double       longitude,
    int          starRating,
    String       status,
    int          totalRooms,
    List<RoomSummary> rooms,
    Instant      createdAt
) {
    public static HotelResponse from(Hotel h) {
        List<RoomSummary> roomSummaries = h.getRooms().stream()
            .map(r -> new RoomSummary(
                r.getId().getValue(),
                r.getRoomNumber(),
                r.getRoomType().name(),
                r.getStatus().name(),
                r.getRatePerNight().getAmount(),
                r.getRatePerNight().getCurrency(),
                r.getMaxOccupancy()))
            .toList();

        return new HotelResponse(
            h.getId().getValue(),
            h.getManagerId(),
            h.getName(),
            h.getDescription(),
            h.getAddress().getCity(),
            h.getAddress().getCountry(),
            h.getAddress().getLatitude(),
            h.getAddress().getLongitude(),
            h.getStarRating(),
            h.getStatus().name(),
            h.getRooms().size(),
            roomSummaries,
            h.getCreatedAt()
        );
    }

    public record RoomSummary(
        String     roomId,
        String     roomNumber,
        String     roomType,
        String     status,
        java.math.BigDecimal ratePerNight,
        String     currency,
        int        maxOccupancy
    ) {}
}
