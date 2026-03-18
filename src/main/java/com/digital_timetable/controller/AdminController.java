package com.digital_timetable.controller;

import com.digital_timetable.dto.IntakeDto;
import com.digital_timetable.entity.Course;
import com.digital_timetable.entity.Department;
import com.digital_timetable.entity.Intake;
import com.digital_timetable.entity.IntakeCourseCompletion;
import com.digital_timetable.entity.Timetable;
import com.digital_timetable.enums.IntakeStatus;
import com.digital_timetable.repository.CourseRepo;
import com.digital_timetable.repository.DepartmentRepo;
import com.digital_timetable.repository.IntakeCourseCompletionRepository;
import com.digital_timetable.repository.IntakeRepository;
import com.digital_timetable.repository.TimetableRepo;
import com.digital_timetable.service.interf.AdminService;
import com.digital_timetable.service.interf.UserService;
import com.digital_timetable.service.CourseCompletionRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final CourseCompletionRequestService courseCompletionRequestService;
    private final UserService userService;

    private final IntakeRepository intakeRepository;
    private final IntakeCourseCompletionRepository intakeCourseCompletionRepository;
    private final CourseRepo courseRepo;
    private final DepartmentRepo departmentRepo;
    private final TimetableRepo timetableRepo;



    @PostMapping("/activate/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> activateAccount(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            adminService.activateAccount(userId);
            response.put("status", "SUCCESS");
            response.put("message", "Account activated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PutMapping("/intakes/{intakeId}/course-completions/{courseId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> setIntakeCourseCompletion(
            @PathVariable Long intakeId,
            @PathVariable Long courseId,
            @RequestBody Map<String, Object> body) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean completed = false;
            Object raw = body != null ? body.get("completed") : null;
            if (raw instanceof Boolean) {
                completed = (Boolean) raw;
            } else if (raw instanceof String) {
                completed = Boolean.parseBoolean(((String) raw).trim());
            }

            Intake intake = intakeRepository.findById(intakeId)
                    .orElseThrow(() -> new IllegalArgumentException("Intake not found"));
            Course course = courseRepo.findById(courseId)
                    .orElseThrow(() -> new IllegalArgumentException("Course not found"));

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String adminEmail = auth != null ? auth.getName() : null;
            var admin = adminEmail != null ? userService.getUserByEmail(adminEmail) : null;
            if (admin == null) {
                throw new IllegalStateException("Admin user not found");
            }

            var existingOpt = intakeCourseCompletionRepository.findByIntake_IdAndCourse_Id(intakeId, courseId);
            if (completed) {
                IntakeCourseCompletion completion = existingOpt.orElseGet(IntakeCourseCompletion::new);
                completion.setIntake(intake);
                completion.setCourse(course);
                completion.setMarkedByAdmin(admin);
                completion.setCompletedAt(LocalDateTime.now());
                Object notesRaw = body != null ? body.get("notes") : null;
                if (notesRaw instanceof String && !((String) notesRaw).trim().isEmpty()) {
                    completion.setNotes(((String) notesRaw).trim());
                }
                intakeCourseCompletionRepository.save(completion);

                // Hide timetables for this intake+course
                try {
                    List<Timetable> timetables = timetableRepo.findByCourse_IdAndIntake_Id(courseId, intakeId);
                    if (timetables != null) {
                        for (Timetable t : timetables) {
                            if (t == null) continue;
                            t.setStatus("completed");
                        }
                        timetableRepo.saveAll(timetables);
                    }
                } catch (Exception ignored) {}
            } else {
                // Un-complete: remove completion record and show timetables again
                existingOpt.ifPresent(intakeCourseCompletionRepository::delete);

                try {
                    List<Timetable> timetables = timetableRepo.findByCourse_IdAndIntake_Id(courseId, intakeId);
                    if (timetables != null) {
                        for (Timetable t : timetables) {
                            if (t == null) continue;
                            String s = t.getStatus();
                            if (s != null && s.equalsIgnoreCase("completed")) {
                                t.setStatus("scheduled");
                            }
                        }
                        timetableRepo.saveAll(timetables);
                    }
                } catch (Exception ignored) {}
            }

            // Update intake status
            try {
                List<Course> intakeCourses = courseRepo.findByIntake_Id(intakeId);
                if (intakeCourses != null && !intakeCourses.isEmpty()) {
                    List<IntakeCourseCompletion> intakeCompletions = intakeCourseCompletionRepository.findByIntake_Id(intakeId);
                    long completedCount = intakeCompletions == null ? 0 : intakeCompletions.stream()
                            .filter(c -> c.getCourse() != null && c.getCourse().getId() != null)
                            .map(c -> c.getCourse().getId())
                            .distinct()
                            .count();
                    long totalCount = intakeCourses.stream()
                            .filter(c -> c.getId() != null)
                            .map(Course::getId)
                            .distinct()
                            .count();

                    if (totalCount > 0 && completedCount >= totalCount) {
                        intake.setStatus(IntakeStatus.COMPLETED);
                    } else {
                        intake.setStatus(IntakeStatus.ONGOING);
                    }
                } else {
                    intake.setStatus(IntakeStatus.ONGOING);
                }
                intakeRepository.save(intake);
            } catch (Exception ignored) {}

            response.put("status", "SUCCESS");
            response.put("message", "Completion updated");
            response.put("intakeId", intakeId);
            response.put("courseId", courseId);
            response.put("completed", completed);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // =============== Intake management ===============

    @PostMapping("/intakes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createIntake(@RequestBody IntakeDto intakeDto) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (intakeDto.getName() == null || intakeDto.getName().isBlank()) {
                throw new IllegalArgumentException("Intake name is required");
            }
            if (intakeDto.getIntakeCode() == null || intakeDto.getIntakeCode().isBlank()) {
                throw new IllegalArgumentException("Intake code is required");
            }
            if (intakeDto.getProgramName() == null || intakeDto.getProgramName().isBlank()) {
                throw new IllegalArgumentException("Program name is required");
            }
            if (intakeDto.getStudyMode() == null || intakeDto.getStudyMode().isBlank()) {
                throw new IllegalArgumentException("Study mode is required");
            }
            String mode = intakeDto.getStudyMode().trim().toUpperCase();
            if (!(mode.equals("DAY") || mode.equals("EVENING") || mode.equals("WEEKEND"))) {
                throw new IllegalArgumentException("Study mode must be one of: DAY, EVENING, WEEKEND");
            }
            if (intakeDto.getCampus() == null || intakeDto.getCampus().isBlank()) {
                throw new IllegalArgumentException("Campus is required");
            }
            if (intakeDto.getDepartmentId() == null) {
                throw new IllegalArgumentException("Department is required");
            }

            Department department = departmentRepo.findById(intakeDto.getDepartmentId())
                    .orElseThrow(() -> new IllegalArgumentException("Department not found"));

            Intake intake = new Intake();
            intake.setName(intakeDto.getName());
            intake.setIntakeCode(intakeDto.getIntakeCode());
            intake.setProgramName(intakeDto.getProgramName());
            intake.setStudyMode(mode);
            intake.setCampus(intakeDto.getCampus());
            intake.setDepartment(department);

            if (intakeDto.getStartDate() != null && !intakeDto.getStartDate().isBlank()) {
                intake.setStartDate(java.time.LocalDate.parse(intakeDto.getStartDate()));
            }
            if (intakeDto.getExpectedEndDate() != null && !intakeDto.getExpectedEndDate().isBlank()) {
                intake.setExpectedEndDate(java.time.LocalDate.parse(intakeDto.getExpectedEndDate()));
            }

            Intake saved = intakeRepository.save(intake);

            IntakeDto result = new IntakeDto();
            result.setId(saved.getId());
            result.setName(saved.getName());
            result.setIntakeCode(saved.getIntakeCode());
            result.setProgramName(saved.getProgramName());
            result.setStudyMode(saved.getStudyMode());
            result.setCampus(saved.getCampus());
            result.setDepartmentId(saved.getDepartment() != null ? saved.getDepartment().getId() : null);
            result.setStatus(saved.getStatus() != null ? saved.getStatus().name() : null);
            result.setStartDate(saved.getStartDate() != null ? saved.getStartDate().toString() : null);
            result.setExpectedEndDate(saved.getExpectedEndDate() != null ? saved.getExpectedEndDate().toString() : null);

            response.put("status", "SUCCESS");
            response.put("message", "Intake created successfully");
            response.put("data", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PutMapping("/intakes/{intakeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateIntake(
            @PathVariable Long intakeId,
            @RequestBody IntakeDto intakeDto) {
        Map<String, Object> response = new HashMap<>();
        try {
            Intake intake = intakeRepository.findById(intakeId)
                    .orElseThrow(() -> new IllegalArgumentException("Intake not found"));

            if (intakeDto.getName() != null && !intakeDto.getName().isBlank()) {
                intake.setName(intakeDto.getName());
            }
            if (intakeDto.getIntakeCode() != null && !intakeDto.getIntakeCode().isBlank()) {
                intake.setIntakeCode(intakeDto.getIntakeCode());
            }
            if (intakeDto.getProgramName() != null && !intakeDto.getProgramName().isBlank()) {
                intake.setProgramName(intakeDto.getProgramName());
            }
            if (intakeDto.getStudyMode() != null && !intakeDto.getStudyMode().isBlank()) {
                String mode = intakeDto.getStudyMode().trim().toUpperCase();
                if (!(mode.equals("DAY") || mode.equals("EVENING") || mode.equals("WEEKEND"))) {
                    throw new IllegalArgumentException("Study mode must be one of: DAY, EVENING, WEEKEND");
                }
                intake.setStudyMode(mode);
            }
            if (intakeDto.getCampus() != null && !intakeDto.getCampus().isBlank()) {
                intake.setCampus(intakeDto.getCampus());
            }
            if (intakeDto.getDepartmentId() != null) {
                Department department = departmentRepo.findById(intakeDto.getDepartmentId())
                        .orElseThrow(() -> new IllegalArgumentException("Department not found"));
                intake.setDepartment(department);
            }
            if (intakeDto.getStartDate() != null) {
                if (intakeDto.getStartDate().isBlank()) {
                    intake.setStartDate(null);
                } else {
                    intake.setStartDate(java.time.LocalDate.parse(intakeDto.getStartDate()));
                }
            }
            if (intakeDto.getExpectedEndDate() != null) {
                if (intakeDto.getExpectedEndDate().isBlank()) {
                    intake.setExpectedEndDate(null);
                } else {
                    intake.setExpectedEndDate(java.time.LocalDate.parse(intakeDto.getExpectedEndDate()));
                }
            }

            Intake saved = intakeRepository.save(intake);

            IntakeDto result = new IntakeDto();
            result.setId(saved.getId());
            result.setName(saved.getName());
            result.setIntakeCode(saved.getIntakeCode());
            result.setProgramName(saved.getProgramName());
            result.setStudyMode(saved.getStudyMode());
            result.setCampus(saved.getCampus());
            result.setDepartmentId(saved.getDepartment() != null ? saved.getDepartment().getId() : null);
            result.setStatus(saved.getStatus() != null ? saved.getStatus().name() : null);
            result.setStartDate(saved.getStartDate() != null ? saved.getStartDate().toString() : null);
            result.setExpectedEndDate(saved.getExpectedEndDate() != null ? saved.getExpectedEndDate().toString() : null);

            response.put("status", "SUCCESS");
            response.put("message", "Intake updated successfully");
            response.put("data", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @DeleteMapping("/intakes/{intakeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteIntake(@PathVariable Long intakeId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Intake intake = intakeRepository.findById(intakeId)
                    .orElseThrow(() -> new IllegalArgumentException("Intake not found"));

            if (!intakeCourseCompletionRepository.findByIntake_Id(intakeId).isEmpty()) {
                throw new IllegalStateException("Cannot delete intake with existing course completion records");
            }

            intakeRepository.delete(intake);

            response.put("status", "SUCCESS");
            response.put("message", "Intake deleted successfully");
            response.put("intakeId", intakeId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/intakes/{intakeId}/course-completions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getIntakeCourseCompletions(@PathVariable Long intakeId) {
        Map<String, Object> response = new HashMap<>();
        try {
            intakeRepository.findById(intakeId)
                    .orElseThrow(() -> new IllegalArgumentException("Intake not found"));

            java.util.Set<Long> courseIds = new java.util.LinkedHashSet<>();
            try {
                List<Timetable> intakeTimetables = timetableRepo.findByIntake_Id(intakeId);
                if (intakeTimetables != null) {
                    for (Timetable t : intakeTimetables) {
                        if (t != null && t.getCourse() != null && t.getCourse().getId() != null) {
                            courseIds.add(t.getCourse().getId());
                        }
                    }
                }
            } catch (Exception ignored) {}

            try {
                List<Course> assigned = courseRepo.findByIntake_Id(intakeId);
                if (assigned != null) {
                    for (Course c : assigned) {
                        if (c != null && c.getId() != null) {
                            courseIds.add(c.getId());
                        }
                    }
                }
            } catch (Exception ignored) {}

            List<Course> courses = courseIds.isEmpty() ? java.util.Collections.emptyList() : courseRepo.findAllById(courseIds);
            List<IntakeCourseCompletion> completions = intakeCourseCompletionRepository.findByIntake_Id(intakeId);

            List<Map<String, Object>> courseData = new ArrayList<>();
            for (Course course : courses) {
                Map<String, Object> m = new HashMap<>();
                m.put("courseId", course.getId());
                m.put("courseName", course.getCourse_name());
                m.put("courseCode", course.getCourse_code());

                IntakeCourseCompletion completion = null;
                if (completions != null) {
                    for (IntakeCourseCompletion c : completions) {
                        if (c != null && c.getCourse() != null && course.getId() != null && course.getId().equals(c.getCourse().getId())) {
                            completion = c;
                            break;
                        }
                    }
                }

                m.put("completed", completion != null);
                if (completion != null) {
                    m.put("completedAt", completion.getCompletedAt() != null ? completion.getCompletedAt().toString() : null);
                    m.put("notes", completion.getNotes());
                }
                courseData.add(m);
            }

            response.put("status", "SUCCESS");
            response.put("message", "Intake course completions retrieved successfully");
            response.put("data", courseData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    @PostMapping("/deactivate/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deactivateAccount(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            adminService.deactivateAccount(userId);
            response.put("status", "SUCCESS");
            response.put("message", "Account deactivated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // Delete Account - Returns OK or BAD_REQUEST
    @PostMapping("/delete/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteAccount(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            adminService.deleteUserAccount(userId);
            response.put("status", "SUCCESS");
            response.put("message", "Account deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // Course Completion Request Management
    @GetMapping("/course-completion-requests")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllCourseCompletionRequests() {
        Map<String, Object> response = new HashMap<>();
        try {
            // Get real requests from the service
            var requests = courseCompletionRequestService.getAllRequests();
            
            // Convert to response format
            List<Map<String, Object>> requestData = new ArrayList<>();
            for (var request : requests) {
                Map<String, Object> requestMap = new HashMap<>();
                requestMap.put("id", request.getId());
                requestMap.put("lecturerName", request.getLecturer().getName());
                requestMap.put("lecturerEmail", request.getLecturer().getEmail());
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
            
            response.put("status", "SUCCESS");
            response.put("message", "Course completion requests retrieved successfully");
            response.put("data", requestData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/course-completion-requests/{requestId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> approveCourseCompletion(
            @PathVariable Long requestId, 
            @RequestBody Map<String, Object> requestData) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Get current admin from authentication
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String adminEmail = auth.getName();
            var admin = userService.getUserByEmail(adminEmail);
            
            String adminNotes = (String) requestData.get("adminNotes");
            
            // Approve the request using the service
            var approvedRequest = courseCompletionRequestService.approveRequest(requestId, admin, adminNotes);
            
            response.put("status", "SUCCESS");
            response.put("message", "Course completion request approved successfully");
            response.put("requestId", approvedRequest.getId());
            response.put("adminNotes", approvedRequest.getAdminNotes());
            response.put("reviewedDate", approvedRequest.getReviewedDate().toString());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/course-completion-requests/{requestId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> rejectCourseCompletion(
            @PathVariable Long requestId, 
            @RequestBody Map<String, Object> requestData) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Get current admin from authentication
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String adminEmail = auth.getName();
            var admin = userService.getUserByEmail(adminEmail);
            
            String adminNotes = (String) requestData.get("adminNotes");
            
            // Reject the request using the service
            var rejectedRequest = courseCompletionRequestService.rejectRequest(requestId, admin, adminNotes);
            
            response.put("status", "SUCCESS");
            response.put("message", "Course completion request rejected");
            response.put("requestId", rejectedRequest.getId());
            response.put("adminNotes", rejectedRequest.getAdminNotes());
            response.put("reviewedDate", rejectedRequest.getReviewedDate().toString());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @DeleteMapping("/course-completion-requests/{requestId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteCourseCompletionRequest(@PathVariable Long requestId) {
        Map<String, Object> response = new HashMap<>();
        try {
            courseCompletionRequestService.deleteRequest(requestId);
            response.put("status", "SUCCESS");
            response.put("message", "Course completion request deleted successfully");
            response.put("requestId", requestId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}