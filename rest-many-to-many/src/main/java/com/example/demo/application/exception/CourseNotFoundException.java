package com.example.demo.application.exception;

public class CourseNotFoundException extends RuntimeException {

    public CourseNotFoundException(Long id) {
        super("Course #" + id + " was not found.");
    }
}
