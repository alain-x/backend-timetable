package com.digital_timetable.mapper;

import com.digital_timetable.dto.LoginRequest;
import com.digital_timetable.dto.Response;
import com.digital_timetable.dto.UserDto;
import com.digital_timetable.dto.DepartmentDto;
import com.digital_timetable.dto.FacultyDto;
import com.digital_timetable.dto.RoomDto;
import com.digital_timetable.dto.TimetableDto;
import com.digital_timetable.dto.CourseDto;
import com.digital_timetable.entity.User;
import com.digital_timetable.enums.UserRole;
import com.digital_timetable.entity.Department;
import com.digital_timetable.entity.Faculty;
import com.digital_timetable.entity.Room;
import com.digital_timetable.entity.Timetable;
import com.digital_timetable.entity.Course;
import com.digital_timetable.entity.Intake;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class EntityDtoMapper {

    // Convert User entity to UserDto (basic version)
    public UserDto mapUserToDtoBasic(User user) {
        if (user == null) {
            return null;
        }

        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setPhoneNumber(user.getPhoneNumber());
        userDto.setEmail(user.getEmail());
        userDto.setRole(UserRole.valueOf(user.getRole().name())); // Assuming the role is a string in the DTO
        userDto.setName(user.getName());
        userDto.setActive(user.isActive()); // Ensure active status is included if needed

        return userDto;
    }

    // Convert UserDto to User entity
    public User mapDtoToUser(UserDto userDto) {
        if (userDto == null) {
            return null;
        }

        User user = new User();
        user.setId(userDto.getId());
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setPhoneNumber(userDto.getPhoneNumber());
        user.setPassword(userDto.getPassword()); // Note: Password should be encoded when saving
        user.setRole(userDto.getRole()); // Assuming role is passed as a string in the DTO
        user.setActive(userDto.isActive()); // Ensure active status is set if needed

        return user;
    }

    // Convert User entity to UserDto with all fields (if needed)
    public UserDto mapUserToDtoFull(User user) {
        if (user == null) {
            return null;
        }

        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setPhoneNumber(user.getPhoneNumber());
        userDto.setEmail(user.getEmail());
        userDto.setRole(UserRole.valueOf(user.getRole().name())); // Assuming the role is a string in the DTO
        userDto.setName(user.getName());
        userDto.setActive(user.isActive());
        userDto.setPassword(user.getPassword()); // You may or may not want to include the password in DTOs

        return userDto;
    }

    // Convert LoginRequest DTO to User entity (for login)
    public User mapLoginRequestToUser(LoginRequest loginRequest) {
        if (loginRequest == null) {
            return null;
        }

        User user = new User();
        user.setEmail(loginRequest.getEmail());
        user.setPassword(loginRequest.getPassword()); // Note: Password should be encoded when saving
        return user;
    }

    // Convert Response DTO to User entity if needed
    public Response mapUserToResponse(User user) {
        UserDto userDto = mapUserToDtoBasic(user); // You can choose the appropriate mapping here

        return Response.builder()
                .status(200)
                .message("User found")

                .build();
    }

    // Department
    public DepartmentDto mapDepartmentToDto(Department department) {
        if (department == null) return null;
        DepartmentDto dto = new DepartmentDto();
        dto.setId(department.getId());
        dto.setDepartment_name(department.getDepartment_name());
        
        // Map faculty relationship
        if (department.getFaculty() != null) {
            dto.setFacultyId(department.getFaculty().getId());
            dto.setFacultyName(department.getFaculty().getFaculty_name());
        }
        
        return dto;
    }
    
    public Department mapDtoToDepartment(DepartmentDto dto) {
        if (dto == null) return null;
        Department department = new Department();
        department.setId(dto.getId());
        department.setDepartment_name(dto.getDepartment_name());
        // Set faculty if facultyId is present
        if (dto.getFacultyId() != null) {
            // You may need to inject FacultyRepo or pass Faculty as a parameter
            // department.setFaculty(facultyRepo.findById(dto.getFacultyId()).orElse(null));
            // For now, just set a Faculty with the ID (to be replaced in service layer)
            Faculty faculty = new Faculty();
            faculty.setId(dto.getFacultyId());
            department.setFaculty(faculty);
        }
        return department;
    }
    
    // Faculty
    public FacultyDto mapFacultyToDto(Faculty faculty) {
        if (faculty == null) return null;
        FacultyDto dto = new FacultyDto();
        dto.setId(faculty.getId());
        dto.setFaculty_name(faculty.getFaculty_name());
        return dto;
    }
    
    public Faculty mapDtoToFaculty(FacultyDto dto) {
        if (dto == null) return null;
        Faculty faculty = new Faculty();
        faculty.setId(dto.getId());
        faculty.setFaculty_name(dto.getFaculty_name());
        return faculty;
    }
    
    // Room
    public RoomDto mapRoomToDto(Room room) {
        if (room == null) return null;
        RoomDto dto = new RoomDto();
        dto.setId(room.getId());
        dto.setRoom_name(room.getRoom_name());
        dto.setBlock_name(room.getBlock_name());
        dto.setLocation(room.getLocation());
        dto.setCapacity(room.getCapacity());
        dto.setBooked(room.isBooked());
        // Derive who booked the room, if applicable
        try {
            if (room.isBooked() && room.getTimetables() != null && !room.getTimetables().isEmpty()) {
                // Pick the most recent timetable by startDateTime that references this room
                Timetable latest = null;
                for (Timetable t : room.getTimetables()) {
                    if (t == null) continue;
                    if (latest == null) {
                        latest = t;
                    } else if (t.getStartDateTime() != null &&
                            (latest.getStartDateTime() == null || t.getStartDateTime().isAfter(latest.getStartDateTime()))) {
                        latest = t;
                    }
                }
                if (latest != null && latest.getClassRep() != null) {
                    dto.setBookedByClassRepName(latest.getClassRep().getName());
                }
            }
        } catch (Exception ignored) {
            // Best-effort enrichment; ignore any lazy-loading or null issues
        }
        return dto;
    }
    
    public Room mapDtoToRoom(RoomDto dto) {
        if (dto == null) return null;
        Room room = new Room();
        room.setId(dto.getId());
        room.setRoom_name(dto.getRoom_name());
        room.setBlock_name(dto.getBlock_name());
        room.setLocation(dto.getLocation());
        room.setCapacity(dto.getCapacity());
        room.setBooked(dto.isBooked());
        return room;
    }
    
    // Timetable
    public TimetableDto mapTimetableToDto(Timetable timetable) {
        if (timetable == null) return null;
        TimetableDto dto = new TimetableDto();
        dto.setId(timetable.getId());
        dto.setTitle(timetable.getTitle());
        dto.setDescription(timetable.getDescription());
        dto.setStartDateTime(timetable.getStartDateTime() != null ? timetable.getStartDateTime().toString() : null);
        dto.setEndDateTime(timetable.getEndDateTime() != null ? timetable.getEndDateTime().toString() : null);
        dto.setColor(timetable.getColor());
        dto.setRecurrence(timetable.getRecurrence());
        dto.setNotes(timetable.getNotes());
        dto.setCourse_name(timetable.getCourse_name());
        dto.setFaculty_name(timetable.getFaculty_name());
        dto.setDepartment_name(timetable.getDepartment_name());
        dto.setLecture_name(timetable.getLecture_name());
        dto.setRoom_name(timetable.getRoom_name());
        dto.setSection(timetable.getSection());
        dto.setHours(timetable.getHours());
        dto.setStatus(timetable.getStatus());
        
        // Map relationships
        if (timetable.getCourse() != null) {
            dto.setCourseId(timetable.getCourse().getId());
            dto.setCourseCode(timetable.getCourse().getCourse_code());
        }
        
        if (timetable.getLecturer() != null) {
            dto.setLecturerId(timetable.getLecturer().getId());
            dto.setLecturerName(timetable.getLecturer().getName());
        }
        
        if (timetable.getRoom() != null) {
            dto.setRoomId(timetable.getRoom().getId());
            dto.setRoomBlock(timetable.getRoom().getBlock_name());
            dto.setRoomLocation(timetable.getRoom().getLocation());
        }
        
        if (timetable.getFaculty() != null) {
            dto.setFacultyId(timetable.getFaculty().getId());
        }
        
        if (timetable.getDepartment() != null) {
            dto.setDepartmentId(timetable.getDepartment().getId());
        }
        
        if (timetable.getClassRep() != null) {
            dto.setClassRepUserId(timetable.getClassRep().getId());
            dto.setClassRepName(timetable.getClassRep().getName());
        }

        if (timetable.getIntake() != null) {
            dto.setIntakeId(timetable.getIntake().getId());
            dto.setIntakeName(timetable.getIntake().getName());
        }
        
        return dto;
    }
    
    public Timetable mapDtoToTimetable(TimetableDto dto) {
        if (dto == null) return null;
        Timetable timetable = new Timetable();
        timetable.setId(dto.getId());
        timetable.setTitle(dto.getTitle());
        timetable.setDescription(dto.getDescription());
        timetable.setStartDateTime(dto.getStartDateTime() != null ? LocalDateTime.parse(dto.getStartDateTime()) : null);
        timetable.setEndDateTime(dto.getEndDateTime() != null ? LocalDateTime.parse(dto.getEndDateTime()) : null);
        timetable.setColor(dto.getColor());
        timetable.setRecurrence(dto.getRecurrence());
        timetable.setNotes(dto.getNotes());
        timetable.setCourse_name(dto.getCourse_name());
        timetable.setFaculty_name(dto.getFaculty_name());
        timetable.setDepartment_name(dto.getDepartment_name());
        timetable.setLecture_name(dto.getLecture_name());
        timetable.setRoom_name(dto.getRoom_name());
        timetable.setSection(dto.getSection());
        timetable.setHours(dto.getHours());
        timetable.setStatus(dto.getStatus());

        if (dto.getIntakeId() != null) {
            Intake intake = new Intake();
            intake.setId(dto.getIntakeId());
            timetable.setIntake(intake);
        }
        return timetable;
    }
    
    // Course
    public CourseDto mapCourseToDto(Course course) {
        if (course == null) return null;
        CourseDto dto = new CourseDto();
        dto.setId(course.getId());
        dto.setCourse_name(course.getCourse_name());
        dto.setCourse_code(course.getCourse_code());
        dto.setCourse_credit(course.getCourse_credit());
        
        // Map faculty relationship
        if (course.getFaculty() != null) {
            dto.setFacultyId(course.getFaculty().getId());
            dto.setFacultyName(course.getFaculty().getFaculty_name());
        }
        
        // Map department relationship (single)
        if (course.getDepartment() != null) {
            dto.setDepartmentId(course.getDepartment().getId());
            dto.setDepartmentName(course.getDepartment().getDepartment_name());
        }

        if (course.getIntake() != null) {
            dto.setIntakeId(course.getIntake().getId());
            dto.setIntakeName(course.getIntake().getName());
        }
        
        return dto;
    }
    
    public Course mapDtoToCourse(CourseDto dto) {
        if (dto == null) return null;
        Course course = new Course();
        course.setId(dto.getId());
        course.setCourse_name(dto.getCourse_name());
        course.setCourse_code(dto.getCourse_code());
        course.setCourse_credit(dto.getCourse_credit());

        if (dto.getIntakeId() != null) {
            Intake intake = new Intake();
            intake.setId(dto.getIntakeId());
            course.setIntake(intake);
        }
        return course;
    }
}
