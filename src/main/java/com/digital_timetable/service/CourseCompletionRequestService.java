package com.digital_timetable.service;

import com.digital_timetable.entity.CourseCompletionRequest;
import com.digital_timetable.entity.Course;
import com.digital_timetable.entity.Intake;
import com.digital_timetable.entity.IntakeCourseCompletion;
import com.digital_timetable.entity.Timetable;
import com.digital_timetable.entity.User;
import com.digital_timetable.enums.IntakeStatus;
import com.digital_timetable.repository.CourseCompletionRequestRepository;
import com.digital_timetable.repository.CourseRepo;
import com.digital_timetable.repository.IntakeCourseCompletionRepository;
import com.digital_timetable.repository.IntakeRepository;
import com.digital_timetable.repository.TimetableRepo;
import com.digital_timetable.service.interf.TimetableService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseCompletionRequestService {
    
    private final CourseCompletionRequestRepository courseCompletionRequestRepository;

    private final CourseRepo courseRepo;

    private final IntakeRepository intakeRepository;

    private final IntakeCourseCompletionRepository intakeCourseCompletionRepository;

    private final TimetableRepo timetableRepo;

    private final TimetableService timetableService;
    
    /**
     * Submit a new course completion request from lecturer
     */
    public CourseCompletionRequest submitRequest(User lecturer, Long courseId, String courseName, 
                                               String courseCode, String lecturerNotes) {
        return submitRequest(lecturer, courseId, courseName, courseCode, lecturerNotes, null);
    }

    public CourseCompletionRequest submitRequest(User lecturer, Long courseId, String courseName,
                                               String courseCode, String lecturerNotes, Long intakeId) {
        CourseCompletionRequest request = new CourseCompletionRequest();
        request.setLecturer(lecturer);
        request.setCourseId(courseId);
        request.setCourseName(courseName);
        request.setCourseCode(courseCode);
        request.setLecturerNotes(lecturerNotes);
        request.setStatus(CourseCompletionRequest.RequestStatus.PENDING);
        request.setRequestDate(LocalDateTime.now());

        if (intakeId != null) {
            try {
                Intake intake = intakeRepository.findById(intakeId).orElse(null);
                request.setIntake(intake);
            } catch (Exception ignored) {}
        }
        
        return courseCompletionRequestRepository.save(request);
    }
    
    /**
     * Get all requests for admin view
     */
    @Transactional(readOnly = true)
    public List<CourseCompletionRequest> getAllRequests() {
        return courseCompletionRequestRepository.findAllByOrderByRequestDateDesc();
    }
    
    /**
     * Get requests by lecturer
     */
    @Transactional(readOnly = true)
    public List<CourseCompletionRequest> getRequestsByLecturer(User lecturer) {
        return courseCompletionRequestRepository.findByLecturerOrderByRequestDateDesc(lecturer);
    }
    
    /**
     * Get pending requests
     */
    @Transactional(readOnly = true)
    public List<CourseCompletionRequest> getPendingRequests() {
        return courseCompletionRequestRepository.findPendingRequests();
    }
    
    /**
     * Approve a request
     */
    public CourseCompletionRequest approveRequest(Long requestId, User admin, String adminNotes) {
        Optional<CourseCompletionRequest> requestOpt = courseCompletionRequestRepository.findById(requestId);
        if (requestOpt.isPresent()) {
            CourseCompletionRequest request = requestOpt.get();
            request.setStatus(CourseCompletionRequest.RequestStatus.APPROVED);
            request.setAdminNotes(adminNotes);
            request.setReviewedBy(admin);
            request.setReviewedDate(LocalDateTime.now());

            CourseCompletionRequest savedRequest = courseCompletionRequestRepository.save(request);

            Intake intake = savedRequest.getIntake();
            if (intake != null) {
                Long courseId = savedRequest.getCourseId();
                if (courseId != null) {
                    Course course = courseRepo.findById(courseId)
                            .orElseThrow(() -> new RuntimeException("Course not found with ID: " + courseId));

                    // Remove timetables for this completed course (only within the same intake)
                    try {
                        List<Timetable> timetablesToDelete = timetableRepo.findByCourse_IdAndIntake_Id(course.getId(), intake.getId());
                        if (timetablesToDelete != null && !timetablesToDelete.isEmpty()) {
                            for (Timetable t : timetablesToDelete) {
                                if (t != null && t.getId() != null) {
                                    try {
                                        timetableService.endClass(t.getId());
                                    } catch (Exception ignored) {}
                                    timetableService.deleteTimetable(t.getId());
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to auto-delete timetables for completed course " + course.getId() + " in intake " + intake.getId() + ": " + e.getMessage());
                    }

                    Optional<IntakeCourseCompletion> existingOpt =
                            intakeCourseCompletionRepository.findByIntake_IdAndCourse_Id(intake.getId(), course.getId());

                    IntakeCourseCompletion completion = existingOpt.orElseGet(IntakeCourseCompletion::new);
                    completion.setIntake(intake);
                    completion.setCourse(course);
                    completion.setMarkedByAdmin(admin);
                    completion.setCompletedAt(LocalDateTime.now());
                    if (adminNotes != null && !adminNotes.isBlank()) {
                        completion.setNotes(adminNotes);
                    } else if (savedRequest.getLecturerNotes() != null) {
                        completion.setNotes(savedRequest.getLecturerNotes());
                    }

                    intakeCourseCompletionRepository.save(completion);

                    // Update intake status: COMPLETED when all courses assigned to this intake are completed
                    try {
                        List<Course> intakeCourses = courseRepo.findByIntake_Id(intake.getId());
                        if (intakeCourses != null && !intakeCourses.isEmpty()) {
                            List<IntakeCourseCompletion> intakeCompletions = intakeCourseCompletionRepository.findByIntake_Id(intake.getId());
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
                    } catch (Exception e) {
                        System.err.println("Failed to update intake status for intake " + intake.getId() + ": " + e.getMessage());
                    }
                }
            }

            return savedRequest;
        }
        throw new RuntimeException("Course completion request not found with ID: " + requestId);
    }
    
    /**
     * Reject a request
     */
    public CourseCompletionRequest rejectRequest(Long requestId, User admin, String adminNotes) {
        Optional<CourseCompletionRequest> requestOpt = courseCompletionRequestRepository.findById(requestId);
        if (requestOpt.isPresent()) {
            CourseCompletionRequest request = requestOpt.get();
            request.setStatus(CourseCompletionRequest.RequestStatus.REJECTED);
            request.setAdminNotes(adminNotes);
            request.setReviewedBy(admin);
            request.setReviewedDate(LocalDateTime.now());
            return courseCompletionRequestRepository.save(request);
        }
        throw new RuntimeException("Course completion request not found with ID: " + requestId);
    }
    
    /**
     * Get request by ID
     */
    @Transactional(readOnly = true)
    public Optional<CourseCompletionRequest> getRequestById(Long requestId) {
        return courseCompletionRequestRepository.findById(requestId);
    }

    /**
     * Delete request by ID
     */
    public void deleteRequest(Long requestId) {
        if (courseCompletionRequestRepository.existsById(requestId)) {
            courseCompletionRequestRepository.deleteById(requestId);
        } else {
            throw new RuntimeException("Course completion request not found with ID: " + requestId);
        }
    }

    /**
     * Get statistics
     */
    @Transactional(readOnly = true)
    public long countByStatus(CourseCompletionRequest.RequestStatus status) {
        return courseCompletionRequestRepository.countByStatus(status);
    }
    
    @Transactional(readOnly = true)
    public long countByLecturer(User lecturer) {
        return courseCompletionRequestRepository.countByLecturer(lecturer);
    }
}
