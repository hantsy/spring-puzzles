package com.example.demo.application.exception;

public class StudentsOfCourseReachedMaxLimitationException extends RuntimeException {

    public StudentsOfCourseReachedMaxLimitationException(Long id) {
        super("The students of Course #" + id + "has reached the max limitation");
    }
}
