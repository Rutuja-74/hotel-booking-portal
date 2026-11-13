package com.hotelbooking.dto;

import java.math.BigDecimal;
import java.util.List;

public record HotelResponse(
        Long id,
        String name,
        String city,
        String state,
        String description,
        List<String> amenities,
        String category,
        Integer starRating,
        String imageUrl,
        Long roomId,
        String roomType,
        BigDecimal pricePerNight,
        Integer totalRooms,
        Integer maxGuests,
        Integer availableRooms,
        Integer nights,
        BigDecimal totalAmount
) {
}