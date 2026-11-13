package com.hotelbooking.dto;

import java.util.List;

// One recommended hotel: the hotel details, its score, and why it was picked
public record RecommendationResponse(
        HotelResponse hotel,
        int score,
        List<String> reasons
) {
}