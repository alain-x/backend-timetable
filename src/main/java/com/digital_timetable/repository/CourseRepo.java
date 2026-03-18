package com.digital_timetable.repository;

import com.digital_timetable.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface CourseRepo extends JpaRepository<Course, Long> {
    // Eagerly load department to avoid LazyInitialization and ensure mapper sees data
    @Query("select c from Course c left join fetch c.department where c.id = :id")
    Optional<Course> findByIdWithDepartments(@Param("id") Long id);

    @Query("select distinct c from Course c left join fetch c.department")
    List<Course> findAllWithDepartments();

    // Find courses by department id
    List<Course> findByDepartment_Id(Long departmentId);

    List<Course> findByIntake_Id(Long intakeId);
}