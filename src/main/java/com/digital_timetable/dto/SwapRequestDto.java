package com.digital_timetable.dto;

import java.time.LocalDateTime;

public class SwapRequestDto {
    private Long id;
    private Long requestorId;
    private String requestorName;
    private Long targetUserId;
    private String targetUserName;
    private Long originalTimetableId;
    private String originalTimetableInfo;
    private Long proposedTimetableId;
    private String proposedTimetableInfo;
    private String status;
    private String reason;
    private String adminNotes;
    private String requestDate;
    private String responseDate;
    private String createdAt;

    // Constructors
    public SwapRequestDto() {}

    public SwapRequestDto(Long id, Long requestorId, String requestorName, Long targetUserId, String targetUserName,
                         Long originalTimetableId, String originalTimetableInfo, Long proposedTimetableId, String proposedTimetableInfo,
                         String status, String reason, String adminNotes, String requestDate, String responseDate, String createdAt) {
        this.id = id;
        this.requestorId = requestorId;
        this.requestorName = requestorName;
        this.targetUserId = targetUserId;
        this.targetUserName = targetUserName;
        this.originalTimetableId = originalTimetableId;
        this.originalTimetableInfo = originalTimetableInfo;
        this.proposedTimetableId = proposedTimetableId;
        this.proposedTimetableInfo = proposedTimetableInfo;
        this.status = status;
        this.reason = reason;
        this.adminNotes = adminNotes;
        this.requestDate = requestDate;
        this.responseDate = responseDate;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRequestorId() { return requestorId; }
    public void setRequestorId(Long requestorId) { this.requestorId = requestorId; }

    public String getRequestorName() { return requestorName; }
    public void setRequestorName(String requestorName) { this.requestorName = requestorName; }

    public Long getTargetUserId() { return targetUserId; }
    public void setTargetUserId(Long targetUserId) { this.targetUserId = targetUserId; }

    public String getTargetUserName() { return targetUserName; }
    public void setTargetUserName(String targetUserName) { this.targetUserName = targetUserName; }

    public Long getOriginalTimetableId() { return originalTimetableId; }
    public void setOriginalTimetableId(Long originalTimetableId) { this.originalTimetableId = originalTimetableId; }

    public String getOriginalTimetableInfo() { return originalTimetableInfo; }
    public void setOriginalTimetableInfo(String originalTimetableInfo) { this.originalTimetableInfo = originalTimetableInfo; }

    public Long getProposedTimetableId() { return proposedTimetableId; }
    public void setProposedTimetableId(Long proposedTimetableId) { this.proposedTimetableId = proposedTimetableId; }

    public String getProposedTimetableInfo() { return proposedTimetableInfo; }
    public void setProposedTimetableInfo(String proposedTimetableInfo) { this.proposedTimetableInfo = proposedTimetableInfo; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getAdminNotes() { return adminNotes; }
    public void setAdminNotes(String adminNotes) { this.adminNotes = adminNotes; }

    public String getRequestDate() { return requestDate; }
    public void setRequestDate(String requestDate) { this.requestDate = requestDate; }

    public String getResponseDate() { return responseDate; }
    public void setResponseDate(String responseDate) { this.responseDate = responseDate; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
} 