package com.digital_timetable.service.impl;

import com.digital_timetable.dto.FacultyDto;
import com.digital_timetable.entity.Faculty;
import com.digital_timetable.mapper.EntityDtoMapper;
import com.digital_timetable.repository.FacultyRepo;
import com.digital_timetable.service.interf.FacultyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FacultyServiceImpl implements FacultyService {
    @Autowired
    private FacultyRepo facultyRepo;
    @Autowired
    private EntityDtoMapper mapper;

    @Override
    public FacultyDto createFaculty(FacultyDto facultyDto) {
        Faculty faculty = mapper.mapDtoToFaculty(facultyDto);
        Faculty saved = facultyRepo.save(faculty);
        return mapper.mapFacultyToDto(saved);
    }

    @Override
    public FacultyDto getFacultyById(Long id) {
        Optional<Faculty> faculty = facultyRepo.findById(id);
        return faculty.map(mapper::mapFacultyToDto).orElse(null);
    }

    @Override
    public List<FacultyDto> getAllFaculties() {
        return facultyRepo.findAll().stream().map(mapper::mapFacultyToDto).collect(Collectors.toList());
    }

    @Override
    public FacultyDto updateFaculty(Long id, FacultyDto facultyDto) {
        Optional<Faculty> optional = facultyRepo.findById(id);
        if (optional.isPresent()) {
            Faculty faculty = optional.get();
            faculty.setFaculty_name(facultyDto.getFaculty_name());
            return mapper.mapFacultyToDto(facultyRepo.save(faculty));
        }
        return null;
    }

    @Override
    public void deleteFaculty(Long id) {
        try {
            // Check if faculty exists first
            Optional<Faculty> faculty = facultyRepo.findById(id);
            if (faculty.isEmpty()) {
                throw new RuntimeException("Faculty not found with id: " + id);
            }
            
            // Check if faculty has associated departments or courses
            Faculty facultyEntity = faculty.get();
            if (facultyEntity.getDepartments() != null && !facultyEntity.getDepartments().isEmpty()) {
                throw new RuntimeException("Cannot delete faculty. It has associated departments. Please remove departments first.");
            }
            
            if (facultyEntity.getCourses() != null && !facultyEntity.getCourses().isEmpty()) {
                throw new RuntimeException("Cannot delete faculty. It has associated courses. Please remove courses first.");
            }
            
            facultyRepo.deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete faculty: " + e.getMessage());
        }
    }
}