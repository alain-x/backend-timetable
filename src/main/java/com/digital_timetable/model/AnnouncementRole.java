package com.digital_timetable.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "announcement_roles")
public class AnnouncementRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "roles")
    private String role; // e.g., STUDENT, CLASS_REPRESENT, LECTURER, ADMIN, ALL

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "announcement_id", nullable = false)
    @JsonBackReference
    private Announcement announcement;

    public AnnouncementRole() {}

    public AnnouncementRole(String role, Announcement announcement) {
        this.role = role;
        this.announcement = announcement;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Announcement getAnnouncement() { return announcement; }
    public void setAnnouncement(Announcement announcement) { this.announcement = announcement; }
}
