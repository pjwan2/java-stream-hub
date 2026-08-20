package com.example.streamhub.util;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DateUtilsTest {

    @Test
    void daysBetweenShouldCountFullDays() {
        Instant from = Instant.now().minus(3, ChronoUnit.DAYS);
        Instant to = Instant.now();

        long result = DateUtils.daysBetween(from, to);

        assertEquals(3, result);
    }

    @Test
    void isWithinLastDaysShouldReturnTrueForRecentInstant() {
        Instant recent = Instant.now().minus(2, ChronoUnit.DAYS);

        boolean result = DateUtils.isWithinLastDays(recent, 7);

        assertTrue(result);
    }

    @Test
    void isWithinLastDaysShouldReturnFalseForOldInstant() {
        Instant old = Instant.now().minus(30, ChronoUnit.DAYS);

        boolean result = DateUtils.isWithinLastDays(old, 7);

        assertFalse(result);
    }

    @Test
    void formatShouldRenderPatternInUtc() {
        Instant fixed = Instant.parse("2026-01-15T10:30:00Z");

        String result = DateUtils.format(fixed, "yyyy-MM-dd");

        assertEquals("2026-01-15", result);
    }
}
