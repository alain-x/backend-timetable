package com.digital_timetable.repository;

import com.digital_timetable.entity.Intake;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IntakeRepository extends JpaRepository<Intake, Long> {

    List<Intake> findByDepartment_Id(Long departmentId);

    List<Intake> findByDepartment_IdAndStudyModeIgnoreCase(Long departmentId, String studyMode);
}
