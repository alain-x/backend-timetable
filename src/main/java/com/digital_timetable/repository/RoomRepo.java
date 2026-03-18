package com.digital_timetable.repository;

import com.digital_timetable.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoomRepo extends JpaRepository<Room, Long> {
    // Find rooms by minimum capacity
    List<Room> findByCapacityGreaterThanEqual(int minCapacity);
    
    // Find available rooms (not booked)
    List<Room> findByIsBookedFalse();
    
    // Find room by room name using custom query
    @Query("SELECT r FROM Room r WHERE r.room_name = :roomName")
    List<Room> findByRoomName(@Param("roomName") String roomName);
} 