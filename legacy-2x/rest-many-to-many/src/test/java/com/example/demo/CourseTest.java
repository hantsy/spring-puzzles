package com.example.demo;

import com.example.demo.application.exception.CoursesOfStudentReachedMaxLimitationException;
import com.example.demo.application.exception.StudentAlreadyRegisteredCourseException;
import com.example.demo.application.exception.StudentNeverRegisteredCourseException;
import com.example.demo.application.exception.StudentsOfCourseReachedMaxLimitationException;
import com.example.demo.domain.Course;
import com.example.demo.domain.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CourseTest {

    List<Course> courses;
    List<Student> students;

    @BeforeEach
    public void setup() {
        List<Student> studentList = IntStream.rangeClosed(1, 50)
                .mapToObj(i -> Student.builder().id((long) i).name("test student #" + i).build())
                .toList();//unmodifiable list
        this.students = new ArrayList<>(studentList);//change to modifiable


        List<Course> courseList = IntStream.rangeClosed(1, 5)
                .mapToObj(i -> Course.builder().id((long) i).title("test course #" + i).build())
                .toList();

        this.courses = new ArrayList<>(courseList);
    }

    @Test
    public void testStudentsOfCourseReachedMaxLimitationException() {
        Course course = Course.builder().id(100L).title("test course #100").build();

        this.students.forEach(course::addStudent);

        Student testStudent = Student.builder().id(100L).name("new test student").build();

        assertThatThrownBy(() -> course.addStudent(testStudent))
                .isInstanceOf(StudentsOfCourseReachedMaxLimitationException.class);

    }


    @Test
    public void testCoursesOfStudentReachedMaxLimitationException() {

        Student testStudent = Student.builder().id(100L).name("new test student").build();
        this.courses.forEach(c -> c.addStudent(testStudent));

        Course course = Course.builder().id(100L).title("test course #100").build();

        assertThatThrownBy(() -> course.addStudent(testStudent))
                .isInstanceOf(CoursesOfStudentReachedMaxLimitationException.class);

    }

    @Test
    public void testStudentAlreadyRegisteredCourseException() {
        Course course = Course.builder().id(100L).title("test course #100").build();

        this.students.forEach(course::addStudent);

        Student testStudent = Student.builder().id(1L).name("test student #1").build();//duplicated item.

        assertThatThrownBy(() -> course.addStudent(testStudent))
                .isInstanceOf(StudentAlreadyRegisteredCourseException.class);

    }

    @Test
    public void testStudentNeverRegisteredCourseException() {
        Course course = Course.builder().id(100L).title("test course #100").build();

        this.students.forEach(course::addStudent);

        Student testStudent = Student.builder().id(100L).name("new test student").build();//none existing student

        assertThatThrownBy(() -> course.removeStudent(testStudent))
                .isInstanceOf(StudentNeverRegisteredCourseException.class);

    }
}
