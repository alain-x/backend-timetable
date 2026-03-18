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
    public ResponseEntity<?> subscribe(@RequestParam String userId, @RequestBody Map<String, Object> body, @RequestHeader(value = "User-Agent", required = false) String userAgent) {
        try {
            Long parsedUserId;
            try {
                parsedUserId = Long.parseLong(userId);
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body("Invalid userId. Expected a numeric userId, got: " + userId);
            }

            Object keysObj = body.get("keys");
            if (!(keysObj instanceof Map)) {
                return ResponseEntity.badRequest().body("Invalid subscription payload: keys is required");
            }

            Map<String, Object> keys = (Map<String, Object>) keysObj;
            Object endpointObj = body.get("endpoint");
            if (!(endpointObj instanceof String) || ((String) endpointObj).isBlank()) {
                return ResponseEntity.badRequest().body("Invalid subscription payload: endpoint is required");
            }
            String endpoint = (String) endpointObj;

            Object p256dhObj = keys.get("p256dh");
            Object authObj = keys.get("auth");
            if (!(p256dhObj instanceof String) || ((String) p256dhObj).isBlank() || !(authObj instanceof String) || ((String) authObj).isBlank()) {
                return ResponseEntity.badRequest().body("Invalid subscription payload: keys.p256dh and keys.auth are required");
            }
            String p256dh = (String) p256dhObj;
            String auth = (String) authObj;

            PushSubscription existing = repo.findByEndpoint(endpoint);
            if (existing == null) {
                existing = new PushSubscription();
            }
            existing.setUserId(parsedUserId);
            existing.setEndpoint(endpoint);
            existing.setP256dh(p256dh);
            existing.setAuth(auth);
            existing.setUserAgent(userAgent);
            repo.save(existing);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to save subscription: " + e.getMessage());
        }
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
