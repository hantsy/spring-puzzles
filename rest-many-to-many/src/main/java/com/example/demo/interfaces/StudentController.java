package com.example.demo.interfaces;

import com.example.demo.application.exception.StudentNotFoundException;
import com.example.demo.domain.Student;
import com.example.demo.infra.StudentRepository;
import com.example.demo.interfaces.dto.CourseDto;
import com.example.demo.interfaces.dto.NewStudentCommand;
import com.example.demo.interfaces.dto.StudentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.util.Optional;

import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
@Validated
public class StudentController {
    private final StudentRepository studentRepository;

    @GetMapping()
    public ResponseEntity allStudents() {
        var students = this.studentRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))
                .stream()
                .map(s -> new StudentDto(s.getId(), s.getName()))
                .toList();
        return ok().body(students);
    }

    @PostMapping()
    public ResponseEntity addStudent(@RequestBody @Valid NewStudentCommand data, ServletUriComponentsBuilder uriComponentsBuilder) {
        var saved = this.studentRepository.save(Student.builder().name(data.name()).build());
        var uri = uriComponentsBuilder.path("/students/{id}").build(saved.getId());
        return created(uri).build();
    }

    @GetMapping("{id}")
    public ResponseEntity studentById(@PathVariable Long id) {
        return this.studentRepository.findById(id)
                .map(s -> ok().body(new StudentDto(s.getId(), s.getName())))
                .orElseThrow(() -> new StudentNotFoundException(id));
    }

    @GetMapping("{id}/courses")
    public ResponseEntity coursesByStudentId(@PathVariable Long id) {
        Student student = this.studentRepository.getById(id);
        return Optional.ofNullable(student)
                .map(
                        s -> {
                            var courses = s.getCourses()
                                    .stream()
                                    .map(c -> new CourseDto(c.getId(), c.getTitle(), c.getDescription()))
                                    .toList();
                            return ok().body(courses);
                        }
                )
                .orElseThrow(() -> new StudentNotFoundException(id));
    }
}
