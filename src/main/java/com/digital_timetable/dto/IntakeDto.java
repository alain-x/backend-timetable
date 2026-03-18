package com.digital_timetable.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IntakeDto {

    private Long id;
    private String name;
    private String intakeCode;
    private String programName;
    private String studyMode;
    private String campus;
    private Long departmentId;
    private String status;
    private String startDate;
    private String expectedEndDate;
}
