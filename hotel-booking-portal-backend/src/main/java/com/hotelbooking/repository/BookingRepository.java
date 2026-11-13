package com.hotelbooking.repository;

import com.hotelbooking.dto.BookedPeriod;
import com.hotelbooking.entity.Booking;
import com.hotelbooking.entity.BookingStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    boolean existsByBookingReference(String bookingReference);

    // Used for lookups: loads the booking together with its room and hotel.
    @Query("SELECT b FROM Booking b JOIN FETCH b.room r JOIN FETCH r.hotel "
            + "WHERE b.bookingReference = :reference")
    Optional<Booking> findByReferenceWithDetails(@Param("reference") String reference);

    // Used for cancellation: locks the booking row (SELECT ... FOR UPDATE) until the transaction ends.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Booking b WHERE b.bookingReference = :reference")
    Optional<Booking> findByReferenceForUpdate(@Param("reference") String reference);

    // Overlap condition: existingCheckIn < requestedCheckOut AND existingCheckOut > requestedCheckIn
    @Query("SELECT new com.hotelbooking.dto.BookedPeriod(b.room.id, b.checkIn, b.checkOut) "
            + "FROM Booking b "
            + "WHERE b.room.id = :roomId AND b.status = :status "
            + "AND b.checkIn < :checkOut AND b.checkOut > :checkIn")
    List<BookedPeriod> findBookedPeriodsForRoom(@Param("roomId") Long roomId,
                                                @Param("status") BookingStatus status,
                                                @Param("checkIn") LocalDate checkIn,
                                                @Param("checkOut") LocalDate checkOut);

    // Same condition for every room at once.
    @Query("SELECT new com.hotelbooking.dto.BookedPeriod(b.room.id, b.checkIn, b.checkOut) "
            + "FROM Booking b "
            + "WHERE b.status = :status "
            + "AND b.checkIn < :checkOut AND b.checkOut > :checkIn")
    List<BookedPeriod> findBookedPeriods(@Param("status") BookingStatus status,
                                         @Param("checkIn") LocalDate checkIn,
                                         @Param("checkOut") LocalDate checkOut);
}