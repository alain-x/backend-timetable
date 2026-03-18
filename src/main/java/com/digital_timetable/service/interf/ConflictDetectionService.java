package com.digital_timetable.service.interf;

import com.digital_timetable.entity.Timetable;
import com.digital_timetable.dto.TimetableDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ConflictDetectionService {
    
    /**
     * Check for room conflicts at a specific time
     */
    List<Timetable> checkRoomConflicts(Long roomId, LocalDateTime startTime, LocalDateTime endTime, Long excludeTimetableId);
    
    /**
     * Check for lecturer conflicts at a specific time
     */
    List<Timetable> checkLecturerConflicts(String lecturerName, LocalDateTime startTime, LocalDateTime endTime, Long excludeTimetableId);
    
    /**
     * Check for class representative conflicts at a specific time
     */
    List<Timetable> checkClassRepConflicts(Long classRepId, LocalDateTime startTime, LocalDateTime endTime, Long excludeTimetableId);
    
    /**
     * Comprehensive conflict check for a timetable entry
     */
    Map<String, List<Timetable>> checkAllConflicts(TimetableDto timetableDto);
    
    /**
     * Check if a swap request would create conflicts
     */
    Map<String, List<Timetable>> checkSwapConflicts(Long originalTimetableId, Long proposedTimetableId);
    
    /**
     * Validate if a timetable change is possible without conflicts
     */
    boolean isChangePossible(TimetableDto timetableDto);
    
    /**
     * Get all conflicts for a specific time range
     */
    Map<String, List<Timetable>> getConflictsInTimeRange(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Suggest alternative time slots for a conflicted timetable
     */
    List<LocalDateTime> suggestAlternativeSlots(Long roomId, String lecturerName, LocalDateTime preferredStartTime, LocalDateTime preferredEndTime);
} 