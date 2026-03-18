package com.digital_timetable.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FacultyDto {
    private Long id;
    
    @NotBlank(message = "Faculty name is required")
    private String faculty_name;
} 