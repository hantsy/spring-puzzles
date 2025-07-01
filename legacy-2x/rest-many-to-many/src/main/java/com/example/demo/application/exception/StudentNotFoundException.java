package com.example.demo.application.exception;

public class StudentNotFoundException extends RuntimeException {

    public StudentNotFoundException(Long id) {
        super("Student #" + id + " was not found.");
    }
}
