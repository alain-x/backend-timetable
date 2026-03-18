package com.digital_timetable.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "course_completion_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseCompletionRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecturer_id", nullable = false)
    private User lecturer;
    
    @Column(name = "course_id", nullable = false)
    private Long courseId;
    
    @Column(name = "course_name", nullable = false)
    private String courseName;
    
    @Column(name = "course_code", nullable = false)
    private String courseCode;
    
    @Column(name = "lecturer_notes", columnDefinition = "TEXT")
    private String lecturerNotes;
    
    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RequestStatus status = RequestStatus.PENDING;
    
    @Column(name = "request_date", nullable = false)
    private LocalDateTime requestDate;
    
    @Column(name = "reviewed_date")
    private LocalDateTime reviewedDate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intake_id")
    private Intake intake;
    
    public enum RequestStatus {
        PENDING,
        APPROVED,
        REJECTED
    }
    
    @PrePersist
    protected void onCreate() {
        if (requestDate == null) {
            requestDate = LocalDateTime.now();
        }
        if (status == null) {
            status = RequestStatus.PENDING;
        }
    }
}
