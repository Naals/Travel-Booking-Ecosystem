package com.travel.analytics.application.dto.response;

import java.time.LocalDate;

public record BookingFunnelResponse(
    String    bookingType, // null = aggregated across all types
    LocalDate fromDate,
    LocalDate toDate,
    long      createdCount,
    long      confirmedCount,
    long      completedCount,
    long      cancelledCount,
    Double    confirmationRate, // confirmed / created — null if createdCount is 0
    Double    completionRate,   // completed / confirmed — null if confirmedCount is 0
    Double    cancellationRate  // cancelled / created — null if createdCount is 0
) {
    public static BookingFunnelResponse of(String bookingType, LocalDate from, LocalDate to,
                                           long created, long confirmed,
                                           long completed, long cancelled) {
        return new BookingFunnelResponse(
            bookingType, from, to, created, confirmed, completed, cancelled,
            rate(confirmed, created),
            rate(completed, confirmed),
            rate(cancelled, created));
    }

    private static Double rate(long numerator, long denominator) {
        return denominator == 0 ? null : (double) numerator / denominator;
    }
}
