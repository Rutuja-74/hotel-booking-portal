package com.hotelbooking.service;

import com.hotelbooking.dto.BookingRequest;
import com.hotelbooking.dto.BookingResponse;
import com.hotelbooking.entity.Booking;
import com.hotelbooking.entity.BookingStatus;
import com.hotelbooking.entity.Hotel;
import com.hotelbooking.entity.Room;
import com.hotelbooking.exception.BadRequestException;
import com.hotelbooking.exception.BookingStateException;
import com.hotelbooking.exception.ResourceNotFoundException;
import com.hotelbooking.exception.RoomNotAvailableException;
import com.hotelbooking.repository.BookingRepository;
import com.hotelbooking.repository.RoomRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private AvailabilityService availabilityService;

    @Mock
    private BookingReferenceGenerator referenceGenerator;

    @InjectMocks
    private BookingService bookingService;

    // ---------- helpers ----------

    private Room buildRoom(int totalRooms, String price) {
        Hotel hotel = new Hotel();
        hotel.setId(1L);
        hotel.setName("Test Hotel");
        hotel.setCity("Goa");

        Room room = new Room();
        room.setId(10L);
        room.setHotel(hotel);
        room.setRoomType("Deluxe Room");
        room.setPricePerNight(new BigDecimal(price));
        room.setTotalRooms(totalRooms);
        room.setMaxGuests(2);
        return room;
    }

    private Booking buildBooking(BookingStatus status, LocalDate checkIn) {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setBookingReference("HB-TEST-0001");
        booking.setRoom(buildRoom(2, "2000.00"));
        booking.setCustomerName("Asha Patil");
        booking.setCustomerEmail("asha@example.com");
        booking.setCheckIn(checkIn);
        booking.setCheckOut(checkIn.plusDays(2));
        booking.setNights(2);
        booking.setTotalAmount(new BigDecimal("4000.00"));
        booking.setStatus(status);
        return booking;
    }

    // ---------- create booking ----------

    @Test
    void createBooking_calculatesTotalOnServerAndNormalisesInput() {
        LocalDate checkIn = LocalDate.now().plusDays(10);
        LocalDate checkOut = checkIn.plusDays(3);
        Room room = buildRoom(5, "2500.00");

        when(roomRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(room));
        when(availabilityService.countAvailableRooms(room, checkIn, checkOut)).thenReturn(2);
        when(referenceGenerator.generate()).thenReturn("HB-20261113-ABC234");
        when(bookingRepository.existsByBookingReference("HB-20261113-ABC234")).thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingRequest request = new BookingRequest(10L, "  Asha Patil ", "Asha@Example.COM", checkIn, checkOut);
        BookingResponse response = bookingService.createBooking(request);

        assertEquals("HB-20261113-ABC234", response.bookingReference());
        assertEquals(BookingStatus.CONFIRMED, response.status());
        assertEquals(3, response.nights());
        assertEquals(new BigDecimal("7500.00"), response.totalAmount());

        ArgumentCaptor<Booking> saved = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(saved.capture());
        assertEquals("Asha Patil", saved.getValue().getCustomerName());
        assertEquals("asha@example.com", saved.getValue().getCustomerEmail());
    }

    @Test
    void createBooking_whenNoRoomsLeft_isRejectedAndNothingIsSaved() {
        LocalDate checkIn = LocalDate.now().plusDays(10);
        LocalDate checkOut = checkIn.plusDays(2);
        Room room = buildRoom(1, "2500.00");

        when(roomRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(room));
        when(availabilityService.countAvailableRooms(room, checkIn, checkOut)).thenReturn(0);

        BookingRequest request = new BookingRequest(10L, "Asha", "asha@example.com", checkIn, checkOut);

        assertThrows(RoomNotAvailableException.class, () -> bookingService.createBooking(request));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_withPastCheckIn_isRejectedBeforeAnyDatabaseAccess() {
        LocalDate checkIn = LocalDate.now().minusDays(1);
        BookingRequest request = new BookingRequest(
                10L, "Asha", "asha@example.com", checkIn, checkIn.plusDays(2));

        assertThrows(BadRequestException.class, () -> bookingService.createBooking(request));
        verifyNoInteractions(roomRepository);
    }

    @Test
    void createBooking_unknownRoom_isNotFound() {
        LocalDate checkIn = LocalDate.now().plusDays(10);
        when(roomRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        BookingRequest request = new BookingRequest(
                99L, "Asha", "asha@example.com", checkIn, checkIn.plusDays(2));

        assertThrows(ResourceNotFoundException.class, () -> bookingService.createBooking(request));
    }

    // ---------- retrieve booking ----------

    @Test
    void getBooking_withMatchingEmailIgnoringCase_returnsBooking() {
        Booking booking = buildBooking(BookingStatus.CONFIRMED, LocalDate.now().plusDays(5));
        when(bookingRepository.findByReferenceWithDetails("HB-TEST-0001")).thenReturn(Optional.of(booking));

        BookingResponse response = bookingService.getBooking(" hb-test-0001 ", "ASHA@example.com");

        assertEquals("HB-TEST-0001", response.bookingReference());
        assertEquals(BookingStatus.CONFIRMED, response.status());
    }

    @Test
    void getBooking_withWrongEmail_isNotFound() {
        Booking booking = buildBooking(BookingStatus.CONFIRMED, LocalDate.now().plusDays(5));
        when(bookingRepository.findByReferenceWithDetails("HB-TEST-0001")).thenReturn(Optional.of(booking));

        assertThrows(ResourceNotFoundException.class,
                () -> bookingService.getBooking("HB-TEST-0001", "someone.else@example.com"));
    }

    // ---------- cancel booking ----------

    @Test
    void cancelBooking_confirmedBooking_becomesCancelled() {
        Booking booking = buildBooking(BookingStatus.CONFIRMED, LocalDate.now().plusDays(5));
        when(bookingRepository.findByReferenceForUpdate("HB-TEST-0001")).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingResponse response = bookingService.cancelBooking("hb-test-0001", "asha@example.com");

        assertEquals(BookingStatus.CANCELLED, response.status());
        assertNotNull(response.cancelledAt());
        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
    }

    @Test
    void cancelBooking_alreadyCancelled_isRejected() {
        Booking booking = buildBooking(BookingStatus.CANCELLED, LocalDate.now().plusDays(5));
        when(bookingRepository.findByReferenceForUpdate("HB-TEST-0001")).thenReturn(Optional.of(booking));

        BookingStateException ex = assertThrows(BookingStateException.class,
                () -> bookingService.cancelBooking("HB-TEST-0001", "asha@example.com"));

        assertTrue(ex.getMessage().contains("already been cancelled"));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void cancelBooking_wrongEmail_isNotFoundAndBookingIsUnchanged() {
        Booking booking = buildBooking(BookingStatus.CONFIRMED, LocalDate.now().plusDays(5));
        when(bookingRepository.findByReferenceForUpdate("HB-TEST-0001")).thenReturn(Optional.of(booking));

        assertThrows(ResourceNotFoundException.class,
                () -> bookingService.cancelBooking("HB-TEST-0001", "wrong@example.com"));

        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void cancelBooking_afterCheckInDate_isRejected() {
        Booking booking = buildBooking(BookingStatus.CONFIRMED, LocalDate.now().minusDays(1));
        when(bookingRepository.findByReferenceForUpdate("HB-TEST-0001")).thenReturn(Optional.of(booking));

        BookingStateException ex = assertThrows(BookingStateException.class,
                () -> bookingService.cancelBooking("HB-TEST-0001", "asha@example.com"));

        assertTrue(ex.getMessage().contains("no longer be cancelled"));
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
    }
}