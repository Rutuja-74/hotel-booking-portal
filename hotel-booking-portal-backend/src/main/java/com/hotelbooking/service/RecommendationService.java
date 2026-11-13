
package com.hotelbooking.service;

import com.hotelbooking.dto.HotelResponse;
import com.hotelbooking.dto.RecommendationResponse;
import com.hotelbooking.exception.BadRequestException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class RecommendationService {

    static final int MAX_RESULTS = 3;

    // Points used by the recommendation scoring rules
    private static final int WITHIN_BUDGET_POINTS = 40;
    private static final int SAME_CITY_POINTS = 30;
    private static final int SAME_CATEGORY_POINTS = 20;
    private static final int POINTS_PER_STAR = 4;
    private static final int PLENTY_OF_ROOMS_POINTS = 5;
    private static final int PLENTY_OF_ROOMS_LIMIT = 3;

    private final HotelService hotelService;

    public RecommendationService(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    // City, check-in and check-out are required.
    // Budget and hotelId are optional.
    public List<RecommendationResponse> recommend(
            String city,
            LocalDate checkIn,
            LocalDate checkOut,
            BigDecimal budget,
            Long hotelId) {

        if (city == null || city.isBlank()) {
            throw new BadRequestException("City is required.");
        }

        if (checkIn == null || checkOut == null) {
            throw new BadRequestException(
                    "Check-in and check-out dates are required.");
        }

        if (budget != null && budget.signum() <= 0) {
            throw new BadRequestException(
                    "Budget must be greater than zero.");
        }

        // Find available hotels in the selected city.
        List<HotelResponse> available =
                hotelService.searchHotels(
                        city.trim(),
                        null,
                        null,
                        null,
                        null,
                        checkIn,
                        checkOut
                );

        HotelResponse reference = (hotelId == null)
                ? null
                : hotelService.getHotelById(hotelId, null, null);

        return rank(available, reference, budget);
    }

    // Filter hotels by budget, rank them and return the top three.
    static List<RecommendationResponse> rank(
            List<HotelResponse> candidates,
            HotelResponse reference,
            BigDecimal budget) {

        return candidates.stream()
                .filter(hotel ->
                        reference == null ||
                        !hotel.id().equals(reference.id()))
                .filter(hotel ->
                        budget == null ||
                        hotel.pricePerNight().compareTo(budget) <= 0)
                .map(hotel -> score(hotel, reference, budget))
                .sorted(
                        Comparator.comparingInt(
                                RecommendationResponse::score).reversed()
                                .thenComparing(
                                        result -> result.hotel().pricePerNight())
                                .thenComparing(
                                        result -> result.hotel().id())
                )
                .limit(MAX_RESULTS)
                .toList();
    }

    // Calculate recommendation score and reasons.
    static RecommendationResponse score(
            HotelResponse hotel,
            HotelResponse reference,
            BigDecimal budget) {

        int score = 0;
        List<String> reasons = new ArrayList<>();

        reasons.add("Available for your dates");

        if (budget != null) {
            score += WITHIN_BUDGET_POINTS;
            reasons.add("Within your budget");
        }

        if (reference != null) {
            if (hotel.city().equalsIgnoreCase(reference.city())) {
                score += SAME_CITY_POINTS;
                reasons.add("Same city (" + reference.city() + ")");
            }

            if (hotel.category().equals(reference.category())) {
                score += SAME_CATEGORY_POINTS;
                reasons.add("Same category (" + reference.category() + ")");
            }
        }

        score += hotel.starRating() * POINTS_PER_STAR;

        if (hotel.starRating() >= 4) {
            reasons.add(hotel.starRating() + "-star rated");
        }

        if (hotel.availableRooms() != null &&
                hotel.availableRooms() >= PLENTY_OF_ROOMS_LIMIT) {
            score += PLENTY_OF_ROOMS_POINTS;
            reasons.add("Plenty of rooms left");
        }

        return new RecommendationResponse(hotel, score, reasons);
    }
}
