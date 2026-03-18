package com.digital_timetable.controller;

import com.digital_timetable.dto.TimetableDto;
import com.digital_timetable.service.interf.TimetableService;
import com.digital_timetable.service.interf.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/classrepresent")
@PreAuthorize("hasRole('CLASS_REPRESENT')")
public class ClassRepresentController {
    @Autowired
    private TimetableService timetableService;

    @Autowired
    private RoomService roomService;

    @GetMapping("/timetables")
    public ResponseEntity<List<TimetableDto>> viewTimetables() {
        return ResponseEntity.ok(timetableService.getAllTimetables());
    }

    @PostMapping("/book-room")
    public ResponseEntity<String> bookRoom(@RequestParam Long roomId, @RequestParam Long timetableId) {
        boolean success = roomService.bookRoom(roomId, timetableId);
        return success ? ResponseEntity.ok("Room booked successfully") : ResponseEntity.badRequest().body("Room booking failed");
    }
}
