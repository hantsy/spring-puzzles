package com.example.demo.infra;

import com.example.demo.domain.Student;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {

    @EntityGraph("studentWithCourses")
    Student getById(Long id);

    Student readById(Long studentId);
}
