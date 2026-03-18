package com.digital_timetable.service.interf;

import com.digital_timetable.dto.NotificationDto;

import java.util.List;

public interface NotificationService {
    
    List<NotificationDto> getAllNotifications();
    
    List<NotificationDto> getNotificationsByUserId(Long userId);
    
    List<NotificationDto> getNotificationsByUserEmail(String userEmail);
    
    List<NotificationDto> getRoleBasedNotifications(String userEmail);
    
    List<NotificationDto> getUnreadNotifications();
    
    List<NotificationDto> getUnreadNotificationsByUserEmail(String userEmail);
    
    Long getUnreadCount();
    
    Long getUnreadCountByUserId(Long userId);
    
    Long getUnreadCountByUserEmail(String userEmail);
    
    NotificationDto markAsRead(Long notificationId);
    
    NotificationDto markAsReadByUser(Long notificationId, String userEmail);
    
    void markAllAsRead();
    
    void markAllAsReadByUserId(Long userId);
    
    void markAllAsReadByUserEmail(String userEmail);
    
    void deleteNotification(Long notificationId);
    
    void deleteNotificationByUser(Long notificationId, String userEmail);
    
    void deleteAllNotificationsByUserEmail(String userEmail);
    
    NotificationDto createNotification(NotificationDto notificationDto);
    
    // Helper methods for creating specific types of notifications
    void createBookingNotification(String title, String message, Long userId, Long roomId);
    
    void createRequestNotification(String title, String message, Long userId);
    
    void createConflictNotification(String title, String message, Long roomId, Long timetableId);
    
    void createSystemNotification(String title, String message);
    
    // Real-time notification methods
    void createTaskCompletedNotification(String taskType, String taskDescription, Long userId);
    
    void createRoleBasedNotification(String title, String message, String role);
    
    // Real-time notification broadcasting
    void broadcastNotificationToRole(String title, String message, String role);
    
    // Broadcast a single notification to multiple roles (no DB duplicates)
    void broadcastNotificationToRoles(String title, String message, java.util.List<String> roles);
    
    void broadcastNotificationToUser(String title, String message, String userEmail);
    
    void broadcastSystemNotification(String title, String message);
    
    // WebSocket notification methods
    void sendRealTimeNotification(NotificationDto notificationDto);
    
    void sendNotificationToAllUsers(NotificationDto notificationDto);
    
    void sendNotificationToRole(String role, NotificationDto notificationDto);
    
    // Role-based notification methods
    List<NotificationDto> getNotificationsByRole(String role);
    
    List<NotificationDto> getUnreadNotificationsByRole(String role);
    
    Long getUnreadCountByRole(String role);
    
    void markAllAsReadByRole(String role);
    
    void deleteNotificationsByUserId(Long userId);
    
    void deleteNotificationsByRole(String role);
} 