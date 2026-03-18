package com.digital_timetable.service.impl;

import com.digital_timetable.dto.SwapRequestDto;
import com.digital_timetable.entity.SwapRequest;
import com.digital_timetable.entity.Timetable;
import com.digital_timetable.entity.User;
import com.digital_timetable.repository.SwapRequestRepo;
import com.digital_timetable.repository.TimetableRepo;
import com.digital_timetable.repository.UserRepo;
import com.digital_timetable.service.interf.SwapRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SwapRequestServiceImpl implements SwapRequestService {

    @Autowired
    private SwapRequestRepo swapRequestRepo;

    @Autowired
    private TimetableRepo timetableRepo;

    @Autowired
    private UserRepo userRepo;

    @Override
    public SwapRequestDto createSwapRequest(SwapRequestDto swapRequestDto) {
        User requestor = userRepo.findById(swapRequestDto.getRequestorId()).orElse(null);
        User targetUser = userRepo.findById(swapRequestDto.getTargetUserId()).orElse(null);
        Timetable originalTimetable = timetableRepo.findById(swapRequestDto.getOriginalTimetableId()).orElse(null);
        Timetable proposedTimetable = timetableRepo.findById(swapRequestDto.getProposedTimetableId()).orElse(null);

        if (requestor == null) {
            throw new RuntimeException("Invalid request data: requestor not found (ID: " + swapRequestDto.getRequestorId() + ")");
        }
        if (targetUser == null) {
            throw new RuntimeException("Invalid request data: target user not found (ID: " + swapRequestDto.getTargetUserId() + ")");
        }
        if (originalTimetable == null) {
            throw new RuntimeException("Invalid request data: original timetable not found (ID: " + swapRequestDto.getOriginalTimetableId() + ")");
        }
        if (proposedTimetable == null) {
            throw new RuntimeException("Invalid request data: proposed timetable not found (ID: " + swapRequestDto.getProposedTimetableId() + ")");
        }

        SwapRequest swapRequest = SwapRequest.builder()
                .requestor(requestor)
                .targetUser(targetUser)
                .originalTimetable(originalTimetable)
                .proposedTimetable(proposedTimetable)
                .reason(swapRequestDto.getReason())
                .status(SwapRequest.SwapStatus.PENDING)
                .requestDate(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        SwapRequest savedRequest = swapRequestRepo.save(swapRequest);
        return convertToDto(savedRequest);
    }

    @Override
    public List<SwapRequestDto> getAllSwapRequests() {
        return swapRequestRepo.findAllOrderByCreatedAtDesc()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SwapRequestDto> getSwapRequestsByRequestor(Long requestorId) {
        return swapRequestRepo.findRequestsByRequestorId(requestorId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SwapRequestDto> getSwapRequestsByTarget(Long targetUserId) {
        return swapRequestRepo.findRequestsByTargetUserId(targetUserId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SwapRequestDto> getSwapRequestsByStatus(String status) {
        SwapRequest.SwapStatus swapStatus = SwapRequest.SwapStatus.valueOf(status.toUpperCase());
        return swapRequestRepo.findRequestsByStatus(swapStatus)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public SwapRequestDto getSwapRequestById(Long id) {
        SwapRequest swapRequest = swapRequestRepo.findById(id).orElse(null);
        return swapRequest != null ? convertToDto(swapRequest) : null;
    }

    @Override
    public SwapRequestDto approveSwapRequest(Long id, String notes) {
        SwapRequest swapRequest = swapRequestRepo.findById(id).orElse(null);
        if (swapRequest == null) {
            throw new RuntimeException("Swap request not found");
        }

        swapRequest.setStatus(SwapRequest.SwapStatus.APPROVED);
        swapRequest.setResponseDate(LocalDateTime.now());
        swapRequest.setAdminNotes(notes);

        SwapRequest savedRequest = swapRequestRepo.save(swapRequest);
        return convertToDto(savedRequest);
    }

    @Override
    public SwapRequestDto rejectSwapRequest(Long id, String reason) {
        SwapRequest swapRequest = swapRequestRepo.findById(id).orElse(null);
        if (swapRequest == null) {
            throw new RuntimeException("Swap request not found");
        }

        swapRequest.setStatus(SwapRequest.SwapStatus.REJECTED);
        swapRequest.setResponseDate(LocalDateTime.now());
        swapRequest.setAdminNotes(reason);

        SwapRequest savedRequest = swapRequestRepo.save(swapRequest);
        return convertToDto(savedRequest);
    }

    @Override
    public SwapRequestDto adminApproveSwapRequest(Long id, String adminNotes) {
        SwapRequest swapRequest = swapRequestRepo.findById(id).orElse(null);
        if (swapRequest == null) {
            throw new RuntimeException("Swap request not found");
        }

        swapRequest.setStatus(SwapRequest.SwapStatus.ADMIN_APPROVED);
        swapRequest.setResponseDate(LocalDateTime.now());
        swapRequest.setAdminNotes(adminNotes);

        SwapRequest savedRequest = swapRequestRepo.save(swapRequest);
        return convertToDto(savedRequest);
    }

    @Override
    public SwapRequestDto adminRejectSwapRequest(Long id, String adminNotes) {
        SwapRequest swapRequest = swapRequestRepo.findById(id).orElse(null);
        if (swapRequest == null) {
            throw new RuntimeException("Swap request not found");
        }

        swapRequest.setStatus(SwapRequest.SwapStatus.ADMIN_REJECTED);
        swapRequest.setResponseDate(LocalDateTime.now());
        swapRequest.setAdminNotes(adminNotes);

        SwapRequest savedRequest = swapRequestRepo.save(swapRequest);
        return convertToDto(savedRequest);
    }

    @Override
    public SwapRequestDto cancelSwapRequest(Long id) {
        SwapRequest swapRequest = swapRequestRepo.findById(id).orElse(null);
        if (swapRequest == null) {
            throw new RuntimeException("Swap request not found");
        }

        swapRequest.setStatus(SwapRequest.SwapStatus.CANCELLED);
        swapRequest.setResponseDate(LocalDateTime.now());

        SwapRequest savedRequest = swapRequestRepo.save(swapRequest);
        return convertToDto(savedRequest);
    }

    @Override
    public boolean executeSwap(Long swapRequestId) {
        SwapRequest swapRequest = swapRequestRepo.findById(swapRequestId).orElse(null);
        if (swapRequest == null || swapRequest.getStatus() != SwapRequest.SwapStatus.ADMIN_APPROVED) {
            return false;
        }

        try {
            // Get the timetables
            Timetable originalTimetable = swapRequest.getOriginalTimetable();
            Timetable proposedTimetable = swapRequest.getProposedTimetable();

            // Swap the timetables
            LocalDateTime originalStart = originalTimetable.getStartDateTime();
            LocalDateTime originalEnd = originalTimetable.getEndDateTime();

            originalTimetable.setStartDateTime(proposedTimetable.getStartDateTime());
            originalTimetable.setEndDateTime(proposedTimetable.getEndDateTime());

            proposedTimetable.setStartDateTime(originalStart);
            proposedTimetable.setEndDateTime(originalEnd);

            // Save the changes
            timetableRepo.save(originalTimetable);
            timetableRepo.save(proposedTimetable);

            // Update swap request status
            swapRequest.setStatus(SwapRequest.SwapStatus.EXPIRED);
            swapRequestRepo.save(swapRequest);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Long getPendingRequestsCount(Long userId) {
        return swapRequestRepo.countPendingRequestsByRequestor(userId);
    }

    @Override
    public Long getPendingTargetRequestsCount(Long userId) {
        return swapRequestRepo.countPendingRequestsByTarget(userId);
    }

    @Override
    public boolean isSwapRequestValid(Long swapRequestId) {
        SwapRequest swapRequest = swapRequestRepo.findById(swapRequestId).orElse(null);
        if (swapRequest == null) {
            return false;
        }

        // Check if the swap request is in a valid state for execution
        return swapRequest.getStatus() == SwapRequest.SwapStatus.ADMIN_APPROVED;
    }

    private SwapRequestDto convertToDto(SwapRequest swapRequest) {
        return new SwapRequestDto(
                swapRequest.getId(),
                swapRequest.getRequestor().getId(),
                swapRequest.getRequestor().getName(),
                swapRequest.getTargetUser().getId(),
                swapRequest.getTargetUser().getName(),
                swapRequest.getOriginalTimetable().getId(),
                formatTimetableInfo(swapRequest.getOriginalTimetable()),
                swapRequest.getProposedTimetable().getId(),
                formatTimetableInfo(swapRequest.getProposedTimetable()),
                swapRequest.getStatus().toString(),
                swapRequest.getReason(),
                swapRequest.getAdminNotes(),
                swapRequest.getRequestDate().toString(),
                swapRequest.getResponseDate() != null ? swapRequest.getResponseDate().toString() : null,
                swapRequest.getCreatedAt().toString()
        );
    }

    private String formatTimetableInfo(Timetable timetable) {
        return String.format("%s - %s (%s) - %s to %s",
                timetable.getCourse_name(),
                timetable.getLecture_name(),
                timetable.getRoom_name(),
                timetable.getStartDateTime(),
                timetable.getEndDateTime()
        );
    }
} 