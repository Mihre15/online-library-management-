package com.library_management.project.integration;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/** Overrides the application's {@link Clock} bean so integration tests get a deterministic "today". */
@TestConfiguration
public class FixedClockTestConfig {

    public static final LocalDate FIXED_TODAY = LocalDate.of(2026, 6, 15);

    @Bean
    @Primary
    public Clock testClock() {
        return Clock.fixed(FIXED_TODAY.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC);
    }
}
