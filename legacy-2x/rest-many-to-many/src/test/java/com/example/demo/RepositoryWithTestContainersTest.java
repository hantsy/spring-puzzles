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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@Slf4j
public class RepositoryWithTestContainersTest {

    @Container
    static MySQLContainer mysql = new MySQLContainer(DockerImageName.parse("mysql"));

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    TransactionTemplate txTemplate;

    @PersistenceContext
    EntityManager testEntityManager;

    @BeforeEach
    public void setup() {
        txTemplate.executeWithoutResult(
                tx -> {
                    log.debug("clear data in tx");
                    this.studentRepository.findAll().forEach(
                            s -> s.getCourses().clear()
                    );
                    this.courseRepository.findAll().forEach(
                            c -> c.getStudents().clear()
                    );
                    this.studentRepository.deleteAll();
                    this.courseRepository.deleteAll();
                }
        );

        var savedStudent = this.studentRepository.save(Student.builder().name("Test student").build());
        var savedCourse = this.courseRepository.save(Course.builder().title("Test Course").build());

        log.debug("saved student: {} ", savedStudent);
        log.debug("saved course: {} ", savedCourse);
    }

    @AfterEach
    public void teardown() {

    }

    @Test
    @Transactional// open tx context for the whole test methods
    public void addStudentToCourse() {

        List<Student> students = this.studentRepository.findAll();

        assertThat(students.size()).isEqualTo(1);
        var s = students.get(0);

        List<Course> courses = this.courseRepository.findAll();

        assertThat(courses.size()).isEqualTo(1);
        var c = courses.get(0);

        c.addStudent(s);

        assertThat(this.courseRepository.getById(c.getId()).getStudents().size()).isEqualTo(1);
        assertThat(this.studentRepository.getById(s.getId()).getCourses().size()).isEqualTo(1);


        c.removeStudent(s);

        assertThat(this.courseRepository.getById(c.getId()).getStudents().size()).isEqualTo(0);
        assertThat(this.studentRepository.getById(s.getId()).getCourses().size()).isEqualTo(0);
    }

}
