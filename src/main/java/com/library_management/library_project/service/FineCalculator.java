package com.library_management.library_project.service;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;

/**
 * Fine schedule (equivalence classes, by days overdue):
 *   <= 0 days   -> no fine
 *   1-7 days    -> 0.50 per day
 *   8-30 days   -> 1.00 per day
 *   31+ days    -> 2.00 per day
 */
@Component
public class FineCalculator {

    static final BigDecimal LOW_BAND_RATE = new BigDecimal("0.50");
    static final BigDecimal MID_BAND_RATE = new BigDecimal("1.00");
    static final BigDecimal HIGH_BAND_RATE = new BigDecimal("2.00");

    static final int LOW_BAND_MAX_DAYS = 7;
    static final int MID_BAND_MAX_DAYS = 30;

    public BigDecimal calculate(int daysOverdue) {
        if (daysOverdue < 0) {
            throw new IllegalArgumentException("daysOverdue cannot be negative: " + daysOverdue);
        }
        if (daysOverdue == 0) {
            return BigDecimal.ZERO;
        }
        if (daysOverdue <= LOW_BAND_MAX_DAYS) {
            return LOW_BAND_RATE.multiply(BigDecimal.valueOf(daysOverdue));
        }
        if (daysOverdue <= MID_BAND_MAX_DAYS) {
            return MID_BAND_RATE.multiply(BigDecimal.valueOf(daysOverdue));
        }
        return HIGH_BAND_RATE.multiply(BigDecimal.valueOf(daysOverdue));
    }
}
