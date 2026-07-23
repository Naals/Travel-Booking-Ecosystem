package com.travel.property.application.usecase;

import com.travel.property.application.dto.request.CreatePropertyRequest;
import com.travel.property.application.dto.response.PropertyResponse;
import com.travel.property.domain.aggregate.Property;
import com.travel.property.domain.repository.PropertyRepository;
import com.travel.property.domain.valueobject.*;
import com.travel.property.infrastructure.messaging.producer.PropertyEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates a property listing in DRAFT status.
 * Host must call publish() separately to make it active in search results.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreatePropertyUseCase {

    private final PropertyRepository    repository;
    private final PropertyEventPublisher eventPublisher;

    @Transactional
    public PropertyResponse execute(String hostId, CreatePropertyRequest request) {
        log.info("Creating property for host={} title={}", hostId, request.title());

        Address address = Address.of(
            request.street(), request.city(), request.state(),
            request.country(), request.postalCode(),
            request.latitude(), request.longitude());

        Money nightlyRate = Money.of(request.nightlyRate(), request.currency());

        Property property = Property.create(
            hostId,
            request.title(),
            request.description(),
            PropertyType.valueOf(request.propertyType()),
            address,
            nightlyRate,
            request.maxGuests(),
            request.bedrooms(),
            request.bathrooms()
        );

        Property saved = repository.save(property);
        log.info("Property created: {}", saved.getId().getValue());
        return PropertyResponse.from(saved);
    }
}
