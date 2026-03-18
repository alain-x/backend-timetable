package com.digital_timetable.entity;

import com.digital_timetable.enums.Program;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "timetables")
public class Timetable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // String fields for backward compatibility
    private String course_name;
    private String faculty_name;
    private String department_name;
    private String lecture_name;
    private String room_name;
    
    private String title;
    private String description;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String color;
    private String recurrence; // NONE, DAILY, WEEKLY, etc.
    private String notes;
    private Program section;
    private int hours;
    private String status; // scheduled, started, ended

    // JPA Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    @JsonBackReference
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecturer_id")
    @JsonBackReference
    private User lecturer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    @JsonBackReference
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id")
    @JsonBackReference
    private Faculty faculty;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    @JsonBackReference
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_rep_id")
    @JsonBackReference
    private User classRep;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intake_id")
    @JsonBackReference
    private Intake intake;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCourse_name() { return course_name; }
    public void setCourse_name(String course_name) { this.course_name = course_name; }
    public String getFaculty_name() { return faculty_name; }
    public void setFaculty_name(String faculty_name) { this.faculty_name = faculty_name; }
    public String getDepartment_name() { return department_name; }
    public void setDepartment_name(String department_name) { this.department_name = department_name; }
    public String getLecture_name() { return lecture_name; }
    public void setLecture_name(String lecture_name) { this.lecture_name = lecture_name; }
    public String getRoom_name() { return room_name; }
    public void setRoom_name(String room_name) { this.room_name = room_name; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getStartDateTime() { return startDateTime; }
    public void setStartDateTime(LocalDateTime startDateTime) { this.startDateTime = startDateTime; }
    public LocalDateTime getEndDateTime() { return endDateTime; }
    public void setEndDateTime(LocalDateTime endDateTime) { this.endDateTime = endDateTime; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public String getRecurrence() { return recurrence; }
    public void setRecurrence(String recurrence) { this.recurrence = recurrence; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Program getSection() { return section; }
    public void setSection(Program section) { this.section = section; }
    public int getHours() { return hours; }
    public void setHours(int hours) { this.hours = hours; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // Getters and setters for JPA relationships
    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
    public User getLecturer() { return lecturer; }
    public void setLecturer(User lecturer) { this.lecturer = lecturer; }
    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }
    public Faculty getFaculty() { return faculty; }
    public void setFaculty(Faculty faculty) { this.faculty = faculty; }
    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
    public User getClassRep() { return classRep; }
    public void setClassRep(User classRep) { this.classRep = classRep; }
    public Intake getIntake() { return intake; }
    public void setIntake(Intake intake) { this.intake = intake; }
}
