package com.travel.property.application.usecase;

import com.travel.property.application.dto.response.PropertyResponse;
import com.travel.property.domain.repository.PropertyRepository;
import com.travel.property.domain.valueobject.PropertyId;
import com.travel.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetPropertyUseCase {

    private final PropertyRepository repository;

    @Transactional(readOnly = true)
    public PropertyResponse execute(String propertyId) {
        return repository.findById(PropertyId.of(propertyId))
            .map(PropertyResponse::from)
            .orElseThrow(() -> new ResourceNotFoundException("Property", propertyId));
    }

    @Transactional(readOnly = true)
    public List<PropertyResponse> executeForHost(String hostId) {
        return repository.findByHostId(hostId)
            .stream()
            .map(PropertyResponse::from)
            .toList();
    }
}
