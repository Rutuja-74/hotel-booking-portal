package com.hotelbooking.service;

import com.hotelbooking.dto.BookingRequest;
import com.hotelbooking.dto.BookingResponse;
import com.hotelbooking.entity.BookingStatus;
import com.hotelbooking.exception.BookingStateException;
import com.hotelbooking.exception.RoomNotAvailableException;
import com.hotelbooking.repository.BookingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Runs against a separate MySQL database (hotel_booking_test_db) that is reset before every test.
 * Skipped automatically when the DB_PASSWORD environment variable is not set.
 * Test data: room 1 has 1 room, room 2 has 3 rooms.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:mysql://localhost:3306/hotel_booking_test_db"
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata",
        "spring.jpa.hibernate.ddl-auto=none"
})
@Sql(scripts = {"/test-schema.sql", "/test-data.sql"})
@EnabledIfEnvironmentVariable(named = "DB_PASSWORD", matches = ".+")
class BookingIntegrationTest {

    private static final LocalDate BASE = LocalDate.now().plusDays(30);

    @Autowired
    private BookingService bookingService;

    @Autowired
    private AvailabilityService availabilityService;

    @Autowired
    private BookingRepository bookingRepository;

    private BookingRequest request(Long roomId, String email, LocalDate checkIn, LocalDate checkOut) {
        return new BookingRequest(roomId, "Test Guest", email, checkIn, checkOut);
    }

    @Test
    void backToBackStaysAreAllowed_butOverlappingStayIsRejected() {
        bookingService.createBooking(request(1L, "a@example.com", BASE, BASE.plusDays(3)));

        // Starts on the day the first stay checks out: not an overlap.
        BookingResponse second = bookingService.createBooking(
                request(1L, "b@example.com", BASE.plusDays(3), BASE.plusDays(5)));
        assertEquals(BookingStatus.CONFIRMED, second.status());

        // Overlaps both existing stays, and the room has only one unit.
        assertThrows(RoomNotAvailableException.class, () -> bookingService.createBooking(
                request(1L, "c@example.com", BASE.plusDays(2), BASE.plusDays(4))));

        assertEquals(2, bookingRepository.count());
    }

    @Test
    void cancellingReleasesTheRoom_andASecondCancelIsRejected() {
        BookingResponse booking = bookingService.createBooking(
                request(1L, "a@example.com", BASE, BASE.plusDays(2)));
        assertEquals(0, availabilityService.checkAvailability(1L, BASE, BASE.plusDays(2)).availableRooms());

        BookingResponse cancelled = bookingService.cancelBooking(booking.bookingReference(), "a@example.com");
        assertEquals(BookingStatus.CANCELLED, cancelled.status());
        assertEquals(1, availabilityService.checkAvailability(1L, BASE, BASE.plusDays(2)).availableRooms());

        assertThrows(BookingStateException.class,
                () -> bookingService.cancelBooking(booking.bookingReference(), "a@example.com"));

        // The released room can be booked again.
        BookingResponse rebooked = bookingService.createBooking(
                request(1L, "d@example.com", BASE, BASE.plusDays(2)));
        assertEquals(BookingStatus.CONFIRMED, rebooked.status());
    }

    @Test
    void simultaneousBookings_neverExceedRoomCapacity() throws Exception {
        // Room 1 has 1 room: eight users race for it, exactly one may win.
        assertEquals(1, runConcurrentBookings(1L, 8));
        // Room 2 has 3 rooms: eight users race, exactly three may win.
        assertEquals(3, runConcurrentBookings(2L, 8));

        assertEquals(4, bookingRepository.count());
    }

    private int runConcurrentBookings(Long roomId, int attempts) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(attempts);
        CountDownLatch startSignal = new CountDownLatch(1);
        List<Future<Boolean>> results = new ArrayList<>();

        for (int i = 0; i < attempts; i++) {
            String email = "guest" + i + "@example.com";
            results.add(pool.submit(() -> {
                startSignal.await(); // every thread waits here until we release them together
                try {
                    bookingService.createBooking(request(roomId, email, BASE, BASE.plusDays(2)));
                    return true;
                } catch (RoomNotAvailableException e) {
                    return false;
                }
            }));
        }

        startSignal.countDown(); // release all threads at the same moment

        int successes = 0;
        for (Future<Boolean> result : results) {
            if (result.get(60, TimeUnit.SECONDS)) {
                successes++;
            }
        }
        pool.shutdown();
        return successes;
    }
}