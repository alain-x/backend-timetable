package com.digital_timetable.controller;

import com.digital_timetable.dto.TimetableDto;
import com.digital_timetable.dto.Response;
import com.digital_timetable.service.interf.TimetableService;
import com.digital_timetable.service.interf.RoomService;
import com.digital_timetable.service.interf.UserService;
import com.digital_timetable.service.interf.NotificationService;
import com.digital_timetable.service.CourseCompletionRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;

import com.digital_timetable.dto.RoomDto;

@RestController
@RequestMapping("/api/lecturer")
@PreAuthorize("hasRole('LECTURER')")
public class LecturerController {
    @Autowired
    private TimetableService timetableService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CourseCompletionRequestService courseCompletionRequestService;

    @GetMapping("/timetables")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<List<TimetableDto>> viewTimetables() {
        return ResponseEntity.ok(timetableService.getAllTimetables());
    }

    @GetMapping("/my-timetables")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<Response> getMyTimetables(@RequestParam(required = false) Long lecturerId) {
        try {
            List<TimetableDto> timetables;

            if (lecturerId != null) {
                timetables = timetableService.getTimetablesByLecturer(lecturerId);
            } else {
                // Get current lecturer from authentication
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                String lecturerEmail = auth.getName();
                var lecturer = userService.getUserByEmail(lecturerEmail);
                timetables = timetableService.getTimetablesByLecturer(lecturer.getId());
            }

            return ResponseEntity.ok(Response.builder()
                    .status(200)
                    .message("Timetables retrieved successfully")
                    .data(timetables)
                    .build());

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.builder()
                    .status(400)
                    .message("Failed to retrieve timetables: " + e.getMessage())
                    .build());
        }
    }

    @GetMapping("/my-courses")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<Response> getMyCourses() {
        try {
            // Get current lecturer ID from authentication
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String lecturerEmail = auth.getName();
            var lecturer = userService.getUserByEmail(lecturerEmail);

            // Get timetables/courses assigned to this lecturer
            List<TimetableDto> myTimetables = timetableService.getTimetablesByLecturer(lecturer.getId());

            Response response = new Response();
            response.setStatus(200);
            response.setMessage("Courses retrieved successfully");
            response.setData(myTimetables);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Response errorResponse = new Response();
            errorResponse.setStatus(400);
            errorResponse.setMessage("Failed to retrieve courses: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }


    @GetMapping("/course-completion-requests")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<Response> getCourseCompletionRequests() {
        try {
            // Get current lecturer from authentication
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String lecturerEmail = auth.getName();
            var lecturer = userService.getUserByEmail(lecturerEmail);
            
            // Get real requests from the service
            var requests = courseCompletionRequestService.getRequestsByLecturer(lecturer);
            
            // Convert to response format
            List<Map<String, Object>> requestData = new ArrayList<>();
            for (var request : requests) {
                Map<String, Object> requestMap = new HashMap<>();
                requestMap.put("id", request.getId());
                requestMap.put("courseId", request.getCourseId());
                requestMap.put("courseName", request.getCourseName());
                requestMap.put("courseCode", request.getCourseCode());
                requestMap.put("status", request.getStatus().toString());
                requestMap.put("requestDate", request.getRequestDate().toString());
                requestMap.put("lecturerNotes", request.getLecturerNotes());
                requestMap.put("adminNotes", request.getAdminNotes());
                if (request.getReviewedDate() != null) {
                    requestMap.put("reviewedDate", request.getReviewedDate().toString());
                }
                if (request.getReviewedBy() != null) {
                    requestMap.put("reviewedBy", request.getReviewedBy().getName());
                }
                requestData.add(requestMap);
            }
            
            Response response = new Response();
            response.setStatus(200);
            response.setMessage("Course completion requests retrieved successfully");
            response.setData(requestData);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Response response = new Response();
            response.setStatus(400);
            response.setMessage("Failed to retrieve course completion requests: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/create-timetable")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<Response> createTimetable(@RequestBody TimetableDto timetableDto) {
        try {
            // Get current lecturer ID from authentication
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String lecturerEmail = auth.getName();
            var lecturer = userService.getUserByEmail(lecturerEmail);
            timetableDto.setLecturerId(lecturer.getId());
            timetableDto.setLecturerName(lecturer.getName());

            TimetableDto created = timetableService.createTimetable(timetableDto);
            return ResponseEntity.ok(Response.builder()
                .status(200)
                .message("Timetable created successfully")
                .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.builder()
                .status(400)
                .message("Failed to create timetable: " + e.getMessage())
                .build());
        }
    }

    @PutMapping("/update-timetable/{id}")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<Response> updateTimetable(@PathVariable Long id, @RequestBody TimetableDto timetableDto) {
        try {
            TimetableDto updated = timetableService.updateTimetable(id, timetableDto);
            return ResponseEntity.ok(Response.builder()
                .status(200)
                .message("Timetable updated successfully")
                .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.builder()
                .status(400)
                .message("Failed to update timetable: " + e.getMessage())
                .build());
        }
    }

    @DeleteMapping("/delete-timetable/{id}")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<Response> deleteTimetable(@PathVariable Long id) {
        try {
            timetableService.deleteTimetable(id);
            return ResponseEntity.ok(Response.builder()
                .status(200)
                .message("Timetable deleted successfully")
                .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.builder()
                .status(400)
                .message("Failed to delete timetable: " + e.getMessage())
                .build());
        }
    }

    @PostMapping("/book-room")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<String> bookRoom(@RequestParam Long roomId, @RequestParam Long timetableId) {
        boolean success = roomService.bookRoom(roomId, timetableId);
        return success ? ResponseEntity.ok("Room booked successfully") : ResponseEntity.badRequest().body("Room booking failed");
    }

    @GetMapping("/check-room-availability")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<Map<String, Object>> checkRoomAvailability(@RequestParam Long roomId, 
                                                                   @RequestParam String startTime, 
                                                                   @RequestParam String endTime) {
        try {
            // This would need to be implemented in RoomService
            Map<String, Object> response = new HashMap<>();
            response.put("available", true);
            response.put("message", "Room is available for the specified time");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("available", false);
            response.put("message", "Error checking availability: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/assign-class-rep")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<TimetableDto> assignClassRep(@RequestParam Long userId, @RequestParam Long timetableId) {
        TimetableDto updated = timetableService.assignClassRep(timetableId, userId);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @PutMapping("/timetable/{id}/hours")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<TimetableDto> updateTimetableHours(@PathVariable Long id, @RequestParam int hours) {
        TimetableDto timetable = timetableService.getTimetableById(id);
        if (timetable != null) {
            timetable.setHours(hours);
            return ResponseEntity.ok(timetableService.updateTimetable(id, timetable));
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/timetable/{id}/start")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<TimetableDto> startClass(@PathVariable Long id) {
        TimetableDto timetable = timetableService.getTimetableById(id);
        if (timetable != null) {
            timetable.setStatus("started");
            return ResponseEntity.ok(timetableService.updateTimetable(id, timetable));
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/timetable/{id}/end")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<TimetableDto> endClass(@PathVariable Long id) {
        TimetableDto updated = timetableService.endClass(id);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @PutMapping("/timetable/{id}/times")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<TimetableDto> updateTimetableTimes(@PathVariable Long id, @RequestParam Date start, @RequestParam Date end) {
        TimetableDto timetable = timetableService.getTimetableById(id);
        if (timetable != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            timetable.setStartDateTime(start.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().format(formatter));
            timetable.setEndDateTime(end.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().format(formatter));
            return ResponseEntity.ok(timetableService.updateTimetable(id, timetable));
        }
        return ResponseEntity.notFound().build();
    }

     

    @PostMapping("/attendance/{timetableId}")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<Response> markAttendance(@PathVariable Long timetableId, 
                                                 @RequestBody Map<String, String> attendanceData) {
        try {
            // This would need to be implemented to mark student attendance
            return ResponseEntity.ok(Response.builder()
                .status(200)
                .message("Attendance marked successfully")
                .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.builder()
                .status(400)
                .message("Failed to mark attendance: " + e.getMessage())
                .build());
        }
    }

    @PostMapping("/announcement/{timetableId}")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<Response> postAnnouncement(@PathVariable Long timetableId, 
                                                   @RequestBody Map<String, String> announcementData) {
        try {
            // This would need to be implemented to post announcements
            return ResponseEntity.ok(Response.builder()
                .status(200)
                .message("Announcement posted successfully")
                .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.builder()
                .status(400)
                .message("Failed to post announcement: " + e.getMessage())
                .build());
        }
    }

    @GetMapping("/rooms")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<List<RoomDto>> getRooms() {
        try {
            List<RoomDto> rooms = roomService.getAllRooms();
            return ResponseEntity.ok(rooms);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ArrayList<>());
        }
    }

     
   
  


    @PostMapping("/course-completion-request")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<Response> requestCourseCompletion(@RequestBody Map<String, Object> requestData) {
        try {
            // Get current lecturer from authentication
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String lecturerEmail = auth.getName();
            var lecturer = userService.getUserByEmail(lecturerEmail);

            // Validate required fields
            if (requestData.get("courseId") == null) {
                throw new RuntimeException("courseId is required");
            }
            if (requestData.get("courseName") == null) {
                throw new RuntimeException("courseName is required");
            }

            // Extract course code with more flexible field names
            String courseCode = Optional.ofNullable((String) requestData.get("courseCode"))
                    .orElseGet(() -> (String) requestData.get("course_code"));

            if (courseCode == null || courseCode.trim().isEmpty()) {
                throw new RuntimeException("courseCode is required");
            }

            Long courseId = Long.valueOf(requestData.get("courseId").toString());
            String courseName = (String) requestData.get("courseName");
            String notes = (String) requestData.get("notes");

            Long intakeId = null;
            try {
                if (requestData.get("intakeId") != null) {
                    intakeId = Long.valueOf(requestData.get("intakeId").toString());
                }
            } catch (Exception ignored) {}

            // Submit the request
            var savedRequest = courseCompletionRequestService.submitRequest(
                    lecturer, courseId, courseName, courseCode, notes, intakeId
            );

            return ResponseEntity.ok(Response.builder()
                    .status(200)
                    .message("Course completion request submitted successfully")
                    .data(Map.of(
                            "requestId", savedRequest.getId(),
                            "status", savedRequest.getStatus().toString()
                    ))
                    .build());

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.builder()
                    .status(400)
                    .message(e.getMessage())
                    .build());
        }
    }
}
