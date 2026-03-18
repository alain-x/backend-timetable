package com.digital_timetable.repository;

import com.digital_timetable.entity.IntakeCourseCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IntakeCourseCompletionRepository extends JpaRepository<IntakeCourseCompletion, Long> {

    Optional<IntakeCourseCompletion> findByIntake_IdAndCourse_Id(Long intakeId, Long courseId);

    List<IntakeCourseCompletion> findByIntake_Id(Long intakeId);
}
