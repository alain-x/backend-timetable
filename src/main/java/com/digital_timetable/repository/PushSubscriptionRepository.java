package com.digital_timetable.repository;

import com.digital_timetable.entity.PushSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {
    List<PushSubscription> findByUserId(Long userId);
    PushSubscription findByEndpoint(String endpoint);
}
