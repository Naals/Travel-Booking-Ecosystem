package com.travel.property.infrastructure.persistence.mapper;

import com.travel.property.domain.aggregate.Property;
import com.travel.property.domain.aggregate.Reservation;
import com.travel.property.domain.valueobject.*;
import com.travel.property.infrastructure.persistence.entity.PropertyJpaEntity;
import com.travel.property.infrastructure.persistence.entity.ReservationJpaEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PropertyMapper {

    public PropertyJpaEntity toEntity(Property p) {
        return PropertyJpaEntity.builder()
            .id(p.getId().getValue())
            .hostId(p.getHostId())
            .title(p.getTitle())
            .description(p.getDescription())
            .type(p.getType())
            .status(p.getStatus())
            .street(p.getAddress().getStreet())
            .city(p.getAddress().getCity())
            .state(p.getAddress().getState())
            .country(p.getAddress().getCountry())
            .postalCode(p.getAddress().getPostalCode())
            .latitude(p.getAddress().getLatitude())
            .longitude(p.getAddress().getLongitude())
            .nightlyRate(p.getNightlyRate().getAmount())
            .currency(p.getNightlyRate().getCurrency())
            .maxGuests(p.getMaxGuests())
            .bedrooms(p.getBedrooms())
            .bathrooms(p.getBathrooms())
            .amenities(p.getAmenities())
            .createdAt(p.getCreatedAt())
            .updatedAt(p.getUpdatedAt())
            .build();
    }

    public Property toDomain(PropertyJpaEntity e, List<ReservationJpaEntity> reservationEntities) {
        List<Reservation> reservations = reservationEntities.stream()
            .map(r -> {
                Reservation res = new Reservation(
                    r.getBookingId(), r.getUserId(),
                    DateRange.of(r.getCheckInDate(), r.getCheckOutDate()));
                if (r.isConfirmed()) res.confirm();
                return res;
            }).toList();

        return Property.reconstitute(
            PropertyId.of(e.getId()),
            e.getHostId(),
            e.getTitle(),
            e.getDescription(),
            e.getType(),
            e.getStatus(),
            Address.of(e.getStreet(), e.getCity(), e.getState(),
                e.getCountry(), e.getPostalCode(),
                e.getLatitude(), e.getLongitude()),
            Money.of(e.getNightlyRate(), e.getCurrency()),
            e.getMaxGuests(),
            e.getBedrooms(),
            e.getBathrooms(),
            e.getAmenities(),
            reservations,
            e.getCreatedAt(),
            e.getUpdatedAt()
        );
    }

    public ReservationJpaEntity toReservationEntity(String propertyId, Reservation r) {
        return ReservationJpaEntity.builder()
            .propertyId(propertyId)
            .bookingId(r.getBookingId())
            .userId(r.getUserId())
            .checkInDate(r.getDateRange().getStart())
            .checkOutDate(r.getDateRange().getEnd())
            .confirmed(r.isConfirmed())
            .build();
    }
}
