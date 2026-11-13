package com.hotelbooking.service;

import com.hotelbooking.dto.HotelResponse;
import com.hotelbooking.dto.RecommendationResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecommendationServiceTest {

    // Helper that builds a hotel the way the API returns it (available for a 2-night stay)
    private HotelResponse hotel(long id, String city, String category, int stars,
                                String price, int availableRooms) {
        BigDecimal pricePerNight = new BigDecimal(price);
        return new HotelResponse(id, "Hotel " + id, city, "State", "Description", List.of(),
                category, stars, null, id, "Deluxe", pricePerNight, 10, 2,
                availableRooms, 2, pricePerNight.multiply(BigDecimal.valueOf(2)));
    }

    @Test
    void hotelWithinBudgetScoresHigherThanHotelOverBudget() {
        HotelResponse cheap = hotel(1, "Goa", "BUDGET", 3, "2400", 8);
        HotelResponse expensive = hotel(2, "Goa", "LUXURY", 3, "7500", 8);
        BigDecimal budget = new BigDecimal("3000");

        RecommendationResponse cheapResult = RecommendationService.score(cheap, null, budget);
        RecommendationResponse expensiveResult = RecommendationService.score(expensive, null, budget);

        assertTrue(cheapResult.score() > expensiveResult.score());
        assertTrue(cheapResult.reasons().contains("Within your budget"));
    }

    @Test
    void sameCityAndCategoryAsWantedHotelGetsSimilarityPoints() {
        HotelResponse wanted = hotel(1, "Goa", "LUXURY", 5, "7500", 0);
        HotelResponse similar = hotel(2, "Goa", "LUXURY", 4, "7000", 5);
        HotelResponse different = hotel(3, "Manali", "STANDARD", 4, "3200", 5);

        RecommendationResponse similarResult = RecommendationService.score(similar, wanted, null);
        RecommendationResponse differentResult = RecommendationService.score(different, wanted, null);

        assertTrue(similarResult.score() > differentResult.score());
        assertTrue(similarResult.reasons().contains("Same city (Goa)"));
        assertTrue(similarResult.reasons().contains("Same category (LUXURY)"));
    }

    @Test
    void rankSortsBestFirstUsingBudgetFit() {
        // Same stars and rooms, so only the price decides: within budget > slightly over > far over
        List<HotelResponse> hotels = List.of(
                hotel(1, "Goa", "LUXURY", 4, "7500", 8),
                hotel(2, "Goa", "BUDGET", 4, "2400", 8),
                hotel(3, "Goa", "STANDARD", 4, "3200", 8));

        List<RecommendationResponse> ranked =
                RecommendationService.rank(hotels, null, new BigDecimal("3000"));

        List<Long> ids = ranked.stream().map(result -> result.hotel().id()).toList();
        assertEquals(List.of(2L, 3L, 1L), ids);
    }

    @Test
    void rankSkipsTheWantedHotelAndReturnsAtMostThree() {
        HotelResponse wanted = hotel(1, "Goa", "LUXURY", 5, "7500", 0);
        List<HotelResponse> hotels = List.of(
                wanted,
                hotel(2, "Goa", "LUXURY", 4, "7000", 5),
                hotel(3, "Goa", "BUDGET", 3, "2400", 9),
                hotel(4, "Jaipur", "LUXURY", 5, "8500", 6),
                hotel(5, "Kochi", "PREMIUM", 4, "4800", 5));

        List<RecommendationResponse> ranked = RecommendationService.rank(hotels, wanted, null);

        assertEquals(3, ranked.size());
        assertTrue(ranked.stream().noneMatch(result -> result.hotel().id().equals(1L)));
    }
}