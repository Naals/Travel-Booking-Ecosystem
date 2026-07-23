package com.travel.property.application.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreatePropertyRequest(

    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 100, message = "Title must be 5-100 characters")
    String title,

    @NotBlank(message = "Description is required")
    @Size(min = 20, max = 2000, message = "Description must be 20-2000 characters")
    String description,

    @NotBlank(message = "Property type is required")
    String propertyType,

    @NotBlank(message = "Street is required")
    String street,

    @NotBlank(message = "City is required")
    String city,

    String state,

    @NotBlank(message = "Country is required")
    @Size(min = 2, max = 2, message = "Country must be ISO 3166-1 alpha-2 code")
    String country,

    String postalCode,

    double latitude,
    double longitude,

    @NotNull @DecimalMin(value = "1.00", message = "Nightly rate must be at least 1.00")
    BigDecimal nightlyRate,

    @NotBlank @Size(min = 3, max = 3) String currency,

    @Min(value = 1, message = "At least 1 guest required") int maxGuests,
    @Min(value = 0, message = "Bedrooms must be non-negative") int bedrooms,
    @Min(value = 1, message = "At least 1 bathroom required") int bathrooms
) {}
