package com.digital_timetable.service.impl;

import com.digital_timetable.dto.LoginRequest;
import com.digital_timetable.dto.Response;
import com.digital_timetable.dto.UserDto;
import com.digital_timetable.entity.User;
import com.digital_timetable.enums.UserRole;
import com.digital_timetable.exception.InvalidCredentialsException;
import com.digital_timetable.exception.NotFoundException;
import com.digital_timetable.mapper.EntityDtoMapper;
import com.digital_timetable.repository.UserRepo;
import com.digital_timetable.security.JwtUtils;
import com.digital_timetable.service.interf.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final EntityDtoMapper entityDtoMapper;

    private static final String DEFAULT_ADMIN_EMAIL = "alainvava54@gmail.com";
    private static final String DEFAULT_ADMIN_PASSWORD = "Yo221122!!"; // You may change this to a more secure password
    private static final String DEFAULT_ADMIN_NAME = "Admin";
    private static final String DEFAULT_ADMIN_PHONE = "0782407887";
    private static final UserRole DEFAULT_ADMIN_ROLE = UserRole.ADMIN;



    @Override
    public Response registerUser(UserDto registrationRequest) {
        // Default role is USER
        UserRole role = UserRole.USER;

        // Check if a role is provided and try to match it case-insensitively
        if (registrationRequest.getRole() != null) {
            role = getUserRoleFromString(String.valueOf(registrationRequest.getRole()));
        }

        User user = User.builder()
                .name(registrationRequest.getName())
                .email(registrationRequest.getEmail())
                .password(passwordEncoder.encode(registrationRequest.getPassword()))
                .phoneNumber(registrationRequest.getPhoneNumber())
                .role(role)
                .build();

        User savedUser = userRepo.save(user);
        System.out.println(savedUser);

        UserDto userDto = entityDtoMapper.mapUserToDtoBasic(savedUser);
        return Response.builder()
                .status(200)
                .message("User Successfully Added")
                .role(role)
                .build();
    }

    // Utility method to safely convert a string to UserRole enum
    private UserRole getUserRoleFromString(String roleString) {
        // Check for null and try to match the string to enum values
        try {
            return UserRole.valueOf(roleString.toUpperCase()); // Convert string to enum
        } catch (IllegalArgumentException e) {
            // Handle invalid role (for example, if "adminstrator" was passed in)
            throw new IllegalArgumentException("Invalid role: " + roleString);
        }
    }



    @Override
    public Response loginUser(LoginRequest loginRequest) {
        User user = userRepo.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new NotFoundException("Email not found"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Password does not match");
        }

        // Block inactive accounts from logging in
        if (!Boolean.TRUE.equals(user.isActive())) {
            return Response.builder()
                    .status(403)
                    .message("Your account is inactive. Please contact Administration for help.")
                    .build();
        }

        String token = jwtUtils.generateToken(user);
        return Response.builder()
                .status(200)
                .message("User Successfully Logged In")
                .token(token)
                .expirationTime("1 hour")
                .role(UserRole.valueOf(user.getRole().name()))
                .userId(user.getId())
                .build();
    }




    // Check if an admin user already exists and create if not
    public void createAdminIfNotExists() {
        Optional<User> existingAdmin = findUserByEmail(DEFAULT_ADMIN_EMAIL);

        if (existingAdmin.isEmpty()) {
            // Create default admin user
            User admin = User.builder()
                    .name(DEFAULT_ADMIN_NAME)
                    .email(DEFAULT_ADMIN_EMAIL)
                    .password(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD))
                    .phoneNumber(DEFAULT_ADMIN_PHONE)
                    .role(DEFAULT_ADMIN_ROLE)
                    .active(true)
                    .build();

            saveUser(admin);
            log.info("Default admin user created with email: " + DEFAULT_ADMIN_EMAIL);
        } else {
            log.info("Admin user already exists with email: " + DEFAULT_ADMIN_EMAIL);
        }
    }

    // Find a user by email
    public Optional<User> findUserByEmail(String email) {

        return userRepo.findByEmail(email);
    }

    // Save a user to the database
    public void saveUser(User user) {

        userRepo.save(user);
    }

    public void setActive(Long userId, boolean isActive) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(isActive);
        userRepo.save(user);
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
    }
}