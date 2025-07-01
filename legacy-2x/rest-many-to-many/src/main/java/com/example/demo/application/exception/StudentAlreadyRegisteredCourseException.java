package com.example.demo.application.exception;

public class StudentAlreadyRegisteredCourseException extends RuntimeException {

    public StudentAlreadyRegisteredCourseException(Long id, Long courseId) {
        super("Student #" + id + " has registered Course #" + courseId);
    }
}
