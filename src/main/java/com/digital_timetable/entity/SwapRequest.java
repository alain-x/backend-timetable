package com.digital_timetable.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "swap_requests")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SwapRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requestor_id", nullable = false)
    private User requestor; // Lecturer or Class Rep who initiated the request

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_user_id", nullable = false)
    private User targetUser; // Lecturer or Class Rep to swap with

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_timetable_id", nullable = false)
    private Timetable originalTimetable; // Original class slot

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposed_timetable_id", nullable = false)
    private Timetable proposedTimetable; // Proposed new slot

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SwapStatus status = SwapStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String reason; // Reason for the swap request

    @Column(columnDefinition = "TEXT")
    private String adminNotes; // Admin notes for approval/rejection

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime requestDate = LocalDateTime.now();

    private LocalDateTime responseDate;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum SwapStatus {
        PENDING,    // Waiting for target user response
        APPROVED,   // Target user approved
        REJECTED,   // Target user rejected
        ADMIN_APPROVED, // Admin approved the swap
        ADMIN_REJECTED, // Admin rejected the swap
        CANCELLED,  // Requestor cancelled
        EXPIRED     // Request expired
    }
} 