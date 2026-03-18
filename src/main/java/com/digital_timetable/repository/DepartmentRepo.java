package com.digital_timetable.repository;

import com.digital_timetable.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface DepartmentRepo extends JpaRepository<Department, Long> {
    // Lookup by department name (entity field is department_name)
    @Query("SELECT d FROM Department d WHERE d.department_name = :name")
    Optional<Department> findByDepartmentName(@Param("name") String name);
}