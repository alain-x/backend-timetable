package com.digital_timetable.repository;

import com.digital_timetable.entity.CourseCompletionRequest;
import com.digital_timetable.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseCompletionRequestRepository extends JpaRepository<CourseCompletionRequest, Long> {
    
    // Find all requests by lecturer
    List<CourseCompletionRequest> findByLecturerOrderByRequestDateDesc(User lecturer);
    
    // Find all requests by status
    List<CourseCompletionRequest> findByStatusOrderByRequestDateDesc(CourseCompletionRequest.RequestStatus status);
    
    // Find all requests (for admin view)
    List<CourseCompletionRequest> findAllByOrderByRequestDateDesc();
    
    // Find pending requests
    @Query("SELECT ccr FROM CourseCompletionRequest ccr WHERE ccr.status = 'PENDING' ORDER BY ccr.requestDate DESC")
    List<CourseCompletionRequest> findPendingRequests();
    
    // Count requests by status
    long countByStatus(CourseCompletionRequest.RequestStatus status);
    
    // Count requests by lecturer
    long countByLecturer(User lecturer);
}
