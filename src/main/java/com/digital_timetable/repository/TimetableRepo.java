package com.digital_timetable.repository;

import com.digital_timetable.entity.Room;
import com.digital_timetable.entity.Timetable;
import com.digital_timetable.enums.Program;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TimetableRepo extends JpaRepository<Timetable, Long> {
    // Find timetables by room
    List<Timetable> findByRoom(Room room);
    
    // Find timetables by room and date time range for conflict detection
    @Query("SELECT t FROM Timetable t WHERE t.room = :room AND " +
           "((t.startDateTime BETWEEN :startDateTime AND :endDateTime) OR " +
           "(t.endDateTime BETWEEN :startDateTime AND :endDateTime) OR " +
           "(:startDateTime BETWEEN t.startDateTime AND t.endDateTime))")
    List<Timetable> findByRoomAndDateTimeRange(@Param("room") Room room, 
                                             @Param("startDateTime") LocalDateTime startDateTime, 
                                             @Param("endDateTime") LocalDateTime endDateTime);
    
    // Find timetables by lecturer
    List<Timetable> findByLecturerId(Long lecturerId);
    
    // Find timetables by course
    List<Timetable> findByCourseId(Long courseId);
    
    // Find timetables by section (DAY/EVENING)
    List<Timetable> findBySection(Program section);
    
    // Find timetables by date range
    @Query("SELECT t FROM Timetable t WHERE t.startDateTime BETWEEN :startDate AND :endDate")
    List<Timetable> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                   @Param("endDate") LocalDateTime endDate);

    // Count timetables by department id (for safe delete checks)
    long countByDepartment_Id(Long departmentId);

    // Find timetables by department id
    List<Timetable> findByDepartment_Id(Long departmentId);

    List<Timetable> findByIntake_Id(Long intakeId);

    List<Timetable> findByCourse_IdAndIntake_Id(Long courseId, Long intakeId);
}