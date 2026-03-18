package com.digital_timetable.controller;

import com.digital_timetable.dto.RoomDto;
import com.digital_timetable.dto.Response;
import com.digital_timetable.service.interf.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
// import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/rooms")
// @PreAuthorize("hasRole('ADMIN')")
public class RoomController {
    @Autowired
    private RoomService roomService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response> createRoom(@Valid @RequestBody RoomDto roomDto) {
        try {
            RoomDto createdRoom = roomService.createRoom(roomDto);
            Response response = Response.builder()
                    .status(HttpStatus.CREATED.value())
                    .message("Room created successfully")
                    .data(createdRoom)
                    .build();
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Response response = Response.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to create room: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/{roomId}/unbook")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response> unbookRoom(@PathVariable Long roomId) {
        try {
            boolean success = roomService.unbookRoom(roomId);
            if (!success) {
                Response bad = Response.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message("Unable to unbook room. It may already be available or not found.")
                        .build();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(bad);
            }
            Response response = Response.builder()
                    .status(HttpStatus.OK.value())
                    .message("Room marked as available successfully")
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Response response = Response.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to unbook room: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response> getRoom(@PathVariable Long id) {
        try {
            RoomDto room = roomService.getRoomById(id);
            if (room != null) {
                Response response = Response.builder()
                        .status(HttpStatus.OK.value())
                        .message("Room retrieved successfully")
                        .data(room)
                        .build();
                return ResponseEntity.ok(response);
            } else {
                Response response = Response.builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .message("Room not found")
                        .build();
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            Response response = Response.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to retrieve room: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('LECTURER', 'CLASS_REPRESENT', 'ADMIN', 'USER')")
    public ResponseEntity<Response> getAllRooms() {
        try {
            List<RoomDto> rooms = roomService.getAllRooms();
            Response response = Response.builder()
                    .status(HttpStatus.OK.value())
                    .message("Rooms retrieved successfully")
                    .data(rooms)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Response response = Response.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to retrieve rooms: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/available")
    public ResponseEntity<Response> getAvailableRooms(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String section) {
        try {
            List<RoomDto> availableRooms = roomService.getAvailableRooms(date, startTime, endTime, section);
            Response response = Response.builder()
                    .status(HttpStatus.OK.value())
                    .message("Available rooms retrieved successfully")
                    .data(availableRooms)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Response response = Response.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to retrieve available rooms: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{roomId}/schedule")
    public ResponseEntity<Response> getRoomSchedule(@PathVariable Long roomId) {
        try {
            List<Object> schedule = roomService.getRoomSchedule(roomId);
            Response response = Response.builder()
                    .status(HttpStatus.OK.value())
                    .message("Room schedule retrieved successfully")
                    .data(schedule)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Response response = Response.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to retrieve room schedule: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/{roomId}/book")
    @PreAuthorize("hasAnyRole('LECTURER','CLASS_REPRESENT','STAFF')")
    public ResponseEntity<Response> bookRoom(
            @PathVariable Long roomId,
            @RequestParam Long timetableId,
            @RequestParam(required = false) String notes) {
        try {
            RoomDto bookedRoom = roomService.bookRoom(roomId, timetableId, notes);
            if (bookedRoom == null) {
                Response bad = Response.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message("Unable to book room. It may already be booked or the timetable already has a room assigned.")
                        .build();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(bad);
            }
            Response response = Response.builder()
                    .status(HttpStatus.OK.value())
                    .message("Room booked successfully")
                    .data(bookedRoom)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Response response = Response.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to book room: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response> updateRoom(@PathVariable Long id, @Valid @RequestBody RoomDto roomDto) {
        try {
            RoomDto updatedRoom = roomService.updateRoom(id, roomDto);
            if (updatedRoom != null) {
                Response response = Response.builder()
                        .status(HttpStatus.OK.value())
                        .message("Room updated successfully")
                        .data(updatedRoom)
                        .build();
                return ResponseEntity.ok(response);
            } else {
                Response response = Response.builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .message("Room not found")
                        .build();
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            Response response = Response.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to update room: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response> deleteRoom(@PathVariable Long id) {
        try {
            roomService.deleteRoom(id);
            Response response = Response.builder()
                    .status(HttpStatus.OK.value())
                    .message("Room deleted successfully")
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Response response = Response.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to delete room: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
} 