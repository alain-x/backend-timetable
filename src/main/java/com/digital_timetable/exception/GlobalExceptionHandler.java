package com.digital_timetable.exception;


import com.digital_timetable.dto.Response;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response> handleAllException(Exception ex, WebRequest request){
        Response errorResponse = Response.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message(ex.getMessage())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Response> handleNotFoundException(NotFoundException ex, WebRequest request){
        Response errorResponse = Response.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .message(ex.getMessage())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Response> handleInvalidCredentialsExceptionException(InvalidCredentialsException ex, WebRequest request){
        Response errorResponse = Response.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(ex.getMessage())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Response> handleDuplicateEntryException(DataIntegrityViolationException ex, WebRequest request) {
        if (ex.getMessage().toLowerCase().contains("duplicate entry")) {
            Response errorResponse = Response.builder()
                    .status(HttpStatus.CONFLICT.value()) // 409 Conflict
                    .message("User already exists. Please log in.")
                    .build();
            return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
        }
        Response errorResponse = Response.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Database error occurred.")
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }


}
