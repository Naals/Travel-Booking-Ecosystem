package com.travel.hotel.application.dto.request;

import jakarta.validation.constraints.*;

public record CreateHotelRequest(

    @NotBlank(message = "Hotel name is required")
    @Size(min = 2, max = 100)
    String name,

    @NotBlank(message = "Description is required")
    @Size(min = 20, max = 2000)
    String description,

    @NotBlank String street,
    @NotBlank String city,

    @NotBlank
    @Size(min = 2, max = 2, message = "Country must be ISO 3166-1 alpha-2")
    String country,

    double latitude,
    double longitude,

    @Min(1) @Max(5)
    int starRating
) {}
