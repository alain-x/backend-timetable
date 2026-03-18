package com.digital_timetable.service.impl;

import com.digital_timetable.dto.RoomDto;
import com.digital_timetable.entity.Room;
import com.digital_timetable.entity.Timetable;
import com.digital_timetable.mapper.EntityDtoMapper;
import com.digital_timetable.repository.RoomRepo;
import com.digital_timetable.repository.TimetableRepo;
import com.digital_timetable.service.interf.NotificationService;
import com.digital_timetable.service.interf.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RoomServiceImpl implements RoomService {
    @Autowired
    private RoomRepo roomRepo;
    @Autowired
    private TimetableRepo timetableRepo;
    @Autowired
    private EntityDtoMapper mapper;
    @Autowired
    private NotificationService notificationService;

    @Override
    public RoomDto createRoom(RoomDto roomDto) {
        Room room = mapper.mapDtoToRoom(roomDto);
        Room saved = roomRepo.save(room);
        return mapper.mapRoomToDto(saved);
    }

    @Override
    public RoomDto getRoomById(Long id) {
        Optional<Room> room = roomRepo.findById(id);
        return room.map(mapper::mapRoomToDto).orElse(null);
    }

    @Override
    public List<RoomDto> getAllRooms() {
        return roomRepo.findAll().stream().map(mapper::mapRoomToDto).collect(Collectors.toList());
    }

    @Override
    public RoomDto updateRoom(Long id, RoomDto roomDto) {
        Optional<Room> optional = roomRepo.findById(id);
        if (optional.isPresent()) {
            Room room = optional.get();
            room.setRoom_name(roomDto.getRoom_name());
            return mapper.mapRoomToDto(roomRepo.save(room));
        }
        return null;
    }

    @Override
    public void deleteRoom(Long id) {
        roomRepo.deleteById(id);
    }

    @Override
    public boolean bookRoom(Long roomId, Long timetableId) {
        try {
            Optional<Room> roomOpt = roomRepo.findById(roomId);
            if (!roomOpt.isPresent()) {
                return false; // Room not found
            }
            
            Room room = roomOpt.get();
            
            // Check if room is available
            if (room.isBooked()) {
                return false; // Room is already booked
            }
            
            // Update room status to booked
            room.setBooked(true);
            roomRepo.save(room);
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean unbookRoom(Long roomId) {
        try {
            Optional<Room> roomOpt = roomRepo.findById(roomId);
            if (!roomOpt.isPresent()) {
                return false; // Room not found
            }
            
            Room room = roomOpt.get();
            
            // Check if room is booked
            if (!room.isBooked()) {
                return false; // Room is not booked
            }
            
            // Detach room from any current/future timetables for consistency
            List<Timetable> timetablesWithRoom = timetableRepo.findByRoom(room);
            LocalDateTime now = LocalDateTime.now();
            for (Timetable t : timetablesWithRoom) {
                if (t.getEndDateTime() == null || !t.getEndDateTime().isBefore(now)) {
                    t.setRoom(null);
                    // Optionally clear display name if used
                    t.setRoom_name(null);
                }
            }
            if (!timetablesWithRoom.isEmpty()) {
                timetableRepo.saveAll(timetablesWithRoom);
            }

            // Update room status to available
            room.setBooked(false);
            roomRepo.save(room);
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<RoomDto> getAvailableRooms(String date, String startTime, String endTime, String section) {
        try {
            LocalDate targetDate = date != null ? LocalDate.parse(date) : LocalDate.now();
            LocalTime start = startTime != null ? LocalTime.parse(startTime) : LocalTime.of(8, 0);
            LocalTime end = endTime != null ? LocalTime.parse(endTime) : LocalTime.of(18, 0);
            
            // Get all rooms
            List<Room> allRooms = roomRepo.findAll();
            
            // Filter rooms based on availability and section
            return allRooms.stream()
                .filter(room -> {
                    // Check if room is not booked
                    if (room.isBooked()) {
                        return false;
                    }
                    
                    // Check for time conflicts
                    LocalDateTime startDateTime = LocalDateTime.of(targetDate, start);
                    LocalDateTime endDateTime = LocalDateTime.of(targetDate, end);
                    
                    // Check if room has any conflicting timetables
                    List<Timetable> conflictingTimetables = timetableRepo.findByRoomAndDateTimeRange(
                        room, startDateTime, endDateTime);
                    
                    return conflictingTimetables.isEmpty();
                })
                .map(mapper::mapRoomToDto)
                .collect(Collectors.toList());
        } catch (Exception e) {
            return List.of();
        }
    }

    @Override
    public List<Object> getRoomSchedule(Long roomId) {
        try {
            Optional<Room> roomOpt = roomRepo.findById(roomId);
            if (!roomOpt.isPresent()) {
                return List.of();
            }
            
            Room room = roomOpt.get();
            List<Timetable> timetables = timetableRepo.findByRoom(room);
            
            return timetables.stream()
                .map(timetable -> {
                    return Map.of(
                        "id", timetable.getId(),
                        "course_name", timetable.getCourse_name(),
                        "lecture_name", timetable.getLecture_name(),
                        "lecturer_name", timetable.getLecturer() != null ? timetable.getLecturer().getName() : "N/A",
                        "startDateTime", timetable.getStartDateTime(),
                        "endDateTime", timetable.getEndDateTime(),
                        "section", timetable.getSection(),
                        "status", timetable.getStatus()
                    );
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            return List.of();
        }
    }

    @Override
    public RoomDto bookRoom(Long roomId, Long timetableId, String notes) {
        try {
            Optional<Room> roomOpt = roomRepo.findById(roomId);
            Optional<Timetable> timetableOpt = timetableRepo.findById(timetableId);
            
            if (!roomOpt.isPresent() || !timetableOpt.isPresent()) {
                return null;
            }
            
            Room room = roomOpt.get();
            Timetable timetable = timetableOpt.get();
            
            // Enforce single-room booking per timetable: if a room is already assigned, do not allow booking another
            if (timetable.getRoom() != null) {
                return null;
            }
            
            // Check availability
            if (!checkRoomAvailability(roomId, 
                timetable.getStartDateTime().toLocalDate().toString(),
                timetable.getStartDateTime().toLocalTime().toString(),
                timetable.getEndDateTime().toLocalTime().toString())) {
                return null;
            }
            
            // Update timetable with room
            timetable.setRoom(room);
            timetableRepo.save(timetable);
            
            // Update room status
            room.setBooked(true);
            Room savedRoom = roomRepo.save(room);
            
            // Notify relevant users about the booking
            try {
                // Get timetable details for notification
                if (timetable != null) {
                    String title = "Room Booked";
                    String message = "Room '" + room.getRoom_name() + "' has been booked for timetable '" + timetable.getCourse_name() + "' (" + timetable.getStartDateTime() + ")";
                    // Notify lecturer
                    if (timetable.getLecturer() != null) {
                        notificationService.createRequestNotification(title, message, timetable.getLecturer().getId());
                    }
                    // Notify class rep
                    if (timetable.getClassRep() != null) {
                        notificationService.createRequestNotification(title, message, timetable.getClassRep().getId());
                    }
                    // TODO: Notify students when student-timetable relationship is available
                    // Currently, only lecturer and class rep are notified.
                }
            } catch (Exception notifyEx) {
                // Log but do not fail booking
                System.err.println("Notification error: " + notifyEx.getMessage());
            }
            return mapper.mapRoomToDto(savedRoom);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean checkRoomAvailability(Long roomId, String date, String startTime, String endTime) {
        try {
            LocalDate targetDate = LocalDate.parse(date);
            LocalTime start = LocalTime.parse(startTime);
            LocalTime end = LocalTime.parse(endTime);
            
            LocalDateTime startDateTime = LocalDateTime.of(targetDate, start);
            LocalDateTime endDateTime = LocalDateTime.of(targetDate, end);
            
            Optional<Room> roomOpt = roomRepo.findById(roomId);
            if (!roomOpt.isPresent()) {
                return false;
            }
            
            Room room = roomOpt.get();
            
            // Check for conflicts
            List<Timetable> conflictingTimetables = timetableRepo.findByRoomAndDateTimeRange(
                room, startDateTime, endDateTime);
            
            return conflictingTimetables.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<RoomDto> getRoomsByCapacity(int minCapacity) {
        return roomRepo.findByCapacityGreaterThanEqual(minCapacity)
            .stream()
            .map(mapper::mapRoomToDto)
            .collect(Collectors.toList());
    }

    @Override
    public List<RoomDto> getRoomsBySection(String section) {
        // This method can be used to get rooms that are typically used for specific sections
        // For now, return all rooms as they can be used for any section
        return getAllRooms();
    }
} 