package com.hotelbooking.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record BookingRequest(

        @NotNull(message = "Room id is required.")
        Long roomId,

        @NotBlank(message = "Customer name is required.")
        @Size(max = 100, message = "Customer name must be at most 100 characters.")
        String customerName,

        @NotBlank(message = "Customer email is required.")
        @Email(message = "Please enter a valid email address.")
        @Size(max = 150, message = "Email must be at most 150 characters.")
        String customerEmail,

        @NotNull(message = "Check-in date is required.")
        LocalDate checkIn,

        @NotNull(message = "Check-out date is required.")
        LocalDate checkOut
) {
}