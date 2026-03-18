package com.digital_timetable.controller;

import com.digital_timetable.dto.TimetableDto;
import com.digital_timetable.dto.Response;
import com.digital_timetable.service.interf.TimetableService;
import com.digital_timetable.service.interf.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/timetables")
public class TimetableController {
    
    @Autowired
    private TimetableService timetableService;
    
    @Autowired
    private NotificationService notificationService;

    @PostMapping
    public ResponseEntity<Response> createTimetable(@Valid @RequestBody TimetableDto timetableDto) {
        try {
            TimetableDto createdTimetable = timetableService.createTimetable(timetableDto);
            
            Response response = Response.builder()
                    .status(HttpStatus.CREATED.value())
                    .message("Timetable created successfully")
                    .data(createdTimetable)
                    .build();
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Response response = Response.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to create timetable: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> getTimetable(@PathVariable Long id) {
        try {
            TimetableDto timetable = timetableService.getTimetableById(id);
            if (timetable != null) {
                Response response = Response.builder()
                        .status(HttpStatus.OK.value())
                        .message("Timetable retrieved successfully")
                        .data(timetable)
                        .build();
                return ResponseEntity.ok(response);
            } else {
                Response response = Response.builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .message("Timetable not found")
                        .build();
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            Response response = Response.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to retrieve timetable: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping
    public ResponseEntity<Response> getAllTimetables() {
        try {
            List<TimetableDto> timetables = timetableService.getAllTimetables();
            Response response = Response.builder()
                    .status(HttpStatus.OK.value())
                    .message("Timetables retrieved successfully")
                    .data(timetables)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Response response = Response.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to retrieve timetables: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/lecturer/{lecturerId}")
    public ResponseEntity<Response> getTimetablesByLecturer(@PathVariable Long lecturerId) {
        try {
            List<TimetableDto> timetables = timetableService.getTimetablesByLecturer(lecturerId);
            Response response = Response.builder()
                    .status(HttpStatus.OK.value())
                    .message("Lecturer timetables retrieved successfully")
                    .data(timetables)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Response response = Response.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to retrieve lecturer timetables: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Response> updateTimetable(@PathVariable Long id, @Valid @RequestBody TimetableDto timetableDto) {
        try {
            TimetableDto updatedTimetable = timetableService.updateTimetable(id, timetableDto);
            if (updatedTimetable != null) {
                Response response = Response.builder()
                        .status(HttpStatus.OK.value())
                        .message("Timetable updated successfully")
                        .data(updatedTimetable)
                        .build();
                return ResponseEntity.ok(response);
            } else {
                Response response = Response.builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .message("Timetable not found")
                        .build();
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            Response response = Response.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to update timetable: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Response> deleteTimetable(@PathVariable Long id) {
        try {
            timetableService.deleteTimetable(id);
            
            Response response = Response.builder()
                    .status(HttpStatus.OK.value())
                    .message("Timetable deleted successfully")
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Response response = Response.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to delete timetable: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/{id}/end-class")
    public ResponseEntity<Response> endClass(@PathVariable Long id) {
        try {
            TimetableDto endedTimetable = timetableService.endClass(id);
            if (endedTimetable != null) {
                Response response = Response.builder()
                        .status(HttpStatus.OK.value())
                        .message("Class ended successfully")
                        .data(endedTimetable)
                        .build();
                return ResponseEntity.ok(response);
            } else {
                Response response = Response.builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .message("Timetable not found")
                        .build();
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            Response response = Response.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to end class: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PutMapping("/{id}/start-class")
    public ResponseEntity<Response> startClass(@PathVariable Long id) {
        try {
            TimetableDto startedTimetable = timetableService.startClass(id);
            if (startedTimetable != null) {
                Response response = Response.builder()
                        .status(HttpStatus.OK.value())
                        .message("Class started successfully")
                        .data(startedTimetable)
                        .build();
                return ResponseEntity.ok(response);
            } else {
                Response response = Response.builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .message("Timetable not found")
                        .build();
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            Response response = Response.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to start class: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/{timetableId}/assign-classrep/{userId}")
    public ResponseEntity<Response> assignClassRep(@PathVariable Long timetableId, @PathVariable Long userId) {
        try {
            TimetableDto updatedTimetable = timetableService.assignClassRep(timetableId, userId);
            if (updatedTimetable != null) {
                Response response = Response.builder()
                        .status(HttpStatus.OK.value())
                        .message("Class representative assigned successfully")
                        .data(updatedTimetable)
                        .build();
                return ResponseEntity.ok(response);
            } else {
                Response response = Response.builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .message("Timetable not found")
                        .build();
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            Response response = Response.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to assign class representative: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
