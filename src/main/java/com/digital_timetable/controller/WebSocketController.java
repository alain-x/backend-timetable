package com.digital_timetable.controller;

import com.digital_timetable.dto.NotificationDto;
import com.digital_timetable.service.interf.NotificationService;
import com.digital_timetable.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.Header;

@Controller
public class WebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private JwtUtils jwtUtils;

    @MessageMapping("/notifications")
    @SendTo("/topic/notifications")
    public NotificationDto sendNotification(@Payload NotificationDto notificationDto, SimpMessageHeaderAccessor headerAccessor) {
        try {
            // Validate the notification
            if (notificationDto == null || notificationDto.getTitle() == null || notificationDto.getMessage() == null) {
                throw new IllegalArgumentException("Invalid notification data");
            }

            // Save the notification to database
            notificationService.sendRealTimeNotification(notificationDto);
            
            // Send to all users subscribed to /topic/notifications
            return notificationDto;
        } catch (Exception e) {
            System.err.println("Error sending notification: " + e.getMessage());
            throw e;
        }
    }

    @MessageMapping("/user-notifications")
    public void sendUserNotification(@Payload NotificationDto notificationDto, @Header("userId") String userId) {
        try {
            // Validate the notification
            if (notificationDto == null || notificationDto.getTitle() == null || notificationDto.getMessage() == null) {
                throw new IllegalArgumentException("Invalid notification data");
            }

            // Save the notification to database
            notificationService.sendRealTimeNotification(notificationDto);
            
            // Send to specific user
            messagingTemplate.convertAndSend("/user/" + userId + "/notifications", notificationDto);
        } catch (Exception e) {
            System.err.println("Error sending user notification: " + e.getMessage());
        }
    }

    @MessageMapping("/role-notifications")
    public void sendRoleNotification(@Payload NotificationDto notificationDto, @Header("role") String role) {
        try {
            // Validate the notification
            if (notificationDto == null || notificationDto.getTitle() == null || notificationDto.getMessage() == null) {
                throw new IllegalArgumentException("Invalid notification data");
            }

            // Save the notification to database
            notificationService.sendRealTimeNotification(notificationDto);
            
            // Send to all users with specific role
            messagingTemplate.convertAndSend("/topic/notifications/" + role, notificationDto);
        } catch (Exception e) {
            System.err.println("Error sending role notification: " + e.getMessage());
        }
    }

    public void sendNotificationToUser(String userId, NotificationDto notificationDto) {
        try {
            // Save the notification to database
            notificationService.sendRealTimeNotification(notificationDto);
            
            // Send to specific user
            messagingTemplate.convertAndSend("/user/" + userId + "/notifications", notificationDto);
        } catch (Exception e) {
            System.err.println("Error sending notification to user " + userId + ": " + e.getMessage());
        }
    }

    public void sendNotificationToRole(String role, NotificationDto notificationDto) {
        try {
            // Save the notification to database
            notificationService.sendRealTimeNotification(notificationDto);
            
            // Send to all users with specific role
            messagingTemplate.convertAndSend("/topic/notifications/" + role, notificationDto);
        } catch (Exception e) {
            System.err.println("Error sending notification to role " + role + ": " + e.getMessage());
        }
    }

    public void sendGlobalNotification(NotificationDto notificationDto) {
        try {
            // Save the notification to database
            notificationService.sendRealTimeNotification(notificationDto);
            
            // Send to all users
            messagingTemplate.convertAndSend("/topic/notifications", notificationDto);
        } catch (Exception e) {
            System.err.println("Error sending global notification: " + e.getMessage());
        }
    }

    public void sendSystemNotification(String title, String message) {
        try {
            NotificationDto notificationDto = new NotificationDto(
                null,
                "system",
                title,
                message,
                java.time.LocalDateTime.now().toString(),
                false,
                null,
                null,
                null
            );
            
            sendGlobalNotification(notificationDto);
        } catch (Exception e) {
            System.err.println("Error sending system notification: " + e.getMessage());
        }
    }

    public void sendBookingNotification(String title, String message, Long userId, Long roomId) {
        try {
            NotificationDto notificationDto = new NotificationDto(
                null,
                "booking",
                title,
                message,
                java.time.LocalDateTime.now().toString(),
                false,
                userId,
                roomId,
                null
            );
            
            if (userId != null) {
                sendNotificationToUser(userId.toString(), notificationDto);
            } else {
                sendGlobalNotification(notificationDto);
            }
        } catch (Exception e) {
            System.err.println("Error sending booking notification: " + e.getMessage());
        }
    }

    public void sendConflictNotification(String title, String message, Long roomId, Long timetableId) {
        try {
            NotificationDto notificationDto = new NotificationDto(
                null,
                "conflict",
                title,
                message,
                java.time.LocalDateTime.now().toString(),
                false,
                null,
                roomId,
                timetableId
            );
            
            sendGlobalNotification(notificationDto);
        } catch (Exception e) {
            System.err.println("Error sending conflict notification: " + e.getMessage());
        }
    }

    public void sendSwapRequestNotification(String title, String message, Long userId) {
        try {
            NotificationDto notificationDto = new NotificationDto(
                null,
                "request",
                title,
                message,
                java.time.LocalDateTime.now().toString(),
                false,
                userId,
                null,
                null
            );
            
            if (userId != null) {
                sendNotificationToUser(userId.toString(), notificationDto);
            } else {
                sendGlobalNotification(notificationDto);
            }
        } catch (Exception e) {
            System.err.println("Error sending swap request notification: " + e.getMessage());
        }
    }

    @MessageExceptionHandler
    public void handleException(Throwable exception) {
        System.err.println("WebSocket error: " + exception.getMessage());
        // You can send error messages back to the client if needed
    }
} 