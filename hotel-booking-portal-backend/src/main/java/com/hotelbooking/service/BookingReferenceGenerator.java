package com.hotelbooking.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class BookingReferenceGenerator {

    private static final String CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int RANDOM_PART_LENGTH = 6;

    private final SecureRandom random = new SecureRandom();

    public String generate() {
        StringBuilder reference = new StringBuilder("HB-");
        reference.append(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));
        reference.append('-');
        for (int i = 0; i < RANDOM_PART_LENGTH; i++) {
            reference.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return reference.toString();
    }
}