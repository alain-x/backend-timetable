package com.digital_timetable.repository;

import com.digital_timetable.entity.DismissedNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DismissedNotificationRepo extends JpaRepository<DismissedNotification, Long> {
    List<DismissedNotification> findByUserId(Long userId);
    boolean existsByUserIdAndNotificationId(Long userId, Long notificationId);
    void deleteByNotificationId(Long notificationId);
}