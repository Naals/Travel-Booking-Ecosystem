package com.travel.analytics.application.usecase;

import com.travel.analytics.domain.model.DailyRevenueMetric;
import com.travel.analytics.domain.repository.DailyRevenueMetricRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class RecordPaymentCompletedUseCase {

    private final DailyRevenueMetricRepository repository;

    @Transactional
    public void execute(BigDecimal amount, String currency, LocalDate date) {
        DailyRevenueMetric metric = repository.findByDateAndCurrency(date, currency)
            .orElseGet(() -> DailyRevenueMetric.initial(date, currency));
        metric.addGrossRevenue(amount);
        repository.save(metric);
    }
}
