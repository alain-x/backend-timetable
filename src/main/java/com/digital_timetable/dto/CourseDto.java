package com.digital_timetable.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseDto {
    private Long id;
    
    @NotBlank(message = "Course name is required")
    private String course_name;
    
    @NotBlank(message = "Course code is required")
    private String course_code;
    
    @NotNull(message = "Course credit is required")
    @Min(value = 10, message = "Course credit must be at least 10")
    private int course_credit;

    // Relationship fields
    private Long facultyId;
    private String facultyName;
    private Long departmentId;
    private String departmentName;

    private Long intakeId;
    private String intakeName;
} 