package com.hotelbooking.validation;

import com.hotelbooking.exception.BadRequestException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Validates the dates of a stay. "today" is passed in so the rules are easy to unit test.
 */
public final class StayDateValidator {

    public static final int MAX_NIGHTS = 30;

    private StayDateValidator() {
    }

    public static void validate(LocalDate checkIn, LocalDate checkOut, LocalDate today) {
        if (checkIn == null || checkOut == null) {
            throw new BadRequestException("Both check-in and check-out dates are required.");
        }
        if (checkIn.isBefore(today)) {
            throw new BadRequestException("Check-in date cannot be in the past.");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new BadRequestException("Check-out date must be after check-in date.");
        }
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (nights > MAX_NIGHTS) {
            throw new BadRequestException("A stay cannot be longer than " + MAX_NIGHTS + " nights.");
        }
    }
}