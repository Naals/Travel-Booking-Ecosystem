package com.travel.analytics.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RevenueSummaryResponse(
    String     currency,
    LocalDate  fromDate,
    LocalDate  toDate,
    BigDecimal grossRevenue,
    BigDecimal refundedAmount,
    BigDecimal netRevenue
) {}
