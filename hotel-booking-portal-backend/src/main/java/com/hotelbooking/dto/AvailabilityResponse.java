package com.hotelbooking.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AvailabilityResponse(
        Long roomId,
        Long hotelId,
        String hotelName,
        String roomType,
        LocalDate checkIn,
        LocalDate checkOut,
        Integer nights,
        BigDecimal pricePerNight,
        Integer totalRooms,
        Integer availableRooms,
        boolean available,
        BigDecimal totalAmount
) {
}