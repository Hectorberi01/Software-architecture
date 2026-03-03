package com.parking.shared.util;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class BusinessDayUtils {

    private BusinessDayUtils() {
    }

    /**
     * Counts the number of business days (Monday–Friday) between two dates
     * inclusive.
     */
    public static long countBusinessDays(LocalDate start, LocalDate end) {
        if (start.isAfter(end))
            return 0;
        long count = 0;
        LocalDate current = start;
        while (!current.isAfter(end)) {
            DayOfWeek day = current.getDayOfWeek();
            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                count++;
            }
            current = current.plusDays(1);
        }
        return count;
    }

    /**
     * Checks if a date is a business day.
     */
    public static boolean isBusinessDay(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
    }
}
