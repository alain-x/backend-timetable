package com.digital_timetable.controller;

import com.digital_timetable.dto.AdminRequest;
import com.digital_timetable.dto.StaffRequest;
import com.digital_timetable.dto.UserUpdateRequest;
import com.digital_timetable.entity.User;
import com.digital_timetable.enums.UserRole;
import com.digital_timetable.repository.UserRepo;
import com.digital_timetable.service.interf.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    private static final String[] USER_EXPORT_HEADERS = new String[] {
            "name",
            "email",
            "phoneNumber",
            "password",
            "role",
            "active"
    };

    private UserRole parseRoleOrNull(String raw) {
        if (raw == null) return null;
        String s = raw.trim();
        if (s.isEmpty()) return null;
        try {
            return UserRole.valueOf(s.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

    private boolean parseActive(Boolean raw, Boolean fallback) {
        if (raw == null) return fallback != null ? fallback : true;
        return raw;
    }

    private Boolean parseActiveStringOrNull(String raw) {
        if (raw == null) return null;
        String s = raw.trim().toLowerCase();
        if (s.isEmpty()) return null;
        if (s.equals("true") || s.equals("1") || s.equals("yes") || s.equals("y")) return true;
        if (s.equals("false") || s.equals("0") || s.equals("no") || s.equals("n")) return false;
        return null;
    }

    private void upsertUserFromImport(String name, String email, String phoneNumber, String password, String roleRaw, String activeRaw) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required for import");
        }
        String normalizedEmail = email.trim();
        UserRole role = parseRoleOrNull(roleRaw);
        Boolean active = parseActiveStringOrNull(activeRaw);

        User user = userRepo.findByEmail(normalizedEmail).orElseGet(User::new);
        user.setEmail(normalizedEmail);
        if (name != null && !name.trim().isEmpty()) user.setName(name.trim());
        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) user.setPhoneNumber(phoneNumber.trim());
        if (role != null) user.setRole(role);
        user.setActive(parseActive(active, user.getId() == null ? Boolean.TRUE : user.isActive()));

        if (password != null && !password.trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(password.trim()));
        } else if (user.getId() == null) {
            // For brand-new imported users, ensure they can login by setting a default password.
            user.setPassword(passwordEncoder.encode("admin"));
        }

        userRepo.save(user);
    }

    // Create Admin
    @PostMapping("/create-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createAdminUser(@Valid @RequestBody AdminRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = User.builder()
                    .email(request.getEmail())
                    .name(request.getName())
                    .phoneNumber(request.getPhoneNumber())
                    .password(request.getPassword())
                    .role(request.getRole())
                    .build();

            User createdUser = adminUserService.createAdminUser(user);

            response.put("status", "SUCCESS");
            response.put("message", "Admin user created successfully");
            response.put("data", createdUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // Create Staff
    @PostMapping("/create-staff")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createStaffUser(@Valid @RequestBody StaffRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = User.builder()
                    .email(request.getEmail())
                    .name(request.getName())
                    .phoneNumber(request.getPhoneNumber())
                    .password(request.getPassword())
                    .role(request.getRole())
                    .build();

            User createdUser = adminUserService.createStaffUser(user);

            response.put("status", "SUCCESS");
            response.put("message", "Staff user created successfully");
            response.put("data", createdUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // Create any user (Admin, Lecturer, Class Representative, Staff)
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createUser(@Valid @RequestBody StaffRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = User.builder()
                    .email(request.getEmail())
                    .name(request.getName())
                    .phoneNumber(request.getPhoneNumber())
                    .password(request.getPassword())
                    .role(request.getRole())
                    .active(true)
                    .build();

            User createdUser = adminUserService.createUser(user);

            response.put("status", "SUCCESS");
            response.put("message", "User created successfully");
            response.put("data", createdUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // Update user
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = User.builder()
                    .id(id)
                    .email(request.getEmail())
                    .name(request.getName())
                    .phoneNumber(request.getPhoneNumber())
                    .password(request.getPassword())
                    .role(request.getRole())
                    .build();

            User updatedUser = adminUserService.updateUser(user);

            response.put("status", "SUCCESS");
            response.put("message", "User updated successfully");
            response.put("data", updatedUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // Activate/Deactivate user
    @PostMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> toggleUserStatus(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            User updatedUser = adminUserService.toggleUserStatus(id);
            response.put("status", "SUCCESS");
            response.put("message", "User status updated successfully");
            response.put("data", updatedUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // Delete user
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            adminUserService.deleteUser(id);
            response.put("status", "SUCCESS");
            response.put("message", "User deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // Get all users without sample data
    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = userRepo.findAll();
            List<Map<String, Object>> userDtos = users.stream()
                .map(user -> {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("id", user.getId());
                    dto.put("name", user.getName());
                    dto.put("email", user.getEmail());
                    dto.put("phoneNumber", user.getPhoneNumber());
                    dto.put("role", user.getRole().toString());
                    dto.put("active", user.isActive());
                    dto.put("createdAt", user.getCreatedAt());
                    return dto;
                })
                .collect(Collectors.toList());

            return ResponseEntity.ok(userDtos);
        } catch (Exception e) {
            System.err.println("Error fetching users: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching users");
        }
    }

    // ================= Import / Export Users =================

    @GetMapping(value = "/export/csv", produces = "text/csv")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportUsersCsv() {
        try {
            List<User> users = userRepo.findAll();
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            try (CSVPrinter printer = new CSVPrinter(
                    new java.io.OutputStreamWriter(out, StandardCharsets.UTF_8),
                    CSVFormat.DEFAULT.withHeader(USER_EXPORT_HEADERS)
            )) {
                for (User u : users) {
                    printer.printRecord(
                            u.getName(),
                            u.getEmail(),
                            u.getPhoneNumber(),
                            "", // never export passwords
                            u.getRole() != null ? u.getRole().name() : "",
                            String.valueOf(u.isActive())
                    );
                }
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users.csv");
            return new ResponseEntity<>(out.toByteArray(), headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Failed to export users CSV: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
        }
    }

    @GetMapping(value = "/export/xlsx", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportUsersXlsx() {
        try {
            List<User> users = userRepo.findAll();
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            try (XSSFWorkbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("users");

                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < USER_EXPORT_HEADERS.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(USER_EXPORT_HEADERS[i]);
                }

                int rowIdx = 1;
                for (User u : users) {
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(u.getName() != null ? u.getName() : "");
                    row.createCell(1).setCellValue(u.getEmail() != null ? u.getEmail() : "");
                    row.createCell(2).setCellValue(u.getPhoneNumber() != null ? u.getPhoneNumber() : "");
                    row.createCell(3).setCellValue(""); // never export passwords
                    row.createCell(4).setCellValue(u.getRole() != null ? u.getRole().name() : "");
                    row.createCell(5).setCellValue(u.isActive());
                }

                workbook.write(out);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users.xlsx");
            return new ResponseEntity<>(out.toByteArray(), headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Failed to export users XLSX: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
        }
    }

    @PostMapping(value = "/import/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> importUsersCsv(@RequestPart("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("File is required");
            }

            int processed = 0;
            try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
                 CSVParser parser = CSVFormat.DEFAULT
                         .withFirstRecordAsHeader()
                         .withIgnoreEmptyLines()
                         .withTrim()
                         .parse(reader)) {
                for (CSVRecord r : parser) {
                    upsertUserFromImport(
                            r.isMapped("name") ? r.get("name") : null,
                            r.isMapped("email") ? r.get("email") : null,
                            r.isMapped("phoneNumber") ? r.get("phoneNumber") : (r.isMapped("phone_number") ? r.get("phone_number") : null),
                            r.isMapped("password") ? r.get("password") : null,
                            r.isMapped("role") ? r.get("role") : null,
                            r.isMapped("active") ? r.get("active") : null
                    );
                    processed++;
                }
            }

            response.put("status", "SUCCESS");
            response.put("message", "Users imported successfully (CSV)");
            response.put("processed", processed);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping(value = "/import/xlsx", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> importUsersXlsx(@RequestPart("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("File is required");
            }

            int processed = 0;
            try (XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream())) {
                Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
                if (sheet == null) {
                    throw new IllegalArgumentException("XLSX has no sheets");
                }

                Row header = sheet.getRow(0);
                if (header == null) {
                    throw new IllegalArgumentException("XLSX missing header row");
                }

                java.util.Map<String, Integer> colIndex = new java.util.HashMap<>();
                for (Cell c : header) {
                    if (c == null) continue;
                    String key = c.getStringCellValue();
                    if (key != null) colIndex.put(key.trim(), c.getColumnIndex());
                }

                for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                    Row row = sheet.getRow(r);
                    if (row == null) continue;

                    String name = getCellString(row, colIndex.get("name"));
                    String email = getCellString(row, colIndex.get("email"));
                    String phoneNumber = getCellString(row, colIndex.get("phoneNumber"));
                    if ((phoneNumber == null || phoneNumber.isBlank()) && colIndex.containsKey("phone_number")) {
                        phoneNumber = getCellString(row, colIndex.get("phone_number"));
                    }
                    String password = getCellString(row, colIndex.get("password"));
                    String role = getCellString(row, colIndex.get("role"));
                    String active = getCellString(row, colIndex.get("active"));

                    // skip fully-empty rows
                    if ((email == null || email.isBlank()) && (name == null || name.isBlank())) {
                        continue;
                    }

                    upsertUserFromImport(name, email, phoneNumber, password, role, active);
                    processed++;
                }
            }

            response.put("status", "SUCCESS");
            response.put("message", "Users imported successfully (XLSX)");
            response.put("processed", processed);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    private String getCellString(Row row, Integer idx) {
        if (row == null || idx == null) return null;
        Cell cell = row.getCell(idx);
        if (cell == null) return null;

        try {
            return switch (cell.getCellType()) {
                case STRING -> cell.getStringCellValue();
                case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
                case NUMERIC -> {
                    double d = cell.getNumericCellValue();
                    if (Math.floor(d) == d) yield String.valueOf((long) d);
                    yield String.valueOf(d);
                }
                case FORMULA -> cell.getCellFormula();
                default -> null;
            };
        } catch (Exception e) {
            return null;
        }
    }
}
