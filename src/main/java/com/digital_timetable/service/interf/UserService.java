package com.digital_timetable.service.interf;

import com.digital_timetable.dto.LoginRequest;
import com.digital_timetable.dto.Response;
import com.digital_timetable.dto.UserDto;
import com.digital_timetable.entity.User;

public interface UserService {
    Response registerUser(UserDto registrationRequest);
    Response loginUser(LoginRequest loginRequest);
    User getUserByEmail(String email);
}
