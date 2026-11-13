package com.hotelbooking.controller;

import com.hotelbooking.dto.AvailabilityResponse;
import com.hotelbooking.service.AvailabilityService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final AvailabilityService availabilityService;

    public RoomController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    // GET /api/rooms/availability?roomId=1&checkIn=2026-12-10&checkOut=2026-12-13
    @GetMapping("/availability")
    public AvailabilityResponse checkAvailability(
            @RequestParam Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {
        return availabilityService.checkAvailability(roomId, checkIn, checkOut);
    }
}