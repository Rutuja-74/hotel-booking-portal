package com.hotelbooking.validation;

import com.hotelbooking.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StayDateValidatorTest {

    // A fixed "today" keeps the tests predictable.
    private static final LocalDate TODAY = LocalDate.of(2026, 12, 1);

    @Test
    void validStay_passes() {
        assertDoesNotThrow(() ->
                StayDateValidator.validate(TODAY.plusDays(5), TODAY.plusDays(8), TODAY));
    }

    @Test
    void checkInToday_isAllowed() {
        assertDoesNotThrow(() ->
                StayDateValidator.validate(TODAY, TODAY.plusDays(1), TODAY));
    }

    @Test
    void checkInYesterday_isRejected() {
        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                StayDateValidator.validate(TODAY.minusDays(1), TODAY.plusDays(1), TODAY));
        assertEquals("Check-in date cannot be in the past.", ex.getMessage());
    }

    @Test
    void checkOutSameAsCheckIn_isRejected() {
        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                StayDateValidator.validate(TODAY.plusDays(2), TODAY.plusDays(2), TODAY));
        assertEquals("Check-out date must be after check-in date.", ex.getMessage());
    }

    @Test
    void checkOutBeforeCheckIn_isRejected() {
        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                StayDateValidator.validate(TODAY.plusDays(5), TODAY.plusDays(3), TODAY));
        assertEquals("Check-out date must be after check-in date.", ex.getMessage());
    }

    @Test
    void missingDates_areRejected() {
        assertThrows(BadRequestException.class, () ->
                StayDateValidator.validate(null, TODAY.plusDays(3), TODAY));
        assertThrows(BadRequestException.class, () ->
                StayDateValidator.validate(TODAY.plusDays(1), null, TODAY));
    }

    @Test
    void thirtyNights_isAllowed() {
        assertDoesNotThrow(() ->
                StayDateValidator.validate(TODAY.plusDays(1), TODAY.plusDays(31), TODAY));
    }

    @Test
    void moreThanThirtyNights_isRejected() {
        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                StayDateValidator.validate(TODAY.plusDays(1), TODAY.plusDays(32), TODAY));
        assertEquals("A stay cannot be longer than 30 nights.", ex.getMessage());
    }
}