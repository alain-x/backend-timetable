package com.digital_timetable;

import com.digital_timetable.service.impl.UserServiceImpl;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    private final UserServiceImpl userService;

    public AdminInitializer(UserServiceImpl userService) {
        this.userService = userService;
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            // Ensure the admin user exists
            userService.createAdminIfNotExists();
        } catch (Exception e) {
            // Log the error but don't fail startup
            System.err.println("Warning: Could not initialize admin user: " + e.getMessage());
        }
    }
}
