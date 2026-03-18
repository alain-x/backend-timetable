package com.digital_timetable.controller;

import com.digital_timetable.dto.TimetableDto;
import com.digital_timetable.service.interf.TimetableService;
import com.digital_timetable.service.interf.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
// import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/staff")
// @PreAuthorize("hasRole('STAFF')")
public class StaffController {
    @Autowired
    private TimetableService timetableService;

    @Autowired
    private RoomService roomService;

    @GetMapping("/timetables")
    // @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<List<TimetableDto>> viewTimetables() {
        return ResponseEntity.ok(timetableService.getAllTimetables());
    }

    @PostMapping("/book-room")
    // @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<String> bookRoom(@RequestParam Long roomId, @RequestParam Long timetableId) {
        boolean success = roomService.bookRoom(roomId, timetableId);
        return success ? ResponseEntity.ok("Room booked successfully") : ResponseEntity.badRequest().body("Room booking failed");
    }
}
