package com.digital_timetable.repository;

import com.digital_timetable.entity.SwapRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SwapRequestRepo extends JpaRepository<SwapRequest, Long> {

    @Query("SELECT sr FROM SwapRequest sr WHERE sr.requestor.id = :userId ORDER BY sr.createdAt DESC")
    List<SwapRequest> findRequestsByRequestorId(@Param("userId") Long userId);

    @Query("SELECT sr FROM SwapRequest sr WHERE sr.targetUser.id = :userId ORDER BY sr.createdAt DESC")
    List<SwapRequest> findRequestsByTargetUserId(@Param("userId") Long userId);

    @Query("SELECT sr FROM SwapRequest sr WHERE sr.status = :status ORDER BY sr.createdAt DESC")
    List<SwapRequest> findRequestsByStatus(@Param("status") SwapRequest.SwapStatus status);

    @Query("SELECT sr FROM SwapRequest sr WHERE sr.status = 'PENDING' AND sr.requestDate < :expiryDate")
    List<SwapRequest> findExpiredRequests(@Param("expiryDate") LocalDateTime expiryDate);

    @Query("SELECT COUNT(sr) FROM SwapRequest sr WHERE sr.requestor.id = :userId AND sr.status = 'PENDING'")
    Long countPendingRequestsByRequestor(@Param("userId") Long userId);

    @Query("SELECT COUNT(sr) FROM SwapRequest sr WHERE sr.targetUser.id = :userId AND sr.status = 'PENDING'")
    Long countPendingRequestsByTarget(@Param("userId") Long userId);

    @Query("SELECT sr FROM SwapRequest sr WHERE sr.originalTimetable.id = :timetableId OR sr.proposedTimetable.id = :timetableId")
    List<SwapRequest> findRequestsByTimetableId(@Param("timetableId") Long timetableId);

    @Query("SELECT sr FROM SwapRequest sr ORDER BY sr.createdAt DESC")
    List<SwapRequest> findAllOrderByCreatedAtDesc();
} 