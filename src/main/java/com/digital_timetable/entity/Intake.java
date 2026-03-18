package com.digital_timetable.entity;

import com.digital_timetable.enums.IntakeStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "intakes")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Intake {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Intake name is required")
    private String name;

    @NotBlank(message = "Intake code is required")
    private String intakeCode;

    @NotBlank(message = "Program name is required")
    private String programName;

    @NotBlank(message = "Study mode is required")
    private String studyMode;

    @NotBlank(message = "Campus is required")
    private String campus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private IntakeStatus status = IntakeStatus.ONGOING;

    private LocalDate startDate;

    private LocalDate expectedEndDate;
}
