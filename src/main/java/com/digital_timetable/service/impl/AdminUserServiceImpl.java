package com.digital_timetable.service.impl;

import com.digital_timetable.entity.User;
import com.digital_timetable.enums.UserRole;
import com.digital_timetable.repository.UserRepo;
import com.digital_timetable.service.interf.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User createAdminUser(User user) {
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        user.setRole(UserRole.ADMIN);
        user.setActive(true);
        return userRepo.save(user);
    }

    @Override
    public User createStaffUser(User user) {
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        user.setRole(UserRole.STAFF);
        user.setActive(true);
        return userRepo.save(user);
    }
    
    @Override
    public User createUser(User user) {
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        user.setActive(true);
        return userRepo.save(user);
    }
    
    @Override
    public User updateUser(User user) {
        User existingUser = userRepo.findById(user.getId())
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());
        existingUser.setPhoneNumber(user.getPhoneNumber());
        existingUser.setRole(user.getRole());
        
        // Only update password if provided
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            String encodedPassword = passwordEncoder.encode(user.getPassword());
            existingUser.setPassword(encodedPassword);
        }
        
        return userRepo.save(existingUser);
    }
    
    @Override
    public User toggleUserStatus(Long userId) {
        User user = userRepo.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setActive(!user.isActive());
        return userRepo.save(user);
    }
    
    @Override
    public void deleteUser(Long userId) {
        User user = userRepo.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        userRepo.delete(user);
    }
}

