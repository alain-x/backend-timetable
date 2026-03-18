package com.digital_timetable.service.interf;

import com.digital_timetable.dto.TimetableDto;
import java.util.List;

public interface TimetableService {
    TimetableDto createTimetable(TimetableDto timetableDto);
    TimetableDto getTimetableById(Long id);
    List<TimetableDto> getAllTimetables();
    List<TimetableDto> getTimetablesByLecturer(Long lecturerId);
    TimetableDto updateTimetable(Long id, TimetableDto timetableDto);
    void deleteTimetable(Long id);
    TimetableDto startClass(Long id);
    TimetableDto endClass(Long id);
    TimetableDto assignClassRep(Long timetableId, Long userId);
} 