package com.example.demo;

import com.example.demo.domain.Course;
import com.example.demo.domain.Student;
import com.example.demo.infra.CourseRepository;
import com.example.demo.infra.StudentRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest// test against embedded H2 database.
@Slf4j
public class RepositoryTest {

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    TransactionTemplate txTemplate;

    @Autowired
    TestEntityManager testEntityManager;

    @BeforeEach
    public void setup() {
        //this.studentRepository.deleteAllInBatch();
        //this.courseRepository.deleteAllInBatch();
        var savedStudent = this.studentRepository.save(Student.builder().name("Test student").build());
        var savedCourse = this.courseRepository.save(Course.builder().title("Test Course").build());

        log.debug("saved student: {} ", savedStudent);
        log.debug("saved course: {} ", savedCourse);
    }

    @AfterEach
    public void teardown() {

    }

    @Test
    public void addStudentToCourse() {

        List<Student> students = this.studentRepository.findAll();

        assertThat(students.size()).isEqualTo(1);
        var s = students.get(0);

        List<Course> courses = this.courseRepository.findAll();

        assertThat(courses.size()).isEqualTo(1);
        var c = courses.get(0);
        txTemplate.executeWithoutResult(
                tx -> {
                    c.addStudent(s);
                }
        );

        assertThat(this.courseRepository.getById(c.getId()).getStudents().size()).isEqualTo(1);
        assertThat(this.studentRepository.getById(s.getId()).getCourses().size()).isEqualTo(1);

        txTemplate.executeWithoutResult(
                tx -> {
                    c.removeStudent(s);
                }
        );

        assertThat(this.courseRepository.getById(c.getId()).getStudents().size()).isEqualTo(0);
        assertThat(this.studentRepository.getById(s.getId()).getCourses().size()).isEqualTo(0);
    }


}
