package com.digital_timetable.service.interf;

import com.digital_timetable.dto.FacultyDto;
import java.util.List;

public interface FacultyService {
    FacultyDto createFaculty(FacultyDto facultyDto);
    FacultyDto getFacultyById(Long id);
    List<FacultyDto> getAllFaculties();
    FacultyDto updateFaculty(Long id, FacultyDto facultyDto);
    void deleteFaculty(Long id);
} 