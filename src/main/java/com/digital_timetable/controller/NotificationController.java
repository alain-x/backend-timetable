package com.digital_timetable.controller;

import com.digital_timetable.dto.NotificationDto;
import com.digital_timetable.dto.Response;
import com.digital_timetable.service.interf.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.security.core.context.SecurityContextHolder;
import com.digital_timetable.security.AuthUser;
import com.digital_timetable.enums.UserRole;
import com.digital_timetable.entity.Notification;
import com.digital_timetable.repository.NotificationRepo;
import com.digital_timetable.entity.User;
import com.digital_timetable.exception.NotFoundException;
import com.digital_timetable.entity.DismissedNotification;
import com.digital_timetable.repository.DismissedNotificationRepo;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepo notificationRepo;

    @Autowired
    private DismissedNotificationRepo dismissedNotificationRepo;

    @GetMapping
    public ResponseEntity<Response> getAllNotifications() {
        try {
            List<NotificationDto> notifications = notificationService.getAllNotifications();
            Response response = Response.builder()
                    .status(HttpStatus.OK.value())
                    .message("Notifications retrieved successfully")
                    .data(notifications)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Response response = Response.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to retrieve notifications: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Response> getNotificationsByUserId(@PathVariable Long userId) {
        try {
            List<NotificationDto> notifications = notificationService.getNotificationsByUserId(userId);
            Response response = Response.builder()
                    .status(HttpStatus.OK.value())
                    .message("User notifications retrieved successfully")
                    .data(notifications)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Response response = Response.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to retrieve user notifications: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/unread")
    public ResponseEntity<Response> getUnreadNotifications() {
        try {
            List<NotificationDto> notifications = notificationService.getUnreadNotifications();
            Response response = Response.builder()
                    .status(HttpStatus.OK.value())
                    .message("Unread notifications retrieved successfully")
                    .data(notifications)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Response response = Response.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to retrieve unread notifications: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/unread/count")
    public ResponseEntity<Response> getUnreadCount() {
        try {
            Long count = notificationService.getUnreadCount();
            Response response = Response.builder()
                    .status(HttpStatus.OK.value())
                    .message("Unread count retrieved successfully")
                    .data(count)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Response response = Response.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to retrieve unread count: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/unread/count/{userId}")
    public ResponseEntity<Response> getUnreadCountByUserId(@PathVariable Long userId) {
        try {
            Long count = notificationService.getUnreadCountByUserId(userId);
            Response response = Response.builder()
                    .status(HttpStatus.OK.value())
                    .message("User unread count retrieved successfully")
                    .data(count)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Response response = Response.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to retrieve user unread count: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Response> markAsRead(@PathVariable Long id) {
        try {
            NotificationDto notification = notificationService.markAsRead(id);
            Response response = Response.builder()
                    .status(HttpStatus.OK.value())
                    .message("Notification marked as read successfully")
                    .data(notification)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Response response = Response.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to mark notification as read: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/mark-all-read")
    public ResponseEntity<Response> markAllAsRead() {
        try {
            notificationService.markAllAsRead();
            Response response = Response.builder()
                    .status(HttpStatus.OK.value())
                    .message("All notifications marked as read successfully")
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Response response = Response.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to mark all notifications as read: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/mark-all-read/{userId}")
    public ResponseEntity<Response> markAllAsReadByUserId(@PathVariable Long userId) {
        try {
            notificationService.markAllAsReadByUserId(userId);
            Response response = Response.builder()
                    .status(HttpStatus.OK.value())
                    .message("All user notifications marked as read successfully")
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Response response = Response.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to mark all user notifications as read: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Response> deleteNotification(@PathVariable Long id) {
        try {
            // Load notification or 404
            Notification notification = notificationRepo.findById(id)
                    .orElseThrow(() -> new NotFoundException("Notification not found"));

            // Determine if current user is admin (by principal or authorities)
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdminAuthority = authentication != null && authentication.getAuthorities() != null &&
                    authentication.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equalsIgnoreCase(a.getAuthority()));

            User currentUser = null;
            if (authentication != null && authentication.getPrincipal() instanceof AuthUser) {
                currentUser = ((AuthUser) authentication.getPrincipal()).getUser();
            }

            boolean isOwner = currentUser != null && notification.getUser() != null &&
                    notification.getUser().getId().equals(currentUser.getId());

            boolean isAdmin = isAdminAuthority || (currentUser != null && currentUser.getRole() == UserRole.ADMIN);

            if (!isAdmin && !isOwner) {
                Response response = Response.builder()
                        .status(HttpStatus.FORBIDDEN.value())
                        .message("You can only delete your own notifications")
                        .build();
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            notificationService.deleteNotification(id);
            Response response = Response.builder()
                    .status(HttpStatus.OK.value())
                    .message("Notification deleted successfully")
                    .build();
            return ResponseEntity.ok(response);
        } catch (NotFoundException e) {
            Response response = Response.builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .message(e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            Response response = Response.builder()
                    .status(HttpStatus.CONFLICT.value())
                    .message("Cannot delete notification due to related data: " + e.getMostSpecificCause().getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        } catch (Exception e) {
            Response response = Response.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to delete notification: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping
    public ResponseEntity<Response> createNotification(@RequestBody NotificationDto notificationDto) {
        try {
            NotificationDto createdNotification = notificationService.createNotification(notificationDto);
            Response response = Response.builder()
                    .status(HttpStatus.CREATED.value())
                    .message("Notification created successfully")
                    .data(createdNotification)
                    .build();
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Response response = Response.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to create notification: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/booking")
    public ResponseEntity<Response> createBookingNotification(@RequestBody NotificationDto notificationDto) {
        try {
            notificationService.createBookingNotification(
                notificationDto.getTitle(),
                notificationDto.getMessage(),
                notificationDto.getUserId(),
                notificationDto.getRoomId()
            );
            Response response = Response.builder()
                    .status(HttpStatus.CREATED.value())
                    .message("Booking notification created successfully")
                    .build();
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Response response = Response.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to create booking notification: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/system")
    public ResponseEntity<Response> createSystemNotification(@RequestBody NotificationDto notificationDto) {
        try {
            notificationService.createSystemNotification(
                notificationDto.getTitle(),
                notificationDto.getMessage()
            );
            Response response = Response.builder()
                    .status(HttpStatus.CREATED.value())
                    .message("System notification created successfully")
                    .build();
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Response response = Response.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to create system notification: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Test endpoint to send an email to a specific user email
    @PostMapping("/test-email")
    public ResponseEntity<Response> sendTestEmail(@RequestParam String email,
                                                  @RequestParam(defaultValue = "Test Notification") String title,
                                                  @RequestParam(defaultValue = "This is a test notification email from Digital Timetable System.") String message) {
        try {
            notificationService.broadcastNotificationToUser(title, message, email);
            Response response = Response.builder()
                    .status(HttpStatus.OK.value())
                    .message("Test email sent to " + email)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Response response = Response.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to send test email: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/{id}/dismiss")
    public ResponseEntity<?> dismissNotification(@PathVariable Long id) {
        AuthUser authUser = (AuthUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = authUser.getUser().getId();
        if (!dismissedNotificationRepo.existsByUserIdAndNotificationId(userId, id)) {
            DismissedNotification dn = new DismissedNotification();
            dn.setUser(authUser.getUser());
            dn.setNotification(notificationRepo.findById(id).orElseThrow());
            dismissedNotificationRepo.save(dn);
        }
        return ResponseEntity.ok().build();
    }
} 