package com.digital_timetable.controller;

import com.digital_timetable.dto.IntakeDto;
import com.digital_timetable.dto.Response;
import com.digital_timetable.entity.Intake;
import com.digital_timetable.repository.IntakeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class IntakeController {

    private final IntakeRepository intakeRepository;

    @GetMapping("/intakes")
    public ResponseEntity<Response> getIntakes(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String studyMode
    ) {
        try {
            List<Intake> intakes;
            if (departmentId != null && studyMode != null && !studyMode.isBlank()) {
                intakes = intakeRepository.findByDepartment_IdAndStudyModeIgnoreCase(departmentId, studyMode);
            } else if (departmentId != null) {
                intakes = intakeRepository.findByDepartment_Id(departmentId);
            } else {
                intakes = intakeRepository.findAll();
            }

            List<IntakeDto> data = new ArrayList<>();
            for (Intake intake : intakes) {
                IntakeDto dto = new IntakeDto();
                dto.setId(intake.getId());
                dto.setName(intake.getName());
                dto.setIntakeCode(intake.getIntakeCode());
                dto.setProgramName(intake.getProgramName());
                dto.setStudyMode(intake.getStudyMode());
                dto.setCampus(intake.getCampus());
                dto.setDepartmentId(intake.getDepartment() != null ? intake.getDepartment().getId() : null);
                dto.setStatus(intake.getStatus() != null ? intake.getStatus().name() : null);
                dto.setStartDate(intake.getStartDate() != null ? intake.getStartDate().toString() : null);
                dto.setExpectedEndDate(intake.getExpectedEndDate() != null ? intake.getExpectedEndDate().toString() : null);
                data.add(dto);
            }

            Response response = Response.builder()
                    .status(HttpStatus.OK.value())
                    .message("Intakes retrieved successfully")
                    .data(data)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Response response = Response.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to retrieve intakes: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
