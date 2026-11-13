package com.hotelbooking.repository;

import com.hotelbooking.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {

    // JOIN FETCH loads the hotel in the same query as the room.
    @Query("SELECT r FROM Room r JOIN FETCH r.hotel ORDER BY r.id")
    List<Room> findAllWithHotel();

    @Query("SELECT r FROM Room r JOIN FETCH r.hotel h WHERE h.id = :hotelId ORDER BY r.id")
    List<Room> findByHotelIdWithHotel(@Param("hotelId") Long hotelId);
}