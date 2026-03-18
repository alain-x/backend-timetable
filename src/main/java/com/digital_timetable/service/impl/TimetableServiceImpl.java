package com.digital_timetable.service.impl;

import com.digital_timetable.dto.TimetableDto;
import com.digital_timetable.entity.Timetable;
import com.digital_timetable.entity.SwapRequest;
import com.digital_timetable.entity.Course;
import com.digital_timetable.entity.User;
import com.digital_timetable.entity.Room;
import com.digital_timetable.entity.Faculty;
import com.digital_timetable.entity.Department;
import com.digital_timetable.entity.Intake;
import com.digital_timetable.entity.IntakeCourseCompletion;
import com.digital_timetable.enums.UserRole;
import com.digital_timetable.enums.Program;
import com.digital_timetable.mapper.EntityDtoMapper;
import com.digital_timetable.repository.TimetableRepo;
import com.digital_timetable.repository.CourseRepo;
import com.digital_timetable.repository.UserRepo;
import com.digital_timetable.repository.RoomRepo;
import com.digital_timetable.repository.FacultyRepo;
import com.digital_timetable.repository.DepartmentRepo;
import com.digital_timetable.repository.IntakeCourseCompletionRepository;
import com.digital_timetable.repository.IntakeRepository;
import com.digital_timetable.repository.SwapRequestRepo;
import com.digital_timetable.service.interf.TimetableService;
import com.digital_timetable.service.interf.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Comparator;

@Service
public class TimetableServiceImpl implements TimetableService {
    @Autowired
    private TimetableRepo timetableRepo;
    
    @Autowired
    private CourseRepo courseRepo;
    
    @Autowired
    private UserRepo userRepo;
    
    @Autowired
    private RoomRepo roomRepo;
    
    @Autowired
    private FacultyRepo facultyRepo;
    
    @Autowired
    private DepartmentRepo departmentRepo;

    @Autowired
    private IntakeRepository intakeRepository;

    @Autowired
    private IntakeCourseCompletionRepository intakeCourseCompletionRepository;
    
    @Autowired
    private EntityDtoMapper mapper;
    
    @Autowired
    private NotificationService notificationService;

    @Autowired
    private SwapRequestRepo swapRequestRepo;

    @Override
    public TimetableDto createTimetable(TimetableDto timetableDto) {
        // Conflict detection: check for overlapping events for the same room or lecturer
        LocalDateTime start = timetableDto.getStartDateTime() != null ? LocalDateTime.parse(timetableDto.getStartDateTime()) : null;
        LocalDateTime end = timetableDto.getEndDateTime() != null ? LocalDateTime.parse(timetableDto.getEndDateTime()) : null;
        if (start != null && end != null && !end.isAfter(start)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        
        // Check for conflicts (room or lecturer) - now section-aware
        if (start != null && end != null) {
            List<Timetable> conflicts = timetableRepo.findAll().stream()
                .filter(t -> {
                    boolean hasTimes = t.getStartDateTime() != null && t.getEndDateTime() != null;
                    if (!hasTimes) return false;
                    boolean overlaps = start.isBefore(t.getEndDateTime()) && end.isAfter(t.getStartDateTime());

                    // Section-aware checks
                    boolean sameLecturer = Objects.equals(t.getLecture_name(), timetableDto.getLecture_name());
                    Program newSection = timetableDto.getSection();
                    Program existingSection = t.getSection();
                    boolean sameSection = (newSection != null && existingSection != null)
                        ? newSection == existingSection
                        : true; // if section is unknown on either side, treat as same to be safe

                    // Room conflict only if SAME room AND SAME section (or section unknown)
                    boolean sameRoom = Objects.equals(t.getRoom_name(), timetableDto.getRoom_name());
                    boolean roomConflict = sameRoom && overlaps && sameSection;

                    // Lecturer conflicts: only a conflict within the SAME section.
                    boolean lecturerConflict = sameLecturer && overlaps && sameSection;

                    return roomConflict || lecturerConflict;
                })
                .toList();
                
            if (!conflicts.isEmpty()) {
                // Provide more detailed conflict information
                StringBuilder conflictMessage = new StringBuilder("Conflicts detected:\n");
                for (Timetable conflict : conflicts) {
                    if (Objects.equals(conflict.getRoom_name(), timetableDto.getRoom_name())) {
                        conflictMessage.append("- Room ").append(conflict.getRoom_name())
                                     .append(" is already booked from ")
                                     .append(conflict.getStartDateTime().toLocalTime())
                                     .append(" to ")
                                     .append(conflict.getEndDateTime().toLocalTime())
                                     .append(" (Section: ").append(conflict.getSection()).append(")\n");
                    }
                    if (Objects.equals(conflict.getLecture_name(), timetableDto.getLecture_name())) {
                        conflictMessage.append("- Lecturer ").append(conflict.getLecture_name())
                                     .append(" is already scheduled from ")
                                     .append(conflict.getStartDateTime().toLocalTime())
                                     .append(" to ")
                                     .append(conflict.getEndDateTime().toLocalTime())
                                     .append(" (Section: ").append(conflict.getSection()).append(")\n");
                    }
                }
                throw new IllegalArgumentException(conflictMessage.toString());
            }
        }
        
        Timetable timetable = mapper.mapDtoToTimetable(timetableDto);
        
        // Set relationships if IDs are provided
        if (timetableDto.getCourseId() != null) {
            Course course = courseRepo.findById(timetableDto.getCourseId()).orElse(null);
            timetable.setCourse(course);
        }
        
        if (timetableDto.getLecturerId() != null) {
            User lecturer = userRepo.findById(timetableDto.getLecturerId()).orElse(null);
            timetable.setLecturer(lecturer);
        }
        
        if (timetableDto.getRoomId() != null) {
            Room room = roomRepo.findById(timetableDto.getRoomId()).orElse(null);
            timetable.setRoom(room);
            // Mark room as booked when used in a new timetable event
            if (room != null && !room.isBooked()) {
                room.setBooked(true);
                roomRepo.save(room);
            }
        }
        
        if (timetableDto.getFacultyId() != null) {
            Faculty faculty = facultyRepo.findById(timetableDto.getFacultyId()).orElse(null);
            timetable.setFaculty(faculty);
        }
        
        if (timetableDto.getDepartmentId() != null) {
            Department department = departmentRepo.findById(timetableDto.getDepartmentId()).orElse(null);
            timetable.setDepartment(department);
        }
        
        if (timetableDto.getClassRepUserId() != null) {
            User classRep = userRepo.findById(timetableDto.getClassRepUserId()).orElse(null);
            // Enforce: Class Rep may only be associated with a single course across active/future timetables
            Long selectedCourseId = timetableDto.getCourseId();
            if (selectedCourseId == null) {
                throw new IllegalArgumentException("Class Rep assignment requires a course selection");
            }
            boolean assignedToAnotherCourse = timetableRepo.findAll().stream()
                .filter(t -> {
                    boolean endedStatus = t.getStatus() != null && t.getStatus().equalsIgnoreCase("ended");
                    boolean inFuture = t.getEndDateTime() == null || t.getEndDateTime().isAfter(LocalDateTime.now());
                    return inFuture && !endedStatus;
                })
                .filter(t -> t.getClassRep() != null && t.getClassRep().getId().equals(classRep != null ? classRep.getId() : -1L))
                .anyMatch(t -> {
                    Long existingCourseId = (t.getCourse() != null ? t.getCourse().getId() : null);
                    return existingCourseId != null && !existingCourseId.equals(selectedCourseId);
                });
            if (assignedToAnotherCourse) {
                throw new IllegalArgumentException("Selected Class Rep is already assigned to a different course");
            }
            timetable.setClassRep(classRep);
        }

        if (timetableDto.getIntakeId() != null) {
            Intake intake = intakeRepository.findById(timetableDto.getIntakeId()).orElse(null);
            timetable.setIntake(intake);
        }
        
        Timetable saved = timetableRepo.save(timetable);
        
        // Send notifications to all students about the new timetable
        sendTimetableNotification(saved, "created");
        
        return mapper.mapTimetableToDto(saved);
    }
    
    private void sendTimetableNotification(Timetable timetable, String action) {
        try {
            // Format the date and time for the notification
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            
            String dateStr = timetable.getStartDateTime().format(dateFormatter);
            String startTimeStr = timetable.getStartDateTime().format(dateFormatter);
            String endTimeStr = timetable.getEndDateTime().format(dateFormatter);
            
            // Get names from related entities or fallback to string fields
            String courseName = timetable.getCourse() != null ? timetable.getCourse().getCourse_name() : 
                               (timetable.getCourse_name() != null ? timetable.getCourse_name() : "Unknown Course");
            String lecturerName = timetable.getLecturer() != null ? timetable.getLecturer().getName() : 
                                 (timetable.getLecture_name() != null ? timetable.getLecture_name() : "Unknown Lecturer");
            String roomName = timetable.getRoom() != null ? timetable.getRoom().getRoom_name() : 
                             (timetable.getRoom_name() != null ? timetable.getRoom_name() : "Unknown Room");
            String facultyName = timetable.getFaculty() != null ? timetable.getFaculty().getFaculty_name() : 
                               (timetable.getFaculty_name() != null ? timetable.getFaculty_name() : "");
            String departmentName = timetable.getDepartment() != null ? timetable.getDepartment().getDepartment_name() : 
                                  (timetable.getDepartment_name() != null ? timetable.getDepartment_name() : "");

            String title = courseName + " is available";
            String message = String.format(
                    "A new Course has been %s:\n" +
                            "\n Course: %s\n " +
                            "\n Lecturer: %s\n " +
                            "\n Room: %s\n " +
                            "\n Date to Start: %s\n " +
                            "\n Date to End: %s\n " +
                            "\n Notes: %s",
                    action,
                    courseName,
                    lecturerName,
                    roomName,
                    startTimeStr,
                    endTimeStr,
                    timetable.getNotes() != null ? timetable.getNotes() : "No additional notes"
            );


            // Send a single system-wide notification (one notification only)
            notificationService.createSystemNotification(title, message);
            
        } catch (Exception e) {
            // Log error but don't fail the timetable creation
            System.err.println("Error sending timetable notification: " + e.getMessage());
        }
    }

    @Override
    public TimetableDto getTimetableById(Long id) {
        Optional<Timetable> timetable = timetableRepo.findById(id);
        return timetable.map(mapper::mapTimetableToDto).orElse(null);
    }

    @Override
    public List<TimetableDto> getAllTimetables() {
        try {
            List<IntakeCourseCompletion> tmpCompletions;
            try {
                tmpCompletions = intakeCourseCompletionRepository.findAll();
            } catch (Exception ignored) {
                tmpCompletions = java.util.Collections.emptyList();
            }
            final List<IntakeCourseCompletion> completions = tmpCompletions;

            return timetableRepo.findAll().stream()
                .filter(t -> {
                    String s = t != null ? t.getStatus() : null;
                    return s == null || (!s.equalsIgnoreCase("deleted") && !s.equalsIgnoreCase("completed"));
                })
                .filter(t -> {
                    if (t == null) return false;
                    if (completions.isEmpty()) return true;
                    if (t.getIntake() == null || t.getIntake().getId() == null) return true;
                    if (t.getCourse() == null || t.getCourse().getId() == null) return true;
                    Long intakeId = t.getIntake().getId();
                    Long courseId = t.getCourse().getId();
                    for (IntakeCourseCompletion c : completions) {
                        if (c == null || c.getIntake() == null || c.getCourse() == null) continue;
                        if (c.getIntake().getId() != null && c.getCourse().getId() != null
                                && c.getIntake().getId().equals(intakeId)
                                && c.getCourse().getId().equals(courseId)) {
                            return false;
                        }
                    }
                    return true;
                })
                .map(timetable -> {
                    try {
                        return mapper.mapTimetableToDto(timetable);
                    } catch (Exception e) {
                        // Log the error but continue processing other timetables
                        System.err.println("Error mapping timetable " + timetable.getId() + ": " + e.getMessage());
                        return null;
                    }
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error fetching timetables: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<TimetableDto> getTimetablesByLecturer(Long lecturerId) {
        try {
            List<IntakeCourseCompletion> tmpCompletions;
            try {
                tmpCompletions = intakeCourseCompletionRepository.findAll();
            } catch (Exception ignored) {
                tmpCompletions = java.util.Collections.emptyList();
            }
            final List<IntakeCourseCompletion> completions = tmpCompletions;

            return timetableRepo.findAll().stream()
                .filter(t -> {
                    String s = t != null ? t.getStatus() : null;
                    return s == null || (!s.equalsIgnoreCase("deleted") && !s.equalsIgnoreCase("completed"));
                })
                .filter(t -> {
                    if (t == null) return false;
                    if (completions.isEmpty()) return true;
                    if (t.getIntake() == null || t.getIntake().getId() == null) return true;
                    if (t.getCourse() == null || t.getCourse().getId() == null) return true;
                    Long intakeId = t.getIntake().getId();
                    Long courseId = t.getCourse().getId();
                    for (IntakeCourseCompletion c : completions) {
                        if (c == null || c.getIntake() == null || c.getCourse() == null) continue;
                        if (c.getIntake().getId() != null && c.getCourse().getId() != null
                                && c.getIntake().getId().equals(intakeId)
                                && c.getCourse().getId().equals(courseId)) {
                            return false;
                        }
                    }
                    return true;
                })
                .filter(timetable -> timetable.getLecturer() != null && timetable.getLecturer().getId().equals(lecturerId))
                .map(timetable -> {
                    try {
                        return mapper.mapTimetableToDto(timetable);
                    } catch (Exception e) {
                        System.err.println("Error mapping timetable " + timetable.getId() + ": " + e.getMessage());
                        return null;
                    }
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error fetching timetables by lecturer: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public TimetableDto updateTimetable(Long id, TimetableDto timetableDto) {
        LocalDateTime start = timetableDto.getStartDateTime() != null ? LocalDateTime.parse(timetableDto.getStartDateTime()) : null;
        LocalDateTime end = timetableDto.getEndDateTime() != null ? LocalDateTime.parse(timetableDto.getEndDateTime()) : null;
        if (start != null && end != null && !end.isAfter(start)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        Optional<Timetable> optional = timetableRepo.findById(id);
        if (optional.isPresent()) {
            Timetable timetable = optional.get();
            // Conflict detection for update (ignore self). Lecturer conflict is section-aware.
            if (start != null && end != null) {
                List<Timetable> conflicts = timetableRepo.findAll().stream()
                    .filter(t -> !t.getId().equals(id))
                    .filter(t -> {
                        if (t.getStartDateTime() == null || t.getEndDateTime() == null) return false;
                        boolean overlaps = start.isBefore(t.getEndDateTime()) && end.isAfter(t.getStartDateTime());

                        boolean sameLecturer = Objects.equals(t.getLecture_name(), timetableDto.getLecture_name());
                        Program newSection = timetableDto.getSection();
                        Program existingSection = t.getSection();
                        boolean sameSection = (newSection != null && existingSection != null)
                            ? newSection == existingSection
                            : true; // treat as same if unknown to avoid double booking

                        boolean sameRoom = Objects.equals(t.getRoom_name(), timetableDto.getRoom_name());
                        boolean roomConflict = sameRoom && overlaps && sameSection;

                        boolean lecturerConflict = sameLecturer && overlaps && sameSection;

                        return roomConflict || lecturerConflict;
                    })
                    .toList();
                if (!conflicts.isEmpty()) {
                    throw new IllegalArgumentException("Room or lecturer is already booked for the selected time range");
                }
            }
            timetable.setTitle(timetableDto.getTitle());
            timetable.setDescription(timetableDto.getDescription());
            timetable.setStartDateTime(start);
            timetable.setEndDateTime(end);
            timetable.setColor(timetableDto.getColor());
            timetable.setRecurrence(timetableDto.getRecurrence());
            timetable.setNotes(timetableDto.getNotes());
            timetable.setCourse_name(timetableDto.getCourse_name());
            timetable.setFaculty_name(timetableDto.getFaculty_name());
            timetable.setDepartment_name(timetableDto.getDepartment_name());
            timetable.setLecture_name(timetableDto.getLecture_name());
            timetable.setRoom_name(timetableDto.getRoom_name());
            timetable.setSection(timetableDto.getSection());
            timetable.setHours(timetableDto.getHours());
            timetable.setStatus(timetableDto.getStatus());
            
            // Update relationships if provided
            if (timetableDto.getCourseId() != null) {
                Course course = courseRepo.findById(timetableDto.getCourseId()).orElse(null);
                timetable.setCourse(course);
            }
            
            if (timetableDto.getLecturerId() != null) {
                User lecturer = userRepo.findById(timetableDto.getLecturerId()).orElse(null);
                timetable.setLecturer(lecturer);
            }
            
            if (timetableDto.getRoomId() != null) {
                Room room = roomRepo.findById(timetableDto.getRoomId()).orElse(null);
                timetable.setRoom(room);
            }
            
            if (timetableDto.getFacultyId() != null) {
                Faculty faculty = facultyRepo.findById(timetableDto.getFacultyId()).orElse(null);
                timetable.setFaculty(faculty);
            }
            
            if (timetableDto.getDepartmentId() != null) {
                Department department = departmentRepo.findById(timetableDto.getDepartmentId()).orElse(null);
                timetable.setDepartment(department);
            }
            
            if (timetableDto.getClassRepUserId() != null) {
                User classRep = userRepo.findById(timetableDto.getClassRepUserId()).orElse(null);
                Long selectedCourseId = timetableDto.getCourseId() != null ? timetableDto.getCourseId() : (timetable.getCourse() != null ? timetable.getCourse().getId() : null);
                if (selectedCourseId == null) {
                    throw new IllegalArgumentException("Class Rep assignment requires a course selection");
                }
                boolean assignedToAnotherCourse = timetableRepo.findAll().stream()
                    .filter(t -> {
                        boolean endedStatus = t.getStatus() != null && t.getStatus().equalsIgnoreCase("ended");
                        boolean inFuture = t.getEndDateTime() == null || t.getEndDateTime().isAfter(LocalDateTime.now());
                        return inFuture && !endedStatus;
                    })
                    .filter(t -> !t.getId().equals(id))
                    .filter(t -> t.getClassRep() != null && t.getClassRep().getId().equals(classRep != null ? classRep.getId() : -1L))
                    .anyMatch(t -> {
                        Long existingCourseId = (t.getCourse() != null ? t.getCourse().getId() : null);
                        return existingCourseId != null && !existingCourseId.equals(selectedCourseId);
                    });
                if (assignedToAnotherCourse) {
                    throw new IllegalArgumentException("Selected Class Rep is already assigned to a different course");
                }
                timetable.setClassRep(classRep);
            }

            if (timetableDto.getIntakeId() != null) {
                Intake intake = intakeRepository.findById(timetableDto.getIntakeId()).orElse(null);
                timetable.setIntake(intake);
            }
            
            Timetable saved = timetableRepo.save(timetable);
            
            // Send notifications about the timetable update
            sendTimetableNotification(saved, "updated");
            
            return mapper.mapTimetableToDto(saved);
        }
        return null;
    }

    @Override
    @Transactional
    public void deleteTimetable(Long id) {
        Optional<Timetable> timetableOpt = timetableRepo.findById(id);
        if (timetableOpt.isPresent()) {
            Timetable timetable = timetableOpt.get();
            
            // Send cancellation notification before deleting
            sendTimetableCancellationNotification(timetable);
            
            // Delete dependent swap requests referencing this timetable (original or proposed)
            try {
                List<SwapRequest> relatedRequests = swapRequestRepo.findRequestsByTimetableId(id);
                if (relatedRequests != null && !relatedRequests.isEmpty()) {
                    swapRequestRepo.deleteAll(relatedRequests);
                }
            } catch (Exception e) {
                System.err.println("Error deleting related swap requests for timetable " + id + ": " + e.getMessage());
            }

            // Soft delete: keep record but hide from calendar (restorable)
            timetable.setStatus("completed");
            timetableRepo.save(timetable);

            // Best-effort: also mark intake/course as completed so it appears in intake completion list
            try {
                Intake intake = timetable.getIntake();
                Course course = timetable.getCourse();
                if (intake != null && intake.getId() != null && course != null && course.getId() != null) {
                    // Choose a fallback admin user if available (endpoint-level admin toggle will set correct admin)
                    User fallbackAdmin = null;
                    try {
                        fallbackAdmin = userRepo.findByRole(UserRole.ADMIN).stream()
                                .filter(u -> u != null && u.getId() != null)
                                .min(Comparator.comparing(User::getId))
                                .orElse(null);
                    } catch (Exception ignored) {}

                    if (fallbackAdmin != null) {
                        var existingOpt = intakeCourseCompletionRepository.findByIntake_IdAndCourse_Id(intake.getId(), course.getId());
                        var completion = existingOpt.orElseGet(com.digital_timetable.entity.IntakeCourseCompletion::new);
                        completion.setIntake(intake);
                        completion.setCourse(course);
                        completion.setMarkedByAdmin(fallbackAdmin);
                        completion.setCompletedAt(LocalDateTime.now());
                        if (completion.getNotes() == null || completion.getNotes().isBlank()) {
                            completion.setNotes("Marked completed by timetable deletion");
                        }
                        intakeCourseCompletionRepository.save(completion);
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to mark completion on soft-delete for timetable " + id + ": " + e.getMessage());
            }
        }
    }
    
    private void sendTimetableCancellationNotification(Timetable timetable) {
        try {
            // Format the date and time for the notification
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            
            String dateStr = timetable.getStartDateTime().format(dateFormatter);
            String startTimeStr = timetable.getStartDateTime().format(timeFormatter);
            
            // Get names from related entities or fallback to string fields
            String courseName = timetable.getCourse() != null ? timetable.getCourse().getCourse_name() : 
                               (timetable.getCourse_name() != null ? timetable.getCourse_name() : "Unknown Course");
            String lecturerName = timetable.getLecturer() != null ? timetable.getLecturer().getName() : 
                                 (timetable.getLecture_name() != null ? timetable.getLecture_name() : "Unknown Lecturer");
            String roomName = timetable.getRoom() != null ? timetable.getRoom().getRoom_name() : 
                             (timetable.getRoom_name() != null ? timetable.getRoom_name() : "Unknown Room");
            
            String title = "Class Ended";
            String message = String.format(
                "A class has been Ended:\n" +
                "Course: %s\n" +
                "\n Lecturer: %s\n" +
                "\n Room: %s\n" +
                "\n Date: %s\n" +
                "\n Time: %s\n" +
                "\n Status: Ended",
                courseName,
                lecturerName,
                roomName,
                dateStr,
                startTimeStr
            );
            
            // Send a single system-wide notification (one notification only)
            notificationService.createSystemNotification(title, message);
            
        } catch (Exception e) {
            // Log error but don't fail the timetable deletion
            System.err.println("Error sending timetable cancellation notification: " + e.getMessage());
        }
    }

    @Override
    public TimetableDto endClass(Long id) {
        Optional<Timetable> optional = timetableRepo.findById(id);
        if (optional.isPresent()) {
            Timetable timetable = optional.get();
            timetable.setStatus("ended");
            
            Timetable saved = timetableRepo.save(timetable);
            
            // Send notification that class has ended
            sendClassEndedNotification(saved);
            
            return mapper.mapTimetableToDto(saved);
        }
        return null;
    }
    
    @Override
    public TimetableDto startClass(Long id) {
        Optional<Timetable> optional = timetableRepo.findById(id);
        if (optional.isPresent()) {
            Timetable timetable = optional.get();
            timetable.setStatus("started");
            
            Timetable saved = timetableRepo.save(timetable);
            
            // Send notification that class has started
            sendClassStartedNotification(saved);
            
            return mapper.mapTimetableToDto(saved);
        }
        return null;
    }
    
    private void sendClassEndedNotification(Timetable timetable) {
        try {
            // Get names from related entities or fallback to string fields
            String courseName = timetable.getCourse() != null ? timetable.getCourse().getCourse_name() : 
                               (timetable.getCourse_name() != null ? timetable.getCourse_name() : "Unknown Course");
            String lecturerName = timetable.getLecturer() != null ? timetable.getLecturer().getName() : 
                                 (timetable.getLecture_name() != null ? timetable.getLecture_name() : "Unknown Lecturer");
            String roomName = timetable.getRoom() != null ? timetable.getRoom().getRoom_name() : 
                             (timetable.getRoom_name() != null ? timetable.getRoom_name() : "Unknown Room");
            
            String title = "Class Ended";
            String message = String.format(
                "A class has ended:\n" +
                "📚 Course: %s\n" +
                "👨‍🏫 Lecturer: %s\n" +
                "🏢 Room: %s\n" +
                "✅ Status: Completed",
                courseName,
                lecturerName,
                roomName
            );
            
            // Send a single system-wide notification (one notification only)
            notificationService.createSystemNotification(title, message);
            
        } catch (Exception e) {
            // Log error but don't fail the class ending
            System.err.println("Error sending class ended notification: " + e.getMessage());
        }
    }
    
    private void sendClassStartedNotification(Timetable timetable) {
        try {
            // Get names from related entities or fallback to string fields
            String courseName = timetable.getCourse() != null ? timetable.getCourse().getCourse_name() : 
                               (timetable.getCourse_name() != null ? timetable.getCourse_name() : "Unknown Course");
            String lecturerName = timetable.getLecturer() != null ? timetable.getLecturer().getName() : 
                                 (timetable.getLecture_name() != null ? timetable.getLecture_name() : "Unknown Lecturer");
            String roomName = timetable.getRoom() != null ? timetable.getRoom().getRoom_name() : 
                             (timetable.getRoom_name() != null ? timetable.getRoom_name() : "Unknown Room");
            
            String title = "Class Started";
            String message = String.format(
                "A class has started:\n" +
                "📚 Course: %s\n" +
                "👨‍🏫 Lecturer: %s\n" +
                "🏢 Room: %s\n" +
                "🟢 Status: In Progress",
                courseName,
                lecturerName,
                roomName
            );
            
            // Send notification to all students (USER role)
            notificationService.broadcastNotificationToRole(title, message, UserRole.USER.name());
            
            // Also send to class representatives
            notificationService.broadcastNotificationToRole(title, message, UserRole.CLASS_REPRESENT.name());
            
            // Send system notification
            notificationService.createSystemNotification(title, message);
            
        } catch (Exception e) {
            // Log error but don't fail the class starting
            System.err.println("Error sending class started notification: " + e.getMessage());
        }
    }

    @Override
    public TimetableDto assignClassRep(Long timetableId, Long userId) {
        Optional<Timetable> optional = timetableRepo.findById(timetableId);
        if (optional.isPresent()) {
            Timetable timetable = optional.get();
            
            // Set the ClassRep relationship
            User classRep = userRepo.findById(userId).orElse(null);
            timetable.setClassRep(classRep);
            
            return mapper.mapTimetableToDto(timetableRepo.save(timetable));
        }
        return null;
    }
} 