package com.travel.property.domain.valueobject;

public enum PropertyStatus {
    DRAFT,       // created but not yet published
    ACTIVE,      // visible in search results
    PAUSED,      // temporarily hidden by host
    DEACTIVATED  // permanently removed
}
