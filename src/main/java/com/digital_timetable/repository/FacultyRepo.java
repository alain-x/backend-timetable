package com.digital_timetable.repository;

import com.digital_timetable.entity.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FacultyRepo extends JpaRepository<Faculty, Long> {
    // Additional query methods if needed
} 