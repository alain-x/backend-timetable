package com.digital_timetable.service.interf;

import com.digital_timetable.dto.CourseDto;
import java.util.List;

public interface CourseService {
    CourseDto createCourse(CourseDto courseDto);
    CourseDto getCourseById(Long id);
    List<CourseDto> getAllCourses();
    CourseDto updateCourse(Long id, CourseDto courseDto);
    void deleteCourse(Long id);
} 