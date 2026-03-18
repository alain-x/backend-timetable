package com.digital_timetable.controller;

import com.digital_timetable.dto.SwapRequestDto;
import com.digital_timetable.dto.Response;
import com.digital_timetable.service.interf.SwapRequestService;
import com.digital_timetable.service.interf.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/swap-requests")
@PreAuthorize("hasAnyRole('LECTURER', 'CLASS_REPRESENT', 'ADMIN')")
public class SwapRequestController {

    @Autowired
    private SwapRequestService swapRequestService;
    
    @Autowired
    private NotificationService notificationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('LECTURER', 'CLASS_REPRESENT')")
    public ResponseEntity<Response> createSwapRequest(@RequestBody SwapRequestDto swapRequestDto) {
        try {
            SwapRequestDto createdRequest = swapRequestService.createSwapRequest(swapRequestDto);
            
            // Send notification to target user
            notificationService.createRequestNotification(
                "New Swap Request",
                "You have received a new swap request from " + createdRequest.getRequestorName(),
                createdRequest.getTargetUserId()
            );
            
            Response response = Response.builder()
                    .status(HttpStatus.CREATED.value())
                    .message("Swap request created successfully")
                    .data(createdRequest)
                    .build();
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Response response = Response.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to create swap request: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('LECTURER', 'CLASS_REPRESENT', 'ADMIN')")
    public ResponseEntity<Response> getAllSwapRequests() {
        try {
            List<SwapRequestDto> requests = swapRequestService.getAllSwapRequests();
            Response response = Response.builder()
                    .status(HttpStatus.OK.value())
                    .message("Swap requests retrieved successfully")
                    .data(requests)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Response response = Response.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to retrieve swap requests: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/requestor/{requestorId}")
    @PreAuthorize("hasAnyRole('LECTURER', 'CLASS_REPRESENT', 'ADMIN')")
    public ResponseEntity<Response> getSwapRequestsByRequestor(@PathVariable Long requestorId) {
        try {
            List<SwapRequestDto> requests = swapRequestService.getSwapRequestsByRequestor(requestorId);
            Response response = Response.builder()
                    .status(HttpStatus.OK.value())
                    .message("Swap requests retrieved successfully")
                    .data(requests)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Response response = Response.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to retrieve swap requests: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/target/{targetUserId}")
    @PreAuthorize("hasAnyRole('LECTURER', 'CLASS_REPRESENT', 'ADMIN')")
    public ResponseEntity<Response> getSwapRequestsByTarget(@PathVariable Long targetUserId) {
        try {
            List<SwapRequestDto> requests = swapRequestService.getSwapRequestsByTarget(targetUserId);
            Response response = Response.builder()
                    .status(HttpStatus.OK.value())
                    .message("Swap requests retrieved successfully")
                    .data(requests)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Response response = Response.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to retrieve swap requests: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response> getSwapRequestsByStatus(@PathVariable String status) {
        try {
            List<SwapRequestDto> requests = swapRequestService.getSwapRequestsByStatus(status);
            Response response = Response.builder()
                    .status(HttpStatus.OK.value())
                    .message("Swap requests retrieved successfully")
                    .data(requests)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Response response = Response.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to retrieve swap requests: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('LECTURER', 'CLASS_REPRESENT', 'ADMIN')")
    public ResponseEntity<Response> getSwapRequestById(@PathVariable Long id) {
        try {
            SwapRequestDto request = swapRequestService.getSwapRequestById(id);
            if (request != null) {
                Response response = Response.builder()
                        .status(HttpStatus.OK.value())
                        .message("Swap request retrieved successfully")
                        .data(request)
                        .build();
                return ResponseEntity.ok(response);
            } else {
                Response response = Response.builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .message("Swap request not found")
                        .build();
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            Response response = Response.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to retrieve swap request: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('LECTURER', 'CLASS_REPRESENT')")
    public ResponseEntity<Response> approveSwapRequest(@PathVariable Long id, @RequestParam String notes) {
        try {
            SwapRequestDto request = swapRequestService.approveSwapRequest(id, notes);
            
            // Send notification to requestor
            notificationService.createRequestNotification(
                "Swap Request Approved",
                "Your swap request has been approved by " + request.getTargetUserName(),
                request.getRequestorId()
            );
            
            Response response = Response.builder()
                    .status(HttpStatus.OK.value())
                    .message("Swap request approved successfully")
                    .data(request)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Response response = Response.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to approve swap request: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('LECTURER', 'CLASS_REPRESENT')")
    public ResponseEntity<Response> rejectSwapRequest(@PathVariable Long id, @RequestParam String reason) {
        try {
            SwapRequestDto request = swapRequestService.rejectSwapRequest(id, reason);
            
            // Send notification to requestor
            notificationService.createRequestNotification(
                "Swap Request Rejected",
                "Your swap request has been rejected by " + request.getTargetUserName() + ". Reason: " + reason,
                request.getRequestorId()
            );
            
            Response response = Response.builder()
                    .status(HttpStatus.OK.value())
                    .message("Swap request rejected successfully")
                    .data(request)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Response response = Response.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to reject swap request: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/{id}/admin-approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response> adminApproveSwapRequest(@PathVariable Long id, @RequestParam String adminNotes) {
        try {
            SwapRequestDto request = swapRequestService.adminApproveSwapRequest(id, adminNotes);
            
            // Build detailed message with original/proposed schedules, reason and status
            String detailedMessage = new StringBuilder()
                .append("Original Schedule:\n")
                .append(String.valueOf(request.getOriginalTimetableInfo()))
                .append("\n\nProposed Schedule:\n")
                .append(String.valueOf(request.getProposedTimetableInfo()))
                .append("\n\nReason:\n")
                .append(String.valueOf(request.getReason() != null ? request.getReason() : ""))
                .append("\n\nStatus:\n")
                .append(String.valueOf(request.getStatus()))
                .toString();

            // Send detailed notifications to both users
            notificationService.createRequestNotification(
                "Swap Request Admin Approved",
                detailedMessage,
                request.getRequestorId()
            );
            
            notificationService.createRequestNotification(
                "Swap Request Admin Approved",
                detailedMessage,
                request.getTargetUserId()
            );
            
            Response response = Response.builder()
                    .status(HttpStatus.OK.value())
                    .message("Swap request approved by admin successfully")
                    .data(request)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Response response = Response.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to approve swap request: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/{id}/admin-reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response> adminRejectSwapRequest(@PathVariable Long id, @RequestParam String adminNotes) {
        try {
            SwapRequestDto request = swapRequestService.adminRejectSwapRequest(id, adminNotes);
            
            // Send notifications to both users
            notificationService.createRequestNotification(
                "Swap Request Admin Rejected",
                "Your swap request has been rejected by admin. Reason: " + adminNotes,
                request.getRequestorId()
            );
            
            notificationService.createRequestNotification(
                "Swap Request Admin Rejected",
                "A swap request involving you has been rejected by admin",
                request.getTargetUserId()
            );
            
            Response response = Response.builder()
                    .status(HttpStatus.OK.value())
                    .message("Swap request rejected by admin successfully")
                    .data(request)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Response response = Response.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to reject swap request: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('LECTURER', 'CLASS_REPRESENT', 'ADMIN')")
    public ResponseEntity<Response> cancelSwapRequest(@PathVariable Long id) {
        try {
            SwapRequestDto request = swapRequestService.cancelSwapRequest(id);
            
            // Send notification to target user
            notificationService.createRequestNotification(
                "Swap Request Cancelled",
                "A swap request from " + request.getRequestorName() + " has been cancelled",
                request.getTargetUserId()
            );
            
            Response response = Response.builder()
                    .status(HttpStatus.OK.value())
                    .message("Swap request cancelled successfully")
                    .data(request)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Response response = Response.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to cancel swap request: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/{id}/execute")
    @PreAuthorize("hasAnyRole('LECTURER', 'CLASS_REPRESENT', 'STAFF', 'ADMIN')")
    public ResponseEntity<Response> executeSwap(@PathVariable Long id) {
        try {
            boolean success = swapRequestService.executeSwap(id);
            if (success) {
                // Send notification about successful swap execution
                notificationService.createSystemNotification(
                    "Swap Executed",
                    "A timetable swap has been successfully executed"
                );
                
                Response response = Response.builder()
                        .status(HttpStatus.OK.value())
                        .message("Swap executed successfully")
                        .build();
                return ResponseEntity.ok(response);
            } else {
                Response response = Response.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message("Failed to execute swap - conflicts detected")
                        .build();
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            Response response = Response.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to execute swap: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/pending-count/{userId}")
    @PreAuthorize("hasAnyRole('LECTURER', 'CLASS_REPRESENT', 'ADMIN')")
    public ResponseEntity<Response> getPendingRequestsCount(@PathVariable Long userId) {
        try {
            Long count = swapRequestService.getPendingRequestsCount(userId);
            Response response = Response.builder()
                    .status(HttpStatus.OK.value())
                    .message("Pending requests count retrieved successfully")
                    .data(count)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Response response = Response.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to retrieve pending requests count: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/pending-target-count/{userId}")
    @PreAuthorize("hasAnyRole('LECTURER', 'CLASS_REPRESENT', 'ADMIN')")
    public ResponseEntity<Response> getPendingTargetRequestsCount(@PathVariable Long userId) {
        try {
            Long count = swapRequestService.getPendingTargetRequestsCount(userId);
            Response response = Response.builder()
                    .status(HttpStatus.OK.value())
                    .message("Pending target requests count retrieved successfully")
                    .data(count)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Response response = Response.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to retrieve pending target requests count: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{id}/validate")
    @PreAuthorize("hasAnyRole('LECTURER', 'CLASS_REPRESENT', 'ADMIN')")
    public ResponseEntity<Response> validateSwapRequest(@PathVariable Long id) {
        try {
            boolean isValid = swapRequestService.isSwapRequestValid(id);
            Response response = Response.builder()
                    .status(HttpStatus.OK.value())
                    .message("Swap request validation completed")
                    .data(isValid)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Response response = Response.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to validate swap request: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
} 