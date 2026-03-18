package com.digital_timetable.controller;

import com.digital_timetable.model.Announcement;
import com.digital_timetable.dto.NotificationDto;
import com.digital_timetable.service.AnnouncementService;
import com.digital_timetable.service.interf.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {
    @Autowired
    private AnnouncementService announcementService;

    @Autowired
    private NotificationService notificationService;


    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER', 'CLASS_REPRESENT', 'USER')")
    public ResponseEntity<?> getAllAnnouncements() {
        List<Announcement> announcements = announcementService.getAllAnnouncements();
        return ResponseEntity.ok(Map.of("data", announcements));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER','CLASS_REPRESENT')")
    public ResponseEntity<?> createAnnouncement(@RequestBody Map<String, Object> body, Principal principal) {
        String title = (String) body.get("title");
        String message = (String) body.get("message");
        List<String> roles = (List<String>) body.get("roles");
        String createdBy = principal.getName();
        // Validation: title and message are required; roles are optional and default to ALL
        if (title == null || message == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Title and message are required."));
        }
        if (roles == null || roles.isEmpty()) {
            roles = java.util.Collections.singletonList("ALL");
        }
        Announcement announcement = announcementService.createAnnouncement(title, message, createdBy, roles);
        // Notify targeted audiences without duplicates
        try {
            if (roles.stream().anyMatch(r -> "ALL".equalsIgnoreCase(r))) {
                // Single system-wide broadcast (one DB row + de-duped push/email)
                notificationService.broadcastSystemNotification(title, message);
            } else {
                // Single DB notification, unique recipients across roles, with WebSocket, email, and push
                notificationService.broadcastNotificationToRoles(title, message, roles);
            }
        } catch (Exception e) {
            System.err.println("Failed to broadcast announcement notifications: " + e.getMessage());
        }
        return ResponseEntity.ok(Map.of("data", announcement));
    }
}
