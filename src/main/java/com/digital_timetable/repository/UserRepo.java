package com.digital_timetable.repository;

import com.digital_timetable.entity.User;
import com.digital_timetable.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepo extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    
    List<User> findByRole(UserRole role);
}
