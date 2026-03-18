package com.digital_timetable.service;

import com.digital_timetable.entity.PushSubscription;
import com.digital_timetable.repository.PushSubscriptionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.Security;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PushNotificationService {
    static {
        // Ensure BouncyCastle is registered (required for EC keys used by Web Push)
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }
    
    @Autowired
    private PushSubscriptionRepository pushSubscriptionRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // VAPID keys - replace with your actual keys
    private final String VAPID_PUBLIC_KEY = "BAXmlI80BTm3cU8cZk8DNoIuyAvbaYSjg7GU5gHZNwKYvRpKXog5eHH_H2Hk_lUgCyR_zYZxZJNK7t2vc4ekgDA";
    private final String VAPID_PRIVATE_KEY = "skkrFmpTUuQex6iiSQ2E6VTE4H9Z5Qbzt-sC2BwYGb4";
    private final String VAPID_SUBJECT = "mailto:alainvalentin04@gmail.com";
    
    /**
     * Send push notification to a specific user
     */
    public void sendNotificationToUser(Long userId, String title, String body, String icon, String url) {
        List<PushSubscription> subscriptions = pushSubscriptionRepository.findByUserId(userId);
        for (PushSubscription subscription : subscriptions) {
            sendPushNotification(subscription, title, body, icon, url);
        }
    }
    
    /**
     * Send push notification to all users
     */
    public void sendNotificationToAll(String title, String body, String icon, String url) {
        System.out.println("[PushNotificationService] sendNotificationToAll called. Title: " + title + ", Body: " + body);
        List<PushSubscription> allSubscriptions = pushSubscriptionRepository.findAll();
        for (PushSubscription subscription : allSubscriptions) {
            sendPushNotification(subscription, title, body, icon, url);
        }
    }
    
    /**
     * Send push notification to users by role (requires User entity integration)
     */
    public void sendNotificationByRole(String role, String title, String body, String icon, String url) {
        System.out.println("[PushNotificationService] sendNotificationByRole called. Role: " + role + ", Title: " + title + ", Body: " + body);
        // TODO: Implement role-based filtering when User entity is integrated
        // For now, send to all subscriptions
        sendNotificationToAll(title, body, icon, url);
    }
    
    /**
     * Send individual push notification using Web Push Protocol
     */
    private void sendPushNotification(PushSubscription subscription, String title, String body, String icon, String url) {
        System.out.println("[PushNotificationService] sendPushNotification called. Endpoint: " + subscription.getEndpoint() + ", Title: " + title + ", Body: " + body);
        try {
            // Create notification payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("title", title);
            payload.put("body", body);
            payload.put("icon", icon != null ? icon : "/icon-192.png");
            payload.put("data", Map.of("url", url != null ? url : "/dashboard"));
            
            String payloadJson = objectMapper.writeValueAsString(payload);
            
            // Initialize PushService with VAPID keys
            PushService pushService = new PushService(VAPID_PUBLIC_KEY, VAPID_PRIVATE_KEY, VAPID_SUBJECT);
            
            // Create notification
            Notification notification = new Notification(
                subscription.getEndpoint(),
                subscription.getP256dh(),
                subscription.getAuth(),
                payloadJson
            );
            
            // Send the notification
            pushService.send(notification);
            
            System.out.println("Push notification sent successfully to: " + subscription.getEndpoint());
            
        } catch (Exception e) {
            System.err.println("Failed to send push notification: " + e.getMessage());
            // Remove invalid subscriptions (expired or unsubscribed)
            if (e.getMessage().contains("410") || e.getMessage().contains("404")) {
                pushSubscriptionRepository.delete(subscription);
                System.out.println("Removed invalid subscription: " + subscription.getEndpoint());
            }
        }
    }
    
    /**
     * Test notification method
     */
    public void sendTestNotification(Long userId) {
        sendNotificationToUser(userId, 
            "Test Notification", 
            "This is a test push notification from your Digital Timetable System!", 
            "/icon-192.png", 
            "/dashboard");
    }
}
