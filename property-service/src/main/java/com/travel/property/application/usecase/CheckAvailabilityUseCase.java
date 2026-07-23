package com.travel.property.application.usecase;

import com.travel.property.domain.aggregate.Property;
import com.travel.property.domain.repository.PropertyRepository;
import com.travel.property.domain.valueobject.DateRange;
import com.travel.property.domain.valueobject.PropertyId;
import com.travel.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CheckAvailabilityUseCase {

    @Qualifier("propertyRepositoryAdapter")
    private final PropertyRepository repository;

    @Transactional(readOnly = true)
    public boolean execute(String propertyId, LocalDate checkIn, LocalDate checkOut) {
        Property property = repository.findById(PropertyId.of(propertyId))
            .orElseThrow(() -> new ResourceNotFoundException("Property", propertyId));

        return property.isAvailable(DateRange.of(checkIn, checkOut));
    }
}
