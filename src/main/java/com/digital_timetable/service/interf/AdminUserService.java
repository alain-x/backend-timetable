package com.digital_timetable.service.interf;

import com.digital_timetable.entity.User;

public interface AdminUserService {
 
    User createAdminUser(User user);

    User createStaffUser(User user1);
    
    User createUser(User user);
    
    User updateUser(User user);
    
    User toggleUserStatus(Long userId);
    
    void deleteUser(Long userId);
}
