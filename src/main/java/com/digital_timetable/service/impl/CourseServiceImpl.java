package com.digital_timetable.service.impl;

import com.digital_timetable.dto.CourseDto;
import com.digital_timetable.entity.Course;
import com.digital_timetable.entity.Faculty;
import com.digital_timetable.entity.Department;
import com.digital_timetable.entity.Intake;
import com.digital_timetable.mapper.EntityDtoMapper;
import com.digital_timetable.repository.CourseRepo;
import com.digital_timetable.repository.FacultyRepo;
import com.digital_timetable.repository.DepartmentRepo;
import com.digital_timetable.repository.IntakeRepository;
import com.digital_timetable.service.interf.CourseService;
import com.digital_timetable.service.interf.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CourseServiceImpl implements CourseService {
    @Autowired
    private CourseRepo courseRepo;
    
    @Autowired
    private FacultyRepo facultyRepo;
    
    @Autowired
    private DepartmentRepo departmentRepo;

    @Autowired
    private IntakeRepository intakeRepository;
    
    @Autowired
    private EntityDtoMapper mapper;
    
    @Autowired
    private NotificationService notificationService;

    @Override
    public CourseDto createCourse(CourseDto courseDto) {
        Course course = mapper.mapDtoToCourse(courseDto);
        
        // Set faculty relationship if ID is provided
        if (courseDto.getFacultyId() != null) {
            Faculty faculty = facultyRepo.findById(courseDto.getFacultyId()).orElse(null);
            course.setFaculty(faculty);
        }
        
        // Set department relationship (single). Prefer ID, fallback to name.
        if (courseDto.getDepartmentId() != null) {
            Department dept = departmentRepo.findById(courseDto.getDepartmentId()).orElse(null);
            course.setDepartment(dept);
        } else if (courseDto.getDepartmentName() != null && !courseDto.getDepartmentName().trim().isEmpty()) {
            departmentRepo.findByDepartmentName(courseDto.getDepartmentName().trim())
                    .ifPresent(course::setDepartment);
        }

        if (courseDto.getIntakeId() != null) {
            Intake intake = intakeRepository.findById(courseDto.getIntakeId()).orElse(null);
            course.setIntake(intake);
        }
        
        Course saved = courseRepo.save(course);
        
        // Create notification for course creation
        notificationService.createSystemNotification(
            "New Course Created",
            "Course '" + saved.getCourse_name() + "' (" + saved.getCourse_code() + ") has been created successfully."
        );
        
        return mapper.mapCourseToDto(saved);
    }

    @Override
    public CourseDto getCourseById(Long id) {
        Optional<Course> course = courseRepo.findByIdWithDepartments(id);
        return course.map(mapper::mapCourseToDto).orElse(null);
    }

    @Override
    public List<CourseDto> getAllCourses() {
        return courseRepo.findAllWithDepartments().stream().map(mapper::mapCourseToDto).collect(Collectors.toList());
    }

    @Override
    public CourseDto updateCourse(Long id, CourseDto courseDto) {
        Optional<Course> optional = courseRepo.findById(id);
        if (optional.isPresent()) {
            Course course = optional.get();
            course.setCourse_name(courseDto.getCourse_name());
            course.setCourse_code(courseDto.getCourse_code());
            course.setCourse_credit(courseDto.getCourse_credit());
            
            // Update faculty relationship if provided
            if (courseDto.getFacultyId() != null) {
                Faculty faculty = facultyRepo.findById(courseDto.getFacultyId()).orElse(null);
                course.setFaculty(faculty);
            }
            
            // Update department relationship (single) if provided
            if (courseDto.getDepartmentId() != null) {
                Department dept = departmentRepo.findById(courseDto.getDepartmentId()).orElse(null);
                course.setDepartment(dept);
            } else if (courseDto.getDepartmentName() != null) {
                String name = courseDto.getDepartmentName();
                if (name != null && !name.trim().isEmpty()) {
                    departmentRepo.findByDepartmentName(name.trim()).ifPresent(course::setDepartment);
                }
            }

            if (courseDto.getIntakeId() != null) {
                Intake intake = intakeRepository.findById(courseDto.getIntakeId()).orElse(null);
                course.setIntake(intake);
            }
            
            Course updated = courseRepo.save(course);
            
            // Create notification for course update
            notificationService.createSystemNotification(
                "Course Updated",
                "Course '" + updated.getCourse_name() + "' (" + updated.getCourse_code() + ") has been updated successfully."
            );
            
            return mapper.mapCourseToDto(updated);
        }
        return null;
    }

    @Override
    public void deleteCourse(Long id) {
        Optional<Course> courseOpt = courseRepo.findById(id);
        if (courseOpt.isPresent()) {
            Course course = courseOpt.get();
            String courseName = course.getCourse_name();
            String courseCode = course.getCourse_code();
            
            courseRepo.deleteById(id);
            
            // Create notification for course deletion
            notificationService.createSystemNotification(
                "Course Deleted",
                "Course '" + courseName + "' (" + courseCode + ") has been deleted."
            );
        }
    }
} 