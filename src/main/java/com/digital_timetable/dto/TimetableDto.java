package com.digital_timetable.dto;

import com.digital_timetable.enums.Program;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimetableDto {
    private Long id;
    private String course_name;
    private String faculty_name;
    private String department_name;
    private String lecture_name;
    private String room_name;
    private String title;
    private String description;
    private String startDateTime;
    private String endDateTime;
    private String color;
    private String recurrence;
    private String notes;
    private Program section;
    private int hours;
    private String status;

    // Relationship fields
    private Long courseId;
    private String courseCode;
    private Long lecturerId;
    private String lecturerName;
    private Long roomId;
    private String roomBlock;
    private String roomLocation;
    private Long facultyId;
    private Long departmentId;
    private Long classRepUserId;
    private String classRepName;
    private Long intakeId;
    private String intakeName;
} 