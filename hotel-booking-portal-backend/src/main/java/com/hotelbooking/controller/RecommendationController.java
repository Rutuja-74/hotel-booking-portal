
package com.hotelbooking.controller;

import com.hotelbooking.dto.RecommendationResponse;
import com.hotelbooking.service.RecommendationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    // GET /api/recommendations?city=Goa&checkIn=2026-12-10&checkOut=2026-12-12&budget=3000

    @GetMapping
    public List<RecommendationResponse> getRecommendations(
            @RequestParam String city,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate checkIn,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate checkOut,

            @RequestParam(required = false)
            BigDecimal budget,

            @RequestParam(required = false)
            Long hotelId) {

        return recommendationService.recommend(
                city, checkIn, checkOut, budget, hotelId);
    }
}
