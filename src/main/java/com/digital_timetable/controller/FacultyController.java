package com.digital_timetable.controller;

import com.digital_timetable.dto.FacultyDto;
import com.digital_timetable.service.interf.FacultyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/faculties")
public class FacultyController {
    @Autowired
    private FacultyService facultyService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FacultyDto> createFaculty(@RequestBody FacultyDto facultyDto) {
        return ResponseEntity.ok(facultyService.createFaculty(facultyDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FacultyDto> getFaculty(@PathVariable Long id) {
        FacultyDto dto = facultyService.getFacultyById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<List<FacultyDto>> getAllFaculties() {
        return ResponseEntity.ok(facultyService.getAllFaculties());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FacultyDto> updateFaculty(@PathVariable Long id, @RequestBody FacultyDto facultyDto) {
        FacultyDto updated = facultyService.updateFaculty(id, facultyDto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteFaculty(@PathVariable Long id) {
        try {
            facultyService.deleteFaculty(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
} 