package com.digital_timetable.service.impl;

import com.digital_timetable.dto.DepartmentDto;
import com.digital_timetable.entity.Department;
import com.digital_timetable.entity.Faculty;
import com.digital_timetable.mapper.EntityDtoMapper;
import com.digital_timetable.repository.TimetableRepo;
import com.digital_timetable.repository.CourseRepo;
import com.digital_timetable.repository.DepartmentRepo;
import com.digital_timetable.repository.FacultyRepo;
import com.digital_timetable.service.interf.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl implements DepartmentService {
    @Autowired
    private DepartmentRepo departmentRepo;
    
    @Autowired
    private FacultyRepo facultyRepo;
    
    @Autowired
    private EntityDtoMapper mapper;

    @Autowired
    private TimetableRepo timetableRepo;

    @Autowired
    private CourseRepo courseRepo;

    @Override
    public DepartmentDto createDepartment(DepartmentDto departmentDto) {
        Department department = mapper.mapDtoToDepartment(departmentDto);
        
        // Set faculty relationship if provided
        if (departmentDto.getFacultyId() != null) {
            Faculty faculty = facultyRepo.findById(departmentDto.getFacultyId()).orElse(null);
            department.setFaculty(faculty);
        }
        
        Department saved = departmentRepo.save(department);
        return mapper.mapDepartmentToDto(saved);
    }

    @Override
    public DepartmentDto getDepartmentById(Long id) {
        Optional<Department> department = departmentRepo.findById(id);
        return department.map(mapper::mapDepartmentToDto).orElse(null);
    }

    @Override
    public List<DepartmentDto> getAllDepartments() {
        return departmentRepo.findAll().stream().map(mapper::mapDepartmentToDto).collect(Collectors.toList());
    }

    @Override
    public DepartmentDto updateDepartment(Long id, DepartmentDto departmentDto) {
        Optional<Department> optional = departmentRepo.findById(id);
        if (optional.isPresent()) {
            Department department = optional.get();
            department.setDepartment_name(departmentDto.getDepartment_name());
            
            // Update faculty relationship if provided
            if (departmentDto.getFacultyId() != null) {
                Faculty faculty = facultyRepo.findById(departmentDto.getFacultyId()).orElse(null);
                department.setFaculty(faculty);
            }
            
            return mapper.mapDepartmentToDto(departmentRepo.save(department));
        }
        return null;
    }

    @Override
    @Transactional
    public void deleteDepartment(Long id) {
        try {
            // Ensure department exists
            Optional<Department> optional = departmentRepo.findById(id);
            if (optional.isEmpty()) {
                throw new RuntimeException("Department not found with id: " + id);
            }

            Department department = optional.get();

            // Detach timetables referencing this department
            var timetables = timetableRepo.findByDepartment_Id(id);
            if (timetables != null && !timetables.isEmpty()) {
                timetables.forEach(t -> t.setDepartment(null));
                timetableRepo.saveAll(timetables);
            }

            // Detach courses referencing this department
            var courses = courseRepo.findByDepartment_Id(id);
            if (courses != null && !courses.isEmpty()) {
                courses.forEach(c -> c.setDepartment(null));
                courseRepo.saveAll(courses);
            }

            // Now delete department
            departmentRepo.deleteById(id);
        } catch (Exception e) {
            // Surface a clear message up to the controller
            throw new RuntimeException("Failed to delete department: " + e.getMessage());
        }
    }
}