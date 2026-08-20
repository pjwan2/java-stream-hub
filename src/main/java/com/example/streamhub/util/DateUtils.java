package com.example.streamhub.util;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;


public final class DateUtils {

    private DateUtils() {
    }

    public static long daysBetween(Instant from, Instant to) {
        return Duration.between(from, to).toDays();
}
    public static boolean isWithinLastDays(Instant instant, long days) {
        return daysBetween(instant, Instant.now()) <= days;
}
    public static String format(Instant instant, String pattern) {
    return DateTimeFormatter.ofPattern(pattern)
            .withZone(ZoneOffset.UTC)
            .format(instant);
}



}
