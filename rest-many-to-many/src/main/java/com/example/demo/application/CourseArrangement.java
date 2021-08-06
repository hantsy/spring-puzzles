package com.example.demo.application;

import com.example.demo.application.exception.CourseNotFoundException;
import com.example.demo.application.exception.StudentNotFoundException;
import com.example.demo.domain.Course;
import com.example.demo.domain.Student;
import com.example.demo.infra.CourseRepository;
import com.example.demo.infra.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
//@Transactional is more common, and simplifies the tx work.
public class CourseArrangement {
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
    private final TransactionTemplate txTemplate;//add fine-grained tx control.

    public void addStudentToCourse(Long studentId, Long courseId) {
        txTemplate.executeWithoutResult(tx -> {
            Course course = this.courseRepository.findById(courseId).orElseThrow(() -> new CourseNotFoundException(courseId));
            Student student = this.studentRepository.findById(studentId).orElseThrow(() -> new StudentNotFoundException(studentId));
            course.addStudent(student);
        });
    }

    public void removeStudentFromCourse(Long studentId, Long courseId) {
        txTemplate.executeWithoutResult(tx -> {
            Course course = this.courseRepository.findById(courseId).orElseThrow(() -> new CourseNotFoundException(courseId));
            Student student = this.studentRepository.findById(studentId).orElseThrow(() -> new StudentNotFoundException(studentId));
            course.removeStudent(student);
        });
    }
}
