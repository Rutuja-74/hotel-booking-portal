package com.hotelbooking.service;

import com.hotelbooking.dto.BookingRequest;
import com.hotelbooking.dto.BookingResponse;
import com.hotelbooking.entity.Booking;
import com.hotelbooking.entity.BookingStatus;
import com.hotelbooking.entity.Hotel;
import com.hotelbooking.entity.Room;
import com.hotelbooking.exception.ResourceNotFoundException;
import com.hotelbooking.exception.RoomNotAvailableException;
import com.hotelbooking.repository.BookingRepository;
import com.hotelbooking.repository.RoomRepository;
import com.hotelbooking.validation.StayDateValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class BookingService {

    private static final int MAX_REFERENCE_ATTEMPTS = 5;

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final AvailabilityService availabilityService;
    private final BookingReferenceGenerator referenceGenerator;

    public BookingService(RoomRepository roomRepository,
                          BookingRepository bookingRepository,
                          AvailabilityService availabilityService,
                          BookingReferenceGenerator referenceGenerator) {
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
        this.availabilityService = availabilityService;
        this.referenceGenerator = referenceGenerator;
    }

    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        LocalDate checkIn = request.checkIn();
        LocalDate checkOut = request.checkOut();

        // 1. Validate the dates (no database access yet).
        StayDateValidator.validate(checkIn, checkOut, LocalDate.now());

        // 2. Lock the room row. This MUST be the first database statement in this
        //    transaction. Any other booking for the same room now waits here until we finish.
        Room room = roomRepository.findByIdForUpdate(request.roomId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Room not found with id " + request.roomId()));

        // 3. Check availability while holding the lock.
        int availableRooms = availabilityService.countAvailableRooms(room, checkIn, checkOut);
        if (availableRooms <= 0) {
            throw new RoomNotAvailableException(
                    "No rooms are available for the selected dates. Please choose different dates.");
        }

        // 4. Calculate the price on the server. The client never sends a total.
        int nights = BookingCalculator.calculateNights(checkIn, checkOut);
        BigDecimal totalAmount = BookingCalculator.calculateTotal(room.getPricePerNight(), nights);

        // 5. Save the booking.
        Booking booking = new Booking();
        booking.setBookingReference(generateUniqueReference());
        booking.setRoom(room);
        booking.setCustomerName(request.customerName().trim());
        booking.setCustomerEmail(request.customerEmail().trim().toLowerCase());
        booking.setCheckIn(checkIn);
        booking.setCheckOut(checkOut);
        booking.setNights(nights);
        booking.setTotalAmount(totalAmount);
        booking.setStatus(BookingStatus.CONFIRMED);

        Booking saved = bookingRepository.save(booking);
        return toResponse(saved);
        // 6. When this method returns, the transaction commits and the room lock is released.
    }

    private String generateUniqueReference() {
        for (int attempt = 0; attempt < MAX_REFERENCE_ATTEMPTS; attempt++) {
            String reference = referenceGenerator.generate();
            if (!bookingRepository.existsByBookingReference(reference)) {
                return reference;
            }
        }
        throw new IllegalStateException("Could not generate a unique booking reference.");
    }

    // Also used by the retrieval and cancellation features in the next phase.
    public BookingResponse toResponse(Booking booking) {
        Room room = booking.getRoom();
        Hotel hotel = room.getHotel();
        return new BookingResponse(
                booking.getBookingReference(),
                booking.getStatus(),
                hotel.getId(),
                hotel.getName(),
                hotel.getCity(),
                room.getRoomType(),
                booking.getCustomerName(),
                booking.getCustomerEmail(),
                booking.getCheckIn(),
                booking.getCheckOut(),
                booking.getNights(),
                room.getPricePerNight(),
                booking.getTotalAmount(),
                booking.getCreatedAt(),
                booking.getCancelledAt()
        );
    }
}