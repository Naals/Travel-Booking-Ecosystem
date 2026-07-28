package com.travel.vehicle.application.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record AddVehicleRequest(

    @NotBlank(message = "Make is required")
    String make,

    @NotBlank(message = "Model is required")
    String model,

    @Min(1990) @Max(2100)
    int year,

    @NotBlank(message = "License plate is required")
    String licensePlate,

    @Min(1) @Max(20)
    int seats,

    @NotBlank String transmissionType,
    @NotBlank String fuelType,
    boolean airConditioning,

    @NotBlank String category,

    @NotBlank String locationCode,
    @NotBlank String locationCity,
    @NotBlank String locationCountry,
    String locationAddress,

    @NotNull @DecimalMin("1.00") BigDecimal dailyRate,
    @NotBlank @Size(min = 3, max = 3) String currency
) {}
