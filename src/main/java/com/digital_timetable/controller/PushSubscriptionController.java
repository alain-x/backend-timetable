package com.digital_timetable.controller;

import com.digital_timetable.entity.PushSubscription;
import com.digital_timetable.repository.PushSubscriptionRepository;
import com.digital_timetable.service.PushNotificationService;
import com.digital_timetable.dto.PushSendRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/push")
public class PushSubscriptionController {
    @Autowired
    private PushSubscriptionRepository repo;
    
    @Autowired
    private PushNotificationService pushNotificationService;

    // Expose VAPID public key (replace with your real key)
    @GetMapping("/vapidPublicKey")
    public String getVapidPublicKey() {
        return "BAXmlI80BTm3cU8cZk8DNoIuyAvbaYSjg7GU5gHZNwKYvRpKXog5eHH_H2Hk_lUgCyR_zYZxZJNK7t2vc4ekgDA";
    }

    // Store or update a push subscription
    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(@RequestParam Long userId, @RequestBody Map<String, Object> body, @RequestHeader(value = "User-Agent", required = false) String userAgent) {
        Map<String, String> keys = (Map<String, String>) body.get("keys");
        String endpoint = (String) body.get("endpoint");
        String p256dh = keys.get("p256dh");
        String auth = keys.get("auth");
        PushSubscription existing = repo.findByEndpoint(endpoint);
        if (existing == null) {
            existing = new PushSubscription();
        }
        existing.setUserId(userId);
        existing.setEndpoint(endpoint);
        existing.setP256dh(p256dh);
        existing.setAuth(auth);
        existing.setUserAgent(userAgent);
        repo.save(existing);
        return ResponseEntity.ok().build();
    }
    
    // Test endpoint to send a push notification
    @PostMapping("/test")
    public ResponseEntity<?> sendTestNotification(@RequestParam Long userId) {
        try {
            pushNotificationService.sendTestNotification(userId);
            return ResponseEntity.ok("Test notification sent successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to send notification: " + e.getMessage());
        }
    }
    
    // Send a notification to a specific user via JSON body
    @PostMapping("/send")
    public ResponseEntity<?> sendNotification(@RequestBody PushSendRequest request) {
        try {
            if (request.getUserId() == null) {
                return ResponseEntity.badRequest().body("userId is required");
            }
            pushNotificationService.sendNotificationToUser(
                request.getUserId(),
                request.getTitle(),
                request.getBody(),
                request.getIcon(),
                request.getUrl()
            );
            return ResponseEntity.ok("Notification sent");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to send notification: " + e.getMessage());
        }
    }
    
    // Send notification to all users
    @PostMapping("/broadcast")
    public ResponseEntity<?> broadcastNotification(
            @RequestParam String title,
            @RequestParam String body,
            @RequestParam(required = false) String icon,
            @RequestParam(required = false) String url) {
        try {
            pushNotificationService.sendNotificationToAll(title, body, icon, url);
            return ResponseEntity.ok("Broadcast notification sent successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to send broadcast: " + e.getMessage());
        }
    }
}
