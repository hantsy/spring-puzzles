package com.example.demo.application.exception;

public class CoursesOfStudentReachedMaxLimitationException extends RuntimeException {

    public CoursesOfStudentReachedMaxLimitationException(Long id) {
        super("The registered courses of Student#" + id + "has reached the max limitation");
    }
}
