package com.travel.property.application.usecase;

import com.travel.property.application.dto.response.PropertyResponse;
import com.travel.property.domain.aggregate.Property;
import com.travel.property.domain.repository.PropertyRepository;
import com.travel.property.domain.valueobject.PropertyId;
import com.travel.property.infrastructure.messaging.producer.PropertyEventPublisher;
import com.travel.common.exception.BusinessRuleViolationException;
import com.travel.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublishPropertyUseCase {

    @Qualifier("propertyRepositoryAdapter")
    private final PropertyRepository     repository;
    private final PropertyEventPublisher eventPublisher;

    @Transactional
    public PropertyResponse execute(String propertyId, String hostId) {
        Property property = repository.findById(PropertyId.of(propertyId))
            .orElseThrow(() -> new ResourceNotFoundException("Property", propertyId));

        if (!property.getHostId().equals(hostId))
            throw new BusinessRuleViolationException(
                "Access denied to this property", "FORBIDDEN");

        property.publish();
        Property saved = repository.save(property);

        // PropertyCreatedEvent → search-service indexes this property
        eventPublisher.publishEvents(saved.getDomainEvents());
        saved.clearDomainEvents();

        log.info("Property published: {}", propertyId);
        return PropertyResponse.from(saved);
    }
}
