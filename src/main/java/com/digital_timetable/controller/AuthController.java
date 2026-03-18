package com.digital_timetable.controller;


import com.digital_timetable.dto.LoginRequest;
import com.digital_timetable.dto.Response;
import com.digital_timetable.dto.UserDto;
import com.digital_timetable.entity.User;
import com.digital_timetable.service.interf.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<Response> registerUser(@RequestBody UserDto registrationRequest){
        System.out.println(registrationRequest);
        return ResponseEntity.ok(userService.registerUser(registrationRequest));
    }
    
    @PostMapping("/login")
    public ResponseEntity<Response> loginUser(@RequestBody LoginRequest loginRequest){
        return ResponseEntity.ok(userService.loginUser(loginRequest));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
                // Get user from database using email
                User user = userService.getUserByEmail(authentication.getName());
                
                // Return user data without sensitive information
                return ResponseEntity.ok(Map.of(
                    "id", user.getId(),
                    "name", user.getName(),
                    "email", user.getEmail(),
                    "phoneNumber", user.getPhoneNumber(),
                    "role", user.getRole(),
                    "active", user.isActive()
                ));
            } else {
                return ResponseEntity.status(401).body("Not authenticated");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error getting current user: " + e.getMessage());
        }
    }
}
