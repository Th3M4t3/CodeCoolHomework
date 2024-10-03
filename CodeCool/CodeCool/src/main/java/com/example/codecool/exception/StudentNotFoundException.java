package com.example.codecool.exception;

public class StudentNotFoundException extends RuntimeException{
    private static final Long serialVersionUID = 1L;
    public StudentNotFoundException(String message){
        super(message);
    }
}

