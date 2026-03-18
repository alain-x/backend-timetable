package com.digital_timetable.service.interf;

import com.digital_timetable.dto.SwapRequestDto;

import java.util.List;

public interface SwapRequestService {
    
    SwapRequestDto createSwapRequest(SwapRequestDto swapRequestDto);
    
    List<SwapRequestDto> getAllSwapRequests();
    
    List<SwapRequestDto> getSwapRequestsByRequestor(Long requestorId);
    
    List<SwapRequestDto> getSwapRequestsByTarget(Long targetUserId);
    
    List<SwapRequestDto> getSwapRequestsByStatus(String status);
    
    SwapRequestDto getSwapRequestById(Long id);
    
    SwapRequestDto approveSwapRequest(Long id, String notes);
    
    SwapRequestDto rejectSwapRequest(Long id, String reason);
    
    SwapRequestDto adminApproveSwapRequest(Long id, String adminNotes);
    
    SwapRequestDto adminRejectSwapRequest(Long id, String adminNotes);
    
    SwapRequestDto cancelSwapRequest(Long id);
    
    boolean executeSwap(Long swapRequestId);
    
    Long getPendingRequestsCount(Long userId);
    
    Long getPendingTargetRequestsCount(Long userId);
    
    boolean isSwapRequestValid(Long swapRequestId);
} 