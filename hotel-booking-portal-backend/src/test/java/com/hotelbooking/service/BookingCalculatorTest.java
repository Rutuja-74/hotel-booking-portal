package com.hotelbooking.service;

import com.hotelbooking.dto.BookedPeriod;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BookingCalculatorTest {

    private BookedPeriod booking(String checkIn, String checkOut) {
        return new BookedPeriod(1L, LocalDate.parse(checkIn), LocalDate.parse(checkOut));
    }

    // ---------- number of nights ----------

    @Test
    void nights_threeNightStay() {
        int nights = BookingCalculator.calculateNights(
                LocalDate.of(2026, 12, 10), LocalDate.of(2026, 12, 13));
        assertEquals(3, nights);
    }

    @Test
    void nights_singleNight() {
        int nights = BookingCalculator.calculateNights(
                LocalDate.of(2026, 12, 10), LocalDate.of(2026, 12, 11));
        assertEquals(1, nights);
    }

    @Test
    void nights_acrossMonthAndYearBoundary() {
        int nights = BookingCalculator.calculateNights(
                LocalDate.of(2026, 12, 30), LocalDate.of(2027, 1, 2));
        assertEquals(3, nights);
    }

    @Test
    void nights_acrossLeapDay() {
        int nights = BookingCalculator.calculateNights(
                LocalDate.of(2028, 2, 28), LocalDate.of(2028, 3, 1));
        assertEquals(2, nights);
    }

    // ---------- total amount ----------

    @Test
    void total_isNightsTimesPrice() {
        BigDecimal total = BookingCalculator.calculateTotal(new BigDecimal("7500.00"), 3);
        assertEquals(new BigDecimal("22500.00"), total);
    }

    @Test
    void total_keepsTwoDecimalPlaces() {
        BigDecimal total = BookingCalculator.calculateTotal(new BigDecimal("1999.99"), 3);
        assertEquals(new BigDecimal("5999.97"), total);
    }

    // ---------- busiest night (availability) ----------

    @Test
    void maxRoomsOccupied_noBookings_isZero() {
        int occupied = BookingCalculator.maxRoomsOccupied(
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 7), List.of());
        assertEquals(0, occupied);
    }

    @Test
    void maxRoomsOccupied_sequentialBookingsUseOneRoomPerNight() {
        List<BookedPeriod> bookings = List.of(
                booking("2026-12-01", "2026-12-03"),
                booking("2026-12-03", "2026-12-05"),
                booking("2026-12-05", "2026-12-07"));

        int occupied = BookingCalculator.maxRoomsOccupied(
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 7), bookings);

        // Three bookings overlap the request, but never on the same night.
        assertEquals(1, occupied);
    }

    @Test
    void maxRoomsOccupied_overlappingBookingsCountPerNight() {
        List<BookedPeriod> bookings = List.of(
                booking("2026-12-01", "2026-12-05"),
                booking("2026-12-03", "2026-12-07"));

        int occupied = BookingCalculator.maxRoomsOccupied(
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 7), bookings);

        // Nights of 3 Dec and 4 Dec are used by both bookings.
        assertEquals(2, occupied);
    }

    @Test
    void maxRoomsOccupied_bookingEndingOnCheckInIsNotCounted() {
        List<BookedPeriod> bookings = List.of(booking("2026-12-01", "2026-12-03"));

        int occupied = BookingCalculator.maxRoomsOccupied(
                LocalDate.of(2026, 12, 3), LocalDate.of(2026, 12, 5), bookings);

        // The earlier stay checks out on the 3rd, the new one checks in on the 3rd.
        assertEquals(0, occupied);
    }
}