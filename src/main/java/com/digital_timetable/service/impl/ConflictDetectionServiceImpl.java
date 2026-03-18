package com.digital_timetable.service.impl;

import com.digital_timetable.dto.TimetableDto;
import com.digital_timetable.entity.Timetable;
import com.digital_timetable.repository.TimetableRepo;
import com.digital_timetable.service.interf.ConflictDetectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ConflictDetectionServiceImpl implements ConflictDetectionService {

    @Autowired
    private TimetableRepo timetableRepo;

    @Override
    public List<Timetable> checkRoomConflicts(Long roomId, LocalDateTime startTime, LocalDateTime endTime, Long excludeTimetableId) {
        List<Timetable> allTimetables = timetableRepo.findAll();
        
        return allTimetables.stream()
                .filter(t -> t.getRoom_name() != null && t.getRoom_name().equals(roomId.toString()))
                .filter(t -> !t.getId().equals(excludeTimetableId))
                .filter(t -> hasTimeOverlap(
                    t.getStartDateTime(), 
                    t.getEndDateTime(), 
                    startTime, 
                    endTime
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<Timetable> checkLecturerConflicts(String lecturerName, LocalDateTime startTime, LocalDateTime endTime, Long excludeTimetableId) {
        List<Timetable> allTimetables = timetableRepo.findAll();
        
        return allTimetables.stream()
                .filter(t -> t.getLecture_name() != null && t.getLecture_name().equals(lecturerName))
                .filter(t -> !t.getId().equals(excludeTimetableId))
                .filter(t -> hasTimeOverlap(
                    t.getStartDateTime(), 
                    t.getEndDateTime(), 
                    startTime, 
                    endTime
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<Timetable> checkClassRepConflicts(Long classRepId, LocalDateTime startTime, LocalDateTime endTime, Long excludeTimetableId) {
        List<Timetable> allTimetables = timetableRepo.findAll();
        
        return allTimetables.stream()
                .filter(t -> t.getClassRep() != null && t.getClassRep().getId() != null && t.getClassRep().getId().equals(classRepId))
                .filter(t -> !t.getId().equals(excludeTimetableId))
                .filter(t -> hasTimeOverlap(
                    t.getStartDateTime(), 
                    t.getEndDateTime(), 
                    startTime, 
                    endTime
                ))
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, List<Timetable>> checkAllConflicts(TimetableDto timetableDto) {
        Map<String, List<Timetable>> conflicts = new HashMap<>();
        
        LocalDateTime startTime = parseDateTime(timetableDto.getStartDateTime());
        LocalDateTime endTime = parseDateTime(timetableDto.getEndDateTime());
        
        // Check room conflicts
        if (timetableDto.getRoom_name() != null) {
            List<Timetable> roomConflicts = checkRoomConflicts(
                Long.parseLong(timetableDto.getRoom_name()), 
                startTime, 
                endTime, 
                timetableDto.getId()
            )
            .stream()
            .filter(t -> {
                var existingSection = t.getSection();
                var newSection = timetableDto.getSection();
                return (existingSection != null && newSection != null)
                    ? existingSection == newSection
                    : true; // if unknown, treat as same to be safe
            })
            .collect(Collectors.toList());
            if (!roomConflicts.isEmpty()) {
                conflicts.put("room", roomConflicts);
            }
        }
        
        // Check lecturer conflicts
        if (timetableDto.getLecture_name() != null) {
            List<Timetable> lecturerConflicts = checkLecturerConflicts(
                timetableDto.getLecture_name(), 
                startTime, 
                endTime, 
                timetableDto.getId()
            )
            .stream()
            .filter(t -> {
                var existingSection = t.getSection();
                var newSection = timetableDto.getSection();
                return (existingSection != null && newSection != null)
                    ? existingSection == newSection
                    : true;
            })
            .collect(Collectors.toList());
            if (!lecturerConflicts.isEmpty()) {
                conflicts.put("lecturer", lecturerConflicts);
            }
        }
        
        // Check class rep conflicts
        if (timetableDto.getClassRepUserId() != null) {
            List<Timetable> classRepConflicts = checkClassRepConflicts(
                timetableDto.getClassRepUserId(), 
                startTime, 
                endTime, 
                timetableDto.getId()
            );
            if (!classRepConflicts.isEmpty()) {
                conflicts.put("classRep", classRepConflicts);
            }
        }
        
        return conflicts;
    }

    @Override
    public Map<String, List<Timetable>> checkSwapConflicts(Long originalTimetableId, Long proposedTimetableId) {
        Map<String, List<Timetable>> conflicts = new HashMap<>();
        
        // Get the timetables involved in the swap
        Timetable originalTimetable = timetableRepo.findById(originalTimetableId).orElse(null);
        Timetable proposedTimetable = timetableRepo.findById(proposedTimetableId).orElse(null);
        
        if (originalTimetable == null || proposedTimetable == null) {
            return conflicts;
        }
        
        // Check if swapping would create conflicts
        LocalDateTime originalStart = originalTimetable.getStartDateTime();
        LocalDateTime originalEnd = originalTimetable.getEndDateTime();
        LocalDateTime proposedStart = proposedTimetable.getStartDateTime();
        LocalDateTime proposedEnd = proposedTimetable.getEndDateTime();
        
        // Check room conflicts for the swap
        if (originalTimetable.getRoom_name() != null && proposedTimetable.getRoom_name() != null) {
            List<Timetable> roomConflicts = checkRoomConflicts(
                Long.parseLong(originalTimetable.getRoom_name()), 
                proposedStart, 
                proposedEnd, 
                proposedTimetableId
            );
            if (!roomConflicts.isEmpty()) {
                conflicts.put("room", roomConflicts);
            }
        }
        
        // Check lecturer conflicts for the swap
        if (originalTimetable.getLecture_name() != null && proposedTimetable.getLecture_name() != null) {
            List<Timetable> lecturerConflicts = checkLecturerConflicts(
                originalTimetable.getLecture_name(), 
                proposedStart, 
                proposedEnd, 
                proposedTimetableId
            );
            if (!lecturerConflicts.isEmpty()) {
                conflicts.put("lecturer", lecturerConflicts);
            }
        }
        
        return conflicts;
    }

    @Override
    public boolean isChangePossible(TimetableDto timetableDto) {
        Map<String, List<Timetable>> conflicts = checkAllConflicts(timetableDto);
        return conflicts.isEmpty();
    }

    @Override
    public Map<String, List<Timetable>> getConflictsInTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, List<Timetable>> conflicts = new HashMap<>();
        List<Timetable> allTimetables = timetableRepo.findAll();
        
        // Group conflicts by type
        List<Timetable> roomConflicts = new ArrayList<>();
        List<Timetable> lecturerConflicts = new ArrayList<>();
        List<Timetable> classRepConflicts = new ArrayList<>();
        
        for (Timetable timetable : allTimetables) {
            LocalDateTime timetableStart = timetable.getStartDateTime();
            LocalDateTime timetableEnd = timetable.getEndDateTime();
            
            if (hasTimeOverlap(timetableStart, timetableEnd, startTime, endTime)) {
                // Check for overlapping timetables in the same room
                List<Timetable> overlappingInRoom = allTimetables.stream()
                    .filter(t -> t.getRoom_name() != null && t.getRoom_name().equals(timetable.getRoom_name()))
                    .filter(t -> !t.getId().equals(timetable.getId()))
                    .filter(t -> hasTimeOverlap(
                        t.getStartDateTime(), 
                        t.getEndDateTime(), 
                        timetableStart, 
                        timetableEnd
                    ))
                    .filter(t -> {
                        var s1 = t.getSection();
                        var s2 = timetable.getSection();
                        return (s1 != null && s2 != null) ? s1 == s2 : true;
                    })
                    .collect(Collectors.toList());
                
                if (!overlappingInRoom.isEmpty()) {
                    roomConflicts.addAll(overlappingInRoom);
                }
                
                // Check for overlapping timetables with the same lecturer
                List<Timetable> overlappingLecturer = allTimetables.stream()
                    .filter(t -> t.getLecture_name() != null && t.getLecture_name().equals(timetable.getLecture_name()))
                    .filter(t -> !t.getId().equals(timetable.getId()))
                    .filter(t -> hasTimeOverlap(
                        t.getStartDateTime(), 
                        t.getEndDateTime(), 
                        timetableStart, 
                        timetableEnd
                    ))
                    .filter(t -> {
                        var s1 = t.getSection();
                        var s2 = timetable.getSection();
                        return (s1 != null && s2 != null) ? s1 == s2 : true;
                    })
                    .collect(Collectors.toList());
                
                if (!overlappingLecturer.isEmpty()) {
                    lecturerConflicts.addAll(overlappingLecturer);
                }
            }
        }
        
        if (!roomConflicts.isEmpty()) conflicts.put("room", roomConflicts);
        if (!lecturerConflicts.isEmpty()) conflicts.put("lecturer", lecturerConflicts);
        if (!classRepConflicts.isEmpty()) conflicts.put("classRep", classRepConflicts);
        
        return conflicts;
    }

    @Override
    public List<LocalDateTime> suggestAlternativeSlots(Long roomId, String lecturerName, LocalDateTime preferredStartTime, LocalDateTime preferredEndTime) {
        List<LocalDateTime> suggestions = new ArrayList<>();
        
        // Define working hours (8 AM to 6 PM)
        LocalTime workStart = LocalTime.of(8, 0);
        LocalTime workEnd = LocalTime.of(18, 0);
        
        // Check next 7 days for available slots
        for (int day = 0; day < 7; day++) {
            LocalDateTime checkDate = preferredStartTime.plusDays(day);
            
            // Check each hour within working hours
            for (int hour = workStart.getHour(); hour < workEnd.getHour(); hour++) {
                LocalDateTime slotStart = checkDate.withHour(hour).withMinute(0);
                LocalDateTime slotEnd = slotStart.plusHours(1);
                
                // Check if this slot is available
                List<Timetable> roomConflicts = checkRoomConflicts(roomId, slotStart, slotEnd, null);
                List<Timetable> lecturerConflicts = checkLecturerConflicts(lecturerName, slotStart, slotEnd, null);
                
                if (roomConflicts.isEmpty() && lecturerConflicts.isEmpty()) {
                    suggestions.add(slotStart);
                }
            }
        }
        
        return suggestions.stream()
                .limit(10) // Return max 10 suggestions
                .collect(Collectors.toList());
    }

    private boolean hasTimeOverlap(LocalDateTime start1, LocalDateTime end1, LocalDateTime start2, LocalDateTime end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    private LocalDateTime parseDateTime(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.isEmpty()) {
            return LocalDateTime.now();
        }
        try {
            return LocalDateTime.parse(dateTimeString);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }
} 