package com.digital_timetable.service.interf;

public interface AdminService {
    void activateAccount(Long userId);

    void deactivateAccount(Long userId);

    void deleteUserAccount(Long userId);
}
