package com.travel.property.infrastructure.persistence;

import com.travel.property.domain.aggregate.Property;
import com.travel.property.domain.repository.PropertyRepository;
import com.travel.property.domain.valueobject.PropertyId;
import com.travel.property.domain.valueobject.PropertyStatus;
import com.travel.property.infrastructure.persistence.entity.ReservationJpaEntity;
import com.travel.property.infrastructure.persistence.mapper.PropertyMapper;
import com.travel.property.infrastructure.persistence.repository.PropertyJpaRepository;
import com.travel.property.infrastructure.persistence.repository.ReservationJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PropertyRepositoryAdapter implements PropertyRepository {

    private final PropertyJpaRepository    jpa;
    private final ReservationJpaRepository reservationJpa;
    private final PropertyMapper           mapper;

    @Override
    @Transactional
    public Property save(Property property) {
        var entity = mapper.toEntity(property);
        jpa.save(entity);

        // Sync reservations — delete all and re-insert
        // Simple strategy: property reservations are a small collection
        reservationJpa.deleteAll(
            reservationJpa.findByPropertyId(property.getId().getValue()));

        property.getReservations().forEach(r ->
            reservationJpa.save(
                mapper.toReservationEntity(property.getId().getValue(), r)));

        List<ReservationJpaEntity> savedReservations =
            reservationJpa.findByPropertyId(property.getId().getValue());

        return mapper.toDomain(entity, savedReservations);
    }

    @Override
    public Optional<Property> findById(PropertyId id) {
        return jpa.findById(id.getValue()).map(e -> {
            List<ReservationJpaEntity> reservations =
                reservationJpa.findByPropertyId(id.getValue());
            return mapper.toDomain(e, reservations);
        });
    }

    @Override
    public List<Property> findByHostId(String hostId) {
        return jpa.findByHostId(hostId).stream()
            .map(e -> mapper.toDomain(e,
                reservationJpa.findByPropertyId(e.getId())))
            .toList();
    }

    @Override
    public List<Property> findByStatus(PropertyStatus status) {
        return jpa.findByStatus(status).stream()
            .map(e -> mapper.toDomain(e,
                reservationJpa.findByPropertyId(e.getId())))
            .toList();
    }

    @Override
    public List<Property> findByCity(String city) {
        return jpa.findByCityIgnoreCase(city).stream()
            .map(e -> mapper.toDomain(e,
                reservationJpa.findByPropertyId(e.getId())))
            .toList();
    }

    @Override
    public boolean existsById(PropertyId id) {
        return jpa.existsById(id.getValue());
    }
}
