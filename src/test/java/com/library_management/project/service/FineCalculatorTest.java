package com.library_management.project.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Equivalence partitioning + boundary value analysis for the fine schedule:
 *   Invalid:      daysOverdue < 0
 *   No fine:      daysOverdue == 0
 *   Low band:     1-7 days   @ 0.50/day
 *   Mid band:     8-30 days  @ 1.00/day
 *   High band:    31+ days   @ 2.00/day
 * Boundaries under test: -1, 0, 1, 7, 8, 30, 31.
 */
class FineCalculatorTest {

    private final FineCalculator calculator = new FineCalculator();

    @Test
    @DisplayName("negative days overdue is an invalid partition and is rejected")
    void rejectsNegativeDays() {
        assertThatThrownBy(() -> calculator.calculate(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest(name = "{0} days overdue -> {1}")
    @CsvSource({
        // days, expected fine
        "0,   0.00",   // no-fine partition
        "1,   0.50",   // lower boundary of low band
        "7,   3.50",   // upper boundary of low band
        "8,   8.00",   // lower boundary of mid band
        "30,  30.00",  // upper boundary of mid band
        "31,  62.00",  // lower boundary of high band
        "100, 200.00"  // interior of high band
    })
    void calculatesFineForBand(int daysOverdue, String expected) {
        assertThat(calculator.calculate(daysOverdue)).isEqualByComparingTo(new BigDecimal(expected));
    }
}
