package com.hotelbooking.exception;

// The booking exists, but its current state doesn't allow the action (e.g. already cancelled).
public class BookingStateException extends RuntimeException {

    public BookingStateException(String message) {
        super(message);
    }
}