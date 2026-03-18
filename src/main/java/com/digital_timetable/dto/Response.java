package com.digital_timetable.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.digital_timetable.entity.User;
import com.digital_timetable.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {

    private int status;
    private String message;
    private final LocalDateTime timestamp = LocalDateTime.now();
    private Object data;

    private String token;
    private UserRole role;
    private String expirationTime;
    private Long userId;

    private int totalPage;
    private long totalElement;

    private User user;
    private List<User> userList;










}
