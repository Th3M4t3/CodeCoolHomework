package com.example.codecool.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(StudentNotFoundException.class)
    public ResponseEntity<ExceptionObject> handleStudentNotFoundException(StudentNotFoundException e, WebRequest request) {
        ExceptionObject exceptionObject = new ExceptionObject();
        exceptionObject.setStatusCode(HttpStatus.NOT_FOUND.value());
        exceptionObject.setMessage(e.getMessage());
        exceptionObject.setTimestamp(new Date());
        return new ResponseEntity<>(exceptionObject, HttpStatus.NOT_FOUND);
    }
}
