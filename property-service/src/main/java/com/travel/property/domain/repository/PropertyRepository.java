package com.travel.property.domain.repository;

import com.travel.property.domain.aggregate.Property;
import com.travel.property.domain.valueobject.PropertyId;
import com.travel.property.domain.valueobject.PropertyStatus;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PropertyRepository {
    Property           save(Property property);
    Optional<Property> findById(PropertyId id);
    List<Property>     findByHostId(String hostId);
    List<Property>     findByStatus(PropertyStatus status);
    List<Property>     findByCity(String city);
    boolean            existsById(PropertyId id);
}
