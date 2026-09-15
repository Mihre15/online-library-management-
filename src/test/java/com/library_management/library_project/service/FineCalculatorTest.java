package com.library_management.library_project.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Boundary-value tests for {@link FineCalculator}'s fine schedule:
 * <pre>
 *   &lt;= 0 days   -> no fine
 *   1-7 days    -> 0.50 per day
 *   8-30 days   -> 1.00 per day
 *   31+ days    -> 2.00 per day
 * </pre>
 * Each band boundary (0/1, 7/8, 30/31) gets an explicit test on both sides,
 * plus a representative mid-band value per equivalence class and the
 * negative-input error case.
 */
class FineCalculatorTest {

    private final FineCalculator fineCalculator = new FineCalculator();

    @Test
    @DisplayName("negative days overdue is invalid")
    void negativeDaysOverdue_throws() {
        assertThatThrownBy(() -> fineCalculator.calculate(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("-1");
    }

    @Test
    @DisplayName("boundary: 0 days overdue -> no fine")
    void zeroDaysOverdue_noFine() {
        assertThat(fineCalculator.calculate(0)).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("boundary: 1 day overdue (low band lower bound) -> 0.50")
    void oneDayOverdue_lowBandRate() {
        assertThat(fineCalculator.calculate(1)).isEqualByComparingTo("0.50");
    }

    @Test
    @DisplayName("mid-range low band: 4 days overdue -> 2.00")
    void midLowBand_ratePerDay() {
        assertThat(fineCalculator.calculate(4)).isEqualByComparingTo("2.00");
    }

    @Test
    @DisplayName("boundary: 7 days overdue (low band upper bound) -> 3.50")
    void sevenDaysOverdue_lowBandUpperBound() {
        assertThat(fineCalculator.calculate(7)).isEqualByComparingTo("3.50");
    }

    @Test
    @DisplayName("boundary: 8 days overdue (mid band lower bound) -> 8.00")
    void eightDaysOverdue_midBandLowerBound() {
        assertThat(fineCalculator.calculate(8)).isEqualByComparingTo("8.00");
    }

    @Test
    @DisplayName("mid-range mid band: 15 days overdue -> 15.00")
    void midMidBand_ratePerDay() {
        assertThat(fineCalculator.calculate(15)).isEqualByComparingTo("15.00");
    }

    @Test
    @DisplayName("boundary: 30 days overdue (mid band upper bound) -> 30.00")
    void thirtyDaysOverdue_midBandUpperBound() {
        assertThat(fineCalculator.calculate(30)).isEqualByComparingTo("30.00");
    }

    @Test
    @DisplayName("boundary: 31 days overdue (high band lower bound) -> 62.00")
    void thirtyOneDaysOverdue_highBandLowerBound() {
        assertThat(fineCalculator.calculate(31)).isEqualByComparingTo("62.00");
    }

    @Test
    @DisplayName("mid-range high band: 60 days overdue -> 120.00")
    void midHighBand_ratePerDay() {
        assertThat(fineCalculator.calculate(60)).isEqualByComparingTo("120.00");
    }
}
