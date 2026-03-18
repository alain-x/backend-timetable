package com.digital_timetable.service.impl;

import com.digital_timetable.dto.NotificationDto;
import com.digital_timetable.entity.Notification;
import com.digital_timetable.entity.Room;
import com.digital_timetable.entity.Timetable;
import com.digital_timetable.entity.User;
import com.digital_timetable.enums.UserRole;
import com.digital_timetable.exception.NotFoundException;
import com.digital_timetable.repository.DismissedNotificationRepo;
import com.digital_timetable.repository.NotificationRepo;
import com.digital_timetable.repository.RoomRepo;
import com.digital_timetable.repository.TimetableRepo;
import com.digital_timetable.repository.UserRepo;
import com.digital_timetable.security.AuthUser;
import com.digital_timetable.service.interf.NotificationService;
import com.digital_timetable.service.PushNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepo notificationRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private RoomRepo roomRepo;

    @Autowired
    private TimetableRepo timetableRepo;

    @Autowired
    private DismissedNotificationRepo dismissedNotificationRepo;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private EmailClient emailClient;

    @Autowired
    private PushNotificationService pushNotificationService;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public List<NotificationDto> getAllNotifications() {
        // Get current user from security context
        Object principal = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = null;
        if (principal instanceof AuthUser) {
            userId = ((AuthUser) principal).getUser().getId();
        }
        List<Long> dismissedIds = userId != null ? dismissedNotificationRepo.findByUserId(userId)
            .stream().map(dn -> dn.getNotification().getId()).toList() : List.of();
        return notificationRepo.findAllOrderByTimestampDesc()
                .stream()
                .filter(n -> !dismissedIds.contains(n.getId()))
                .map(this::convertToDto)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<NotificationDto> getNotificationsByUserId(Long userId) {
        return notificationRepo.findNotificationsByUserId(userId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<NotificationDto> getNotificationsByUserEmail(String userEmail) {
        User user = userRepo.findByEmail(userEmail).orElse(null);
        if (user != null) {
            return notificationRepo.findNotificationsByUserId(user.getId())
                    .stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    @Override
    public List<NotificationDto> getRoleBasedNotifications(String userEmail) {
        User user = userRepo.findByEmail(userEmail).orElse(null);
        if (user != null) {
            return notificationRepo.findNotificationsByUserRole(user.getRole().name())
                    .stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    @Override
    public List<NotificationDto> getUnreadNotifications() {
        return notificationRepo.findUnreadNotifications()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<NotificationDto> getUnreadNotificationsByUserEmail(String userEmail) {
        User user = userRepo.findByEmail(userEmail).orElse(null);
        if (user != null) {
            return notificationRepo.findUnreadNotificationsByUserId(user.getId())
                    .stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    @Override
    public Long getUnreadCount() {
        return notificationRepo.countUnreadNotifications();
    }

    @Override
    public Long getUnreadCountByUserId(Long userId) {
        return notificationRepo.countUnreadNotificationsByUserId(userId);
    }

    @Override
    public Long getUnreadCountByUserEmail(String userEmail) {
        User user = userRepo.findByEmail(userEmail).orElse(null);
        if (user != null) {
            return notificationRepo.countUnreadNotificationsByUserId(user.getId());
        }
        return 0L;
    }

    @Override
    public NotificationDto markAsRead(Long notificationId) {
        Notification notification = notificationRepo.findById(notificationId)
                .orElseThrow(() -> new NotFoundException("Notification not found"));

        notification.setRead(true);
        Notification savedNotification = notificationRepo.save(notification);
        return convertToDto(savedNotification);
    }

    @Override
    public NotificationDto markAsReadByUser(Long notificationId, String userEmail) {
        User user = userRepo.findByEmail(userEmail).orElse(null);
        if (user != null) {
            Notification notification = notificationRepo.findById(notificationId)
                    .orElseThrow(() -> new NotFoundException("Notification not found"));
            
            // Check if the notification belongs to the user
            if (notification.getUser() != null && notification.getUser().getId().equals(user.getId())) {
                notification.setRead(true);
                Notification savedNotification = notificationRepo.save(notification);
                return convertToDto(savedNotification);
            } else {
                throw new NotFoundException("Notification not found for this user");
            }
        }
        throw new NotFoundException("User not found");
    }

    @Override
    public void markAllAsRead() {
        List<Notification> unreadNotifications = notificationRepo.findUnreadNotifications();
        unreadNotifications.forEach(notification -> notification.setRead(true));
        notificationRepo.saveAll(unreadNotifications);
    }

    @Override
    public void markAllAsReadByUserId(Long userId) {
        List<Notification> userNotifications = notificationRepo.findNotificationsByUserId(userId);
        userNotifications.stream()
                .filter(notification -> !notification.isRead())
                .forEach(notification -> notification.setRead(true));
        notificationRepo.saveAll(userNotifications);
    }

    @Override
    public void markAllAsReadByUserEmail(String userEmail) {
        User user = userRepo.findByEmail(userEmail).orElse(null);
        if (user != null) {
            List<Notification> userNotifications = notificationRepo.findNotificationsByUserId(user.getId());
            userNotifications.stream()
                    .filter(notification -> !notification.isRead())
                    .forEach(notification -> notification.setRead(true));
            notificationRepo.saveAll(userNotifications);
        }
    }

    @Override
    @Transactional
    public void deleteNotification(Long notificationId) {
        if (!notificationRepo.existsById(notificationId)) {
            throw new NotFoundException("Notification not found");
        }
        // Remove dismissed references first to avoid FK constraint violations
        try {
            dismissedNotificationRepo.deleteByNotificationId(notificationId);
        } catch (Exception e) {
            System.err.println("Failed to delete dismissed notifications for notification " + notificationId + ": " + e.getMessage());
        }
        notificationRepo.deleteById(notificationId);
    }

    @Override
    public void deleteNotificationByUser(Long notificationId, String userEmail) {
        User user = userRepo.findByEmail(userEmail).orElse(null);
        if (user != null) {
            Notification notification = notificationRepo.findById(notificationId)
                    .orElseThrow(() -> new NotFoundException("Notification not found"));
            
            // Check if the notification belongs to the user
            if (notification.getUser() != null && notification.getUser().getId().equals(user.getId())) {
                notificationRepo.deleteById(notificationId);
            } else {
                throw new NotFoundException("Notification not found for this user");
            }
        }
    }

    @Override
    public NotificationDto createNotification(NotificationDto notificationDto) {
        Notification notification = convertToEntity(notificationDto);
        notification.setTimestamp(LocalDateTime.now());
        Notification savedNotification = notificationRepo.save(notification);
        // email: if targeted to a user, send
        if (savedNotification.getUser() != null && savedNotification.getUser().getEmail() != null) {
            emailClient.sendEmailTo(
                    savedNotification.getUser().getEmail(),
                    savedNotification.getTitle(),
                    savedNotification.getMessage()
            );
            try {
                // Push to the specific user as well
                pushNotificationService.sendNotificationToUser(
                        savedNotification.getUser().getId(),
                        savedNotification.getTitle(),
                        savedNotification.getMessage(),
                        "/icon-192.png",
                        "/dashboard"
                );
            } catch (Exception e) {
                System.err.println("Failed to send push (createNotification): " + e.getMessage());
            }
        }
        return convertToDto(savedNotification);
    }

    @Override
    public void createBookingNotification(String title, String message, Long userId, Long roomId) {
        User user = userRepo.findById(userId).orElse(null);
        Room room = roomRepo.findById(roomId).orElse(null);

        Notification notification = Notification.builder()
                .type(Notification.NotificationType.BOOKING)
                .title(title)
                .message(message)
                .timestamp(LocalDateTime.now())
                .read(false)
                .user(user)
                .room(room)
                .build();

        notificationRepo.save(notification);
        // email user if available
        if (user != null && user.getEmail() != null) {
            emailClient.sendEmailTo(user.getEmail(), title, message);
        }
        // push to the user
        if (user != null) {
            try {
                pushNotificationService.sendNotificationToUser(user.getId(), title, message, "/icon-192.png", "/dashboard");
            } catch (Exception e) {
                System.err.println("Failed to send push (createBookingNotification): " + e.getMessage());
            }
        }
    }

    @Override
    public void createRequestNotification(String title, String message, Long userId) {
        User user = userRepo.findById(userId).orElse(null);

        Notification notification = Notification.builder()
                .type(Notification.NotificationType.REQUEST)
                .title(title)
                .message(message)
                .timestamp(LocalDateTime.now())
                .read(false)
                .user(user)
                .build();

        notificationRepo.save(notification);
        // email user if available
        if (user != null && user.getEmail() != null) {
            emailClient.sendEmailTo(user.getEmail(), title, message);
        }
        // push to the user as well
        if (user != null) {
            try {
                pushNotificationService.sendNotificationToUser(user.getId(), title, message, "/icon-192.png", "/dashboard");
            } catch (Exception e) {
                System.err.println("Failed to send push (createRequestNotification): " + e.getMessage());
            }
        }
    }

    @Override
    public void createConflictNotification(String title, String message, Long roomId, Long timetableId) {
        Room room = roomRepo.findById(roomId).orElse(null);
        Timetable timetable = timetableRepo.findById(timetableId).orElse(null);

        Notification notification = Notification.builder()
                .type(Notification.NotificationType.CONFLICT)
                .title(title)
                .message(message)
                .timestamp(LocalDateTime.now())
                .read(false)
                .room(room)
                .timetable(timetable)
                .build();

        notificationRepo.save(notification);
        // notify admins via email
        List<User> admins = userRepo.findByRole(UserRole.ADMIN);
        List<String> emails = admins.stream()
                .map(User::getEmail)
                .filter(java.util.Objects::nonNull)
                .toList();
        if (!emails.isEmpty()) {
            emailClient.sendEmail(emails, title, message);
        }
        // push to admins
        try {
            pushNotificationService.sendNotificationByRole("ADMIN", title, message, "/icon-192.png", "/dashboard");
        } catch (Exception e) {
            System.err.println("Failed to send push (createConflictNotification): " + e.getMessage());
        }
    }

    @Override
    public void createSystemNotification(String title, String message) {
        Notification notification = Notification.builder()
                .type(Notification.NotificationType.SYSTEM)
                .title(title)
                .message(message)
                .timestamp(LocalDateTime.now())
                .read(false)
                .build();

        notificationRepo.save(notification);

        // Send email to all users for system notifications
        try {
            List<User> allUsers = userRepo.findAll();
            List<String> emails = allUsers.stream()
                    .map(User::getEmail)
                    .filter(java.util.Objects::nonNull)
                    .toList();
            if (!emails.isEmpty()) {
                emailClient.sendEmail(emails, title, message);
            }
        } catch (Exception e) {
            System.err.println("Failed to email system notification: " + e.getMessage());
        }
        // Push to all users
        try {
            pushNotificationService.sendNotificationToAll(title, message, "/icon-192.png", "/dashboard");
        } catch (Exception e) {
            System.err.println("Failed to send push (createSystemNotification): " + e.getMessage());
        }
    }

    @Override
    public void createTaskCompletedNotification(String taskType, String taskDescription, Long userId) {
        User user = userRepo.findById(userId).orElse(null);

        Notification notification = Notification.builder()
                .type(Notification.NotificationType.SYSTEM)
                .title("Task Completed: " + taskType)
                .message(taskDescription)
                .timestamp(LocalDateTime.now())
                .read(false)
                .user(user)
                .build();

        notificationRepo.save(notification);
        if (user != null && user.getEmail() != null) {
            emailClient.sendEmailTo(user.getEmail(), "Task Completed: " + taskType, taskDescription);
        }
        if (user != null) {
            try {
                pushNotificationService.sendNotificationToUser(user.getId(), "Task Completed: " + taskType, taskDescription, "/icon-192.png", "/dashboard");
            } catch (Exception e) {
                System.err.println("Failed to send push (createTaskCompletedNotification): " + e.getMessage());
            }
        }
    }

    @Override
    public void createRoleBasedNotification(String title, String message, String role) {
        try {
            UserRole userRole = UserRole.valueOf(role.toUpperCase());
            List<User> usersWithRole = userRepo.findByRole(userRole);

            for (User user : usersWithRole) {
                Notification notification = Notification.builder()
                        .type(Notification.NotificationType.SYSTEM)
                        .title(title)
                        .message(message)
                        .timestamp(LocalDateTime.now())
                        .read(false)
                        .user(user)
                        .build();

                notificationRepo.save(notification);
            }
            // email all users with the role
            List<String> emails = usersWithRole.stream()
                    .map(User::getEmail)
                    .filter(java.util.Objects::nonNull)
                    .toList();
            if (!emails.isEmpty()) {
                emailClient.sendEmail(emails, title, message);
            }
            // email admins
            List<User> admins = userRepo.findByRole(UserRole.ADMIN);
            List<String> adminEmails = admins.stream()
                    .map(User::getEmail)
                    .filter(java.util.Objects::nonNull)
                    .toList();
            if (!adminEmails.isEmpty()) {
                emailClient.sendEmail(adminEmails, title, message);
            }
            // push to the role
            try {
                pushNotificationService.sendNotificationByRole(role, title, message, "/icon-192.png", "/dashboard");
            } catch (Exception e) {
                System.err.println("Failed to send push (createRoleBasedNotification): " + e.getMessage());
            }
        } catch (IllegalArgumentException e) {
            // Handle invalid role string
            System.err.println("Invalid role: " + role);
        }
    }

    @Override
    public List<NotificationDto> getNotificationsByRole(String role) {
        return notificationRepo.findNotificationsByUserRole(role)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<NotificationDto> getUnreadNotificationsByRole(String role) {
        return notificationRepo.findUnreadNotificationsByUserRole(role)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Long getUnreadCountByRole(String role) {
        return notificationRepo.countUnreadNotificationsByUserRole(role);
    }

    @Override
    public void markAllAsReadByRole(String role) {
        List<Notification> roleNotifications = notificationRepo.findNotificationsByUserRole(role);
        roleNotifications.stream()
                .filter(notification -> !notification.isRead())
                .forEach(notification -> notification.setRead(true));
        notificationRepo.saveAll(roleNotifications);
    }

    @Override
    public void deleteNotificationsByUserId(Long userId) {
        List<Notification> userNotifications = notificationRepo.findNotificationsByUserId(userId);
        notificationRepo.deleteAll(userNotifications);
    }

    @Override
    public void deleteNotificationsByRole(String role) {
        List<Notification> roleNotifications = notificationRepo.findNotificationsByUserRole(role);
        notificationRepo.deleteAll(roleNotifications);
    }

    @Override
    public void deleteAllNotificationsByUserEmail(String userEmail) {
        User user = userRepo.findByEmail(userEmail).orElse(null);
        if (user != null) {
            List<Notification> userNotifications = notificationRepo.findNotificationsByUserId(user.getId());
            notificationRepo.deleteAll(userNotifications);
        }
    }

    @Override
    public void sendRealTimeNotification(NotificationDto notificationDto) {
        try {
            Notification notification = convertToEntity(notificationDto);
            notification.setTimestamp(LocalDateTime.now());
            Notification savedNotification = notificationRepo.save(notification);

            NotificationDto savedDto = convertToDto(savedNotification);

            // Send to general topic
            messagingTemplate.convertAndSend("/topic/notifications", savedDto);

            // Send to specific user if userId is provided
            if (notificationDto.getUserId() != null) {
                messagingTemplate.convertAndSend("/user/" + notificationDto.getUserId() + "/notifications", savedDto);
            }

            // Send to role-based topic if user has a role
            if (notificationDto.getUserId() != null) {
                User user = userRepo.findById(notificationDto.getUserId()).orElse(null);
                if (user != null && user.getRole() != null) {
                    messagingTemplate.convertAndSend("/topic/notifications/" + user.getRole().name(), savedDto);
                }
            }

            System.out.println("Real-time notification sent successfully: " + savedDto.getTitle());
        } catch (Exception e) {
            System.err.println("Error sending real-time notification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void sendNotificationToAllUsers(NotificationDto notificationDto) {
        Notification notification = convertToEntity(notificationDto);
        notification.setTimestamp(LocalDateTime.now());
        Notification saved = notificationRepo.save(notification);
        NotificationDto savedDto = convertToDto(saved);

        // WebSocket broadcast to all
        messagingTemplate.convertAndSend("/topic/notifications", savedDto);

        // Email all users automatically
        try {
            List<User> allUsers = userRepo.findAll();
            List<String> emails = allUsers.stream()
                    .map(User::getEmail)
                    .filter(java.util.Objects::nonNull)
                    .toList();
            if (!emails.isEmpty()) {
                emailClient.sendEmail(emails, savedDto.getTitle(), savedDto.getMessage());
            }
        } catch (Exception e) {
            System.err.println("Failed to email all-users notification: " + e.getMessage());
        }
        // Push to all users as well
        try {
            pushNotificationService.sendNotificationToAll(savedDto.getTitle(), savedDto.getMessage(), "/icon-192.png", "/dashboard");
        } catch (Exception e) {
            System.err.println("Failed to send push (sendNotificationToAllUsers): " + e.getMessage());
        }
    }

    @Override
    public void sendNotificationToRole(String role, NotificationDto notificationDto) {
        Notification notification = convertToEntity(notificationDto);
        notification.setTimestamp(LocalDateTime.now());
        Notification saved = notificationRepo.save(notification);
        NotificationDto savedDto = convertToDto(saved);

        // WebSocket broadcast to role topic
        String roleTopic = "/topic/notifications/" + role.toUpperCase();
        messagingTemplate.convertAndSend(roleTopic, savedDto);

        // Email all users with the role automatically
        try {
            UserRole userRole = UserRole.valueOf(role.toUpperCase());
            List<User> usersWithRole = userRepo.findByRole(userRole);
            List<String> emails = usersWithRole.stream()
                    .map(User::getEmail)
                    .filter(java.util.Objects::nonNull)
                    .toList();
            if (!emails.isEmpty()) {
                emailClient.sendEmail(emails, savedDto.getTitle(), savedDto.getMessage());
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid role for role-email broadcast: " + role);
        } catch (Exception e) {
            System.err.println("Failed to email role notification: " + e.getMessage());
        }
        // Push to users with the role as well
        try {
            pushNotificationService.sendNotificationByRole(role, savedDto.getTitle(), savedDto.getMessage(), "/icon-192.png", "/dashboard");
        } catch (Exception e) {
            System.err.println("Failed to send push (sendNotificationToRole): " + e.getMessage());
        }
    }

    @Override
    public void broadcastNotificationToRole(String title, String message, String role) {
        NotificationDto notificationDto = new NotificationDto(
                null,
                "system",
                title,
                message,
                LocalDateTime.now().format(formatter),
                false,
                null,
                null,
                null
        );
        // WebSocket + email handled by sendNotificationToRole
        sendNotificationToRole(role, notificationDto);
    }

    @Override
    public void broadcastNotificationToRoles(String title, String message, java.util.List<String> roles) {
        if (roles == null || roles.isEmpty()) return;
        // If ALL is included, delegate to system broadcast (single path)
        boolean hasAll = roles.stream().anyMatch(r -> "ALL".equalsIgnoreCase(r));
        if (hasAll) {
            broadcastSystemNotification(title, message);
            return;
        }

        // Create one notification in DB
        Notification notification = Notification.builder()
                .type(Notification.NotificationType.SYSTEM)
                .title(title)
                .message(message)
                .timestamp(LocalDateTime.now())
                .read(false)
                .build();
        Notification saved = notificationRepo.save(notification);
        NotificationDto savedDto = convertToDto(saved);

        // Aggregate unique users from roles
        Set<Long> uniqueUserIds = new HashSet<>();
        Set<String> uniqueEmails = new HashSet<>();
        for (String role : roles) {
            try {
                UserRole userRole = UserRole.valueOf(role.toUpperCase());
                List<User> users = userRepo.findByRole(userRole);
                for (User u : users) {
                    if (u != null) {
                        if (u.getId() != null) uniqueUserIds.add(u.getId());
                        if (u.getEmail() != null) uniqueEmails.add(u.getEmail());
                    }
                }
                // WebSocket to role topic
                messagingTemplate.convertAndSend("/topic/notifications/" + userRole.name(), savedDto);
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid role in broadcastNotificationToRoles: " + role);
            }
        }

        // Email unique recipients
        if (!uniqueEmails.isEmpty()) {
            try {
                emailClient.sendEmail(new java.util.ArrayList<>(uniqueEmails), title, message);
            } catch (Exception e) {
                System.err.println("Failed to email multi-role broadcast: " + e.getMessage());
            }
        }

        // Push per unique user
        for (Long uid : uniqueUserIds) {
            try {
                pushNotificationService.sendNotificationToUser(uid, title, message, "/icon-192.png", "/dashboard");
            } catch (Exception e) {
                System.err.println("Failed to push to user " + uid + ": " + e.getMessage());
            }
        }
    }

    @Override
    public void broadcastNotificationToUser(String title, String message, String userEmail) {
        User user = userRepo.findByEmail(userEmail).orElse(null);
        if (user != null) {
            NotificationDto notificationDto = new NotificationDto(
                    null,
                    "system",
                    title,
                    message,
                    LocalDateTime.now().format(formatter),
                    false,
                    user.getId(),
                    null,
                    null
            );
            sendRealTimeNotification(notificationDto);

            // Send email to specific user
            if (user.getEmail() != null) {
                emailClient.sendEmail(java.util.List.of(user.getEmail()), title, message);
            }
        }
    }

    @Override
    public void broadcastSystemNotification(String title, String message) {
        NotificationDto notificationDto = new NotificationDto(
                null,
                "system",
                title,
                message,
                LocalDateTime.now().format(formatter),
                false,
                null,
                null,
                null
        );
        // WebSocket + email handled by sendNotificationToAllUsers
        sendNotificationToAllUsers(notificationDto);
    }

    private NotificationDto convertToDto(Notification notification) {
        return new NotificationDto(
                notification.getId(),
                notification.getType().name().toLowerCase(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getTimestamp().format(formatter),
                notification.isRead(),
                notification.getUser() != null ? notification.getUser().getId() : null,
                notification.getRoom() != null ? notification.getRoom().getId() : null,
                notification.getTimetable() != null ? notification.getTimetable().getId() : null
        );
    }

    private Notification convertToEntity(NotificationDto dto) {
        Notification.NotificationType type = Notification.NotificationType.valueOf(dto.getType().toUpperCase());

        Notification.NotificationBuilder builder = Notification.builder()
                .type(type)
                .title(dto.getTitle())
                .message(dto.getMessage())
                .timestamp(LocalDateTime.now())
                .read(dto.isRead());

        if (dto.getUserId() != null) {
            User user = userRepo.findById(dto.getUserId()).orElse(null);
            builder.user(user);
        }

        if (dto.getRoomId() != null) {
            Room room = roomRepo.findById(dto.getRoomId()).orElse(null);
            builder.room(room);
        }

        if (dto.getTimetableId() != null) {
            Timetable timetable = timetableRepo.findById(dto.getTimetableId()).orElse(null);
            builder.timetable(timetable);
        }

        return builder.build();
    }

    @Scheduled(cron = "0 0 2 * * *") // Every day at 2 AM
    @Transactional
    public void deleteOldNotifications() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(2);
        notificationRepo.deleteNotificationsOlderThan(cutoff);
    }
}