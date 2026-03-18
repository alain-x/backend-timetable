package com.digital_timetable.service.impl;

import com.digital_timetable.entity.User;
import com.digital_timetable.repository.UserRepo;
import com.digital_timetable.service.interf.AdminService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepo userRepo;

    public void activateAccount(Long userId) {
        User user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(true);
        userRepo.save(user);
    }

    @Transactional
    public void deactivateAccount(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setActive(false);

        userRepo.save(user); // This should trigger a database update
        System.out.println("User " + userId + " deactivated: " + user.isActive()); // Debugging log
    }

    public void deleteUserAccount(Long userId) {
        User user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        userRepo.delete(user);
    }
}
