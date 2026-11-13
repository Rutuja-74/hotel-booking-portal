package com.hotelbooking.repository;

import com.hotelbooking.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {

    // Spring builds the query from the method name: rooms WHERE hotel.id = ?
    List<Room> findByHotelId(Long hotelId);
}