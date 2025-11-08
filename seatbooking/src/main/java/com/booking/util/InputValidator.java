package com.booking.util;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.LocalDate;

/**
 * Small input validation utilities used across UI and services.
 */
public final class InputValidator {
    private InputValidator() { }

    public static boolean isValidUsername(String username) {
        return username != null && username.matches("^[A-Za-z0-9_.-]{3,30}$");
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 4; // keep simple rule for now
    }

    public static boolean isValidDate(String dateStr) {
        if (dateStr == null) return false;
        try {
            LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Returns true if the provided date string is a valid ISO_LOCAL_DATE and
     * the date is today or in the future.
     */
    public static boolean isNotPastDate(String dateStr) {
        if (!isValidDate(dateStr)) return false;
        LocalDate d = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        return !d.isBefore(LocalDate.now());
    }
}
