package com.example.demo.application.exception;

public class StudentNeverRegisteredCourseException extends RuntimeException {

    public StudentNeverRegisteredCourseException(Long id, Long courseId) {
        super("Student #" + id + " has never registered Course #" + courseId);
    }
}
