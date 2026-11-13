package com.hotelbooking.service;

import com.hotelbooking.dto.BookedPeriod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Pure calculation helpers (no database, no Spring), so they are simple to test.
 */
public final class BookingCalculator {

    private BookingCalculator() {
    }

    // 10 Dec -> 13 Dec = 3 nights
    public static int calculateNights(LocalDate checkIn, LocalDate checkOut) {
        return (int) ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    // total = nights x price per night, always calculated on the backend
    public static BigDecimal calculateTotal(BigDecimal pricePerNight, int nights) {
        return pricePerNight
                .multiply(BigDecimal.valueOf(nights))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * The highest number of rooms in use on any single night of the requested stay.
     * A booking uses the nights from its check-in up to (not including) its check-out.
     */
    public static int maxRoomsOccupied(LocalDate checkIn, LocalDate checkOut,
                                       List<BookedPeriod> bookings) {
        int maxOccupied = 0;
        for (LocalDate night = checkIn; night.isBefore(checkOut); night = night.plusDays(1)) {
            int occupiedTonight = 0;
            for (BookedPeriod booking : bookings) {
                boolean coversNight = !night.isBefore(booking.checkIn())
                        && night.isBefore(booking.checkOut());
                if (coversNight) {
                    occupiedTonight++;
                }
            }
            maxOccupied = Math.max(maxOccupied, occupiedTonight);
        }
        return maxOccupied;
    }
}