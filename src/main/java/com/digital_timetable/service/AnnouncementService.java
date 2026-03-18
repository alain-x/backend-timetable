package com.digital_timetable.service;

import com.digital_timetable.model.Announcement;
import com.digital_timetable.repository.AnnouncementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.digital_timetable.model.AnnouncementRole;

@Service
public class AnnouncementService {
    @Autowired
    private AnnouncementRepository announcementRepository;

    public Announcement createAnnouncement(String title, String message, String createdBy, List<String> roles) {
        Announcement announcement = new Announcement(title, message, createdBy, LocalDateTime.now(), roles);
        // Map roles -> AnnouncementRole entities with back-reference
        List<AnnouncementRole> roleEntities = new ArrayList<>();
        for (String r : roles) {
            if (r == null) continue;
            AnnouncementRole ar = new AnnouncementRole(r, announcement);
            roleEntities.add(ar);
        }
        announcement.setRoles(roleEntities);
        return announcementRepository.save(announcement);
    }

    public List<Announcement> getAllAnnouncements() {
        return announcementRepository.findAll();
    }
}
