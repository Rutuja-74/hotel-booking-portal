package com.hotelbooking.service;

import com.hotelbooking.dto.AvailabilityResponse;
import com.hotelbooking.dto.BookedPeriod;
import com.hotelbooking.entity.BookingStatus;
import com.hotelbooking.entity.Hotel;
import com.hotelbooking.entity.Room;
import com.hotelbooking.exception.ResourceNotFoundException;
import com.hotelbooking.repository.BookingRepository;
import com.hotelbooking.repository.RoomRepository;
import com.hotelbooking.validation.StayDateValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AvailabilityService {

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;

    public AvailabilityService(RoomRepository roomRepository, BookingRepository bookingRepository) {
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
    }

    // Used by GET /api/rooms/availability (also gives the price preview).
    @Transactional(readOnly = true)
    public AvailabilityResponse checkAvailability(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        StayDateValidator.validate(checkIn, checkOut, LocalDate.now());

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id " + roomId));

        int availableRooms = countAvailableRooms(room, checkIn, checkOut);
        int nights = BookingCalculator.calculateNights(checkIn, checkOut);
        BigDecimal totalAmount = BookingCalculator.calculateTotal(room.getPricePerNight(), nights);
        Hotel hotel = room.getHotel();

        return new AvailabilityResponse(
                room.getId(),
                hotel.getId(),
                hotel.getName(),
                room.getRoomType(),
                checkIn,
                checkOut,
                nights,
                room.getPricePerNight(),
                room.getTotalRooms(),
                availableRooms,
                availableRooms > 0,
                totalAmount
        );
    }

    // Not a transactional method itself: callers (search, booking) already run inside a transaction.
    public int countAvailableRooms(Room room, LocalDate checkIn, LocalDate checkOut) {
        List<BookedPeriod> booked = bookingRepository.findBookedPeriodsForRoom(
                room.getId(), BookingStatus.CONFIRMED, checkIn, checkOut);
        int occupied = BookingCalculator.maxRoomsOccupied(checkIn, checkOut, booked);
        return Math.max(0, room.getTotalRooms() - occupied);
    }

    // One database query for all rooms (used by the search page).
    public Map<Long, Integer> countAvailableRoomsForAll(List<Room> rooms,
                                                        LocalDate checkIn, LocalDate checkOut) {
        Map<Long, List<BookedPeriod>> bookedByRoom = bookingRepository
                .findBookedPeriods(BookingStatus.CONFIRMED, checkIn, checkOut)
                .stream()
                .collect(Collectors.groupingBy(BookedPeriod::roomId));

        Map<Long, Integer> availableByRoom = new HashMap<>();
        for (Room room : rooms) {
            List<BookedPeriod> booked = bookedByRoom.getOrDefault(room.getId(), List.of());
            int occupied = BookingCalculator.maxRoomsOccupied(checkIn, checkOut, booked);
            availableByRoom.put(room.getId(), Math.max(0, room.getTotalRooms() - occupied));
        }
        return availableByRoom;
    }
}