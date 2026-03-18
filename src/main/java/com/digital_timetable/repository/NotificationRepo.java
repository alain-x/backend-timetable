package com.digital_timetable.repository;

import com.digital_timetable.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepo extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM Notification n WHERE n.read = false ORDER BY n.timestamp DESC")
    List<Notification> findUnreadNotifications();

    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId ORDER BY n.timestamp DESC")
    List<Notification> findNotificationsByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.read = false")
    Long countUnreadNotifications();

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId AND n.read = false")
    Long countUnreadNotificationsByUserId(@Param("userId") Long userId);

    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.read = false ORDER BY n.timestamp DESC")
    List<Notification> findUnreadNotificationsByUserId(@Param("userId") Long userId);

    @Query("SELECT n FROM Notification n ORDER BY n.timestamp DESC")
    List<Notification> findAllOrderByTimestampDesc();

    @Query("SELECT n FROM Notification n WHERE n.user.role = :role ORDER BY n.timestamp DESC")
    List<Notification> findNotificationsByUserRole(@Param("role") String role);

    @Query("SELECT n FROM Notification n WHERE n.user.role = :role AND n.read = false ORDER BY n.timestamp DESC")
    List<Notification> findUnreadNotificationsByUserRole(@Param("role") String role);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user.role = :role AND n.read = false")
    Long countUnreadNotificationsByUserRole(@Param("role") String role);

    @Query("DELETE FROM Notification n WHERE n.timestamp < :cutoff")
    @Modifying
    void deleteNotificationsOlderThan(@Param("cutoff") java.time.LocalDateTime cutoff);
} 