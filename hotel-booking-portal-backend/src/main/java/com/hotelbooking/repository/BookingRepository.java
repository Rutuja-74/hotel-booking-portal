package com.hotelbooking.repository;

import com.hotelbooking.dto.BookedPeriod;
import com.hotelbooking.entity.Booking;
import com.hotelbooking.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingReference(String bookingReference);

    boolean existsByBookingReference(String bookingReference);

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