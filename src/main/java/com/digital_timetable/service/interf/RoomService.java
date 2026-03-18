package com.digital_timetable.service.interf;

import com.digital_timetable.dto.RoomDto;
import java.util.List;

public interface RoomService {
    RoomDto createRoom(RoomDto roomDto);
    RoomDto getRoomById(Long id);
    List<RoomDto> getAllRooms();
    RoomDto updateRoom(Long id, RoomDto roomDto);
    void deleteRoom(Long id);
    boolean bookRoom(Long roomId, Long timetableId);
    boolean unbookRoom(Long roomId);
    
    // New methods for flexible room scheduling
    List<RoomDto> getAvailableRooms(String date, String startTime, String endTime, String section);
    List<Object> getRoomSchedule(Long roomId);
    RoomDto bookRoom(Long roomId, Long timetableId, String notes);
    boolean checkRoomAvailability(Long roomId, String date, String startTime, String endTime);
    List<RoomDto> getRoomsByCapacity(int minCapacity);
    List<RoomDto> getRoomsBySection(String section);
} 