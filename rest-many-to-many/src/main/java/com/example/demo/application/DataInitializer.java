package com.example.demo.application;


import com.example.demo.domain.Course;
import com.example.demo.domain.Student;
import com.example.demo.infra.CourseRepository;
import com.example.demo.infra.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.stream.Stream;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
class DataInitializer implements ApplicationRunner {

    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        Stream.of("Jack", "Tom")
                .map(s -> this.studentRepository.save(Student.builder().name(s).build()))
                .forEach(s -> log.debug("saved students: {}", s));

        Stream.of("Java course", "Jakarta EE course", "Spring course", "Spring Cloud course", "Spring Boot course")
                .map(s -> this.courseRepository.save(Course.builder().title(s).description("description of " + s).build()))
                .forEach(s -> log.debug("saved course: {}", s));
        var students = this.studentRepository.findAll();
        this.courseRepository.findAll().forEach(
                c -> c.addStudent(students.get(0))
        );
    }
}
