package com.example.demo.interfaces;

import com.example.demo.DemoApplication;
import com.example.demo.application.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.ResponseEntity.status;

@RestControllerAdvice(basePackageClasses = DemoApplication.class)
public class RestExceptionHandler {

    @ExceptionHandler(value = {
            StudentNotFoundException.class,
            CourseNotFoundException.class
    })
    ResponseEntity handleNotFoundException(Exception exception) {
        return status(HttpStatus.NOT_FOUND).body(new com.example.demo.interfaces.dto.Error("not_found", exception.getMessage()));
    }

    @ExceptionHandler(value = {
            StudentAlreadyRegisteredCourseException.class,
            StudentNeverRegisteredCourseException.class,
            StudentsOfCourseReachedMaxLimitationException.class,
            CoursesOfStudentReachedMaxLimitationException.class
    })
    ResponseEntity handleReachedMaxLimitationException(Exception exception) {
        return status(HttpStatus.BAD_REQUEST).body(new com.example.demo.interfaces.dto.Error("invalid_request", exception.getMessage()));
    }
}
