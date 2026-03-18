package com.digital_timetable.dto;

public class NotificationDto {
    private Long id;
    private String type;
    private String title;
    private String message;
    private String timestamp;
    private boolean read;
    private Long userId;
    private Long roomId;
    private Long timetableId;

    // Constructors
    public NotificationDto() {}

    public NotificationDto(Long id, String type, String title, String message, String timestamp, boolean read, Long userId, Long roomId, Long timetableId) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
        this.read = read;
        this.userId = userId;
        this.roomId = roomId;
        this.timetableId = timetableId;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }

    public Long getTimetableId() { return timetableId; }
    public void setTimetableId(Long timetableId) { this.timetableId = timetableId; }
} 