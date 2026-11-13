package com.hotelbooking.dto;

import com.hotelbooking.entity.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record BookingResponse(
        String bookingReference,
        BookingStatus status,
        Long hotelId,
        String hotelName,
        String city,
        String roomType,
        String customerName,
        String customerEmail,
        LocalDate checkIn,
        LocalDate checkOut,
        Integer nights,
        BigDecimal pricePerNight,
        BigDecimal totalAmount,
        LocalDateTime createdAt,
        LocalDateTime cancelledAt
) {
}