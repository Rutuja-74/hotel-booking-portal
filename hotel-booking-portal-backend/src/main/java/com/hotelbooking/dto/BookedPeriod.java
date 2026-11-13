package com.hotelbooking.dto;

import java.time.LocalDate;

public record BookedPeriod(
        Long roomId,
        LocalDate checkIn,
        LocalDate checkOut
) {
}