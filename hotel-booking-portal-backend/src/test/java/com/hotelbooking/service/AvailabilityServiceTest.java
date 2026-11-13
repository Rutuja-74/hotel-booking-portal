package com.hotelbooking.service;

import com.hotelbooking.dto.BookedPeriod;
import com.hotelbooking.entity.BookingStatus;
import com.hotelbooking.entity.Room;
import com.hotelbooking.repository.BookingRepository;
import com.hotelbooking.repository.RoomRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceTest {

    private static final LocalDate CHECK_IN = LocalDate.of(2026, 12, 1);
    private static final LocalDate CHECK_OUT = LocalDate.of(2026, 12, 7);

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private AvailabilityService availabilityService;

    private Room roomWithCapacity(int totalRooms) {
        Room room = new Room();
        room.setId(10L);
        room.setTotalRooms(totalRooms);
        return room;
    }

    private BookedPeriod period(String checkIn, String checkOut) {
        return new BookedPeriod(10L, LocalDate.parse(checkIn), LocalDate.parse(checkOut));
    }

    private void givenBookings(BookedPeriod... periods) {
        when(bookingRepository.findBookedPeriodsForRoom(
                eq(10L), eq(BookingStatus.CONFIRMED), eq(CHECK_IN), eq(CHECK_OUT)))
                .thenReturn(List.of(periods));
    }

    @Test
    void noBookings_allRoomsAreAvailable() {
        givenBookings();

        assertEquals(5, availabilityService.countAvailableRooms(roomWithCapacity(5), CHECK_IN, CHECK_OUT));
    }

    @Test
    void fullyBooked_noRoomIsAvailable() {
        givenBookings(
                period("2026-12-01", "2026-12-07"),
                period("2026-12-02", "2026-12-06"));

        assertEquals(0, availabilityService.countAvailableRooms(roomWithCapacity(2), CHECK_IN, CHECK_OUT));
    }

    @Test
    void sequentialStays_leaveOneRoomFree() {
        givenBookings(
                period("2026-12-01", "2026-12-03"),
                period("2026-12-03", "2026-12-05"),
                period("2026-12-05", "2026-12-07"));

        assertEquals(1, availabilityService.countAvailableRooms(roomWithCapacity(2), CHECK_IN, CHECK_OUT));
    }

    @Test
    void availability_neverGoesBelowZero() {
        givenBookings(
                period("2026-12-01", "2026-12-07"),
                period("2026-12-01", "2026-12-07"));

        assertEquals(0, availabilityService.countAvailableRooms(roomWithCapacity(1), CHECK_IN, CHECK_OUT));
    }
}