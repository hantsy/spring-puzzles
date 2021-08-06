package com.example.demo.interfaces;

import com.example.demo.application.exception.CourseNotFoundException;
import com.example.demo.application.exception.StudentNotFoundException;
import com.example.demo.domain.Course;
import com.example.demo.domain.Student;
import com.example.demo.infra.CourseRepository;
import com.example.demo.infra.StudentRepository;
import com.example.demo.interfaces.dto.CourseDto;
import com.example.demo.interfaces.dto.NewCourseCommand;
import com.example.demo.interfaces.dto.StudentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.util.Comparator;
import java.util.Optional;

import static org.springframework.http.ResponseEntity.*;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
@Validated
public class CourseController {
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
    private final TransactionTemplate txTemplate;

    @GetMapping()
    public ResponseEntity allCourses() {
        var courses = this.courseRepository.findAll(Sort.by(Sort.Direction.ASC, "title"))
                .stream()
                .map(s -> new CourseDto(s.getId(), s.getTitle(), s.getDescription()))
                .toList();
        return ok().body(courses);
    }

    @PostMapping()
    public ResponseEntity addCourse(@RequestBody @Valid NewCourseCommand data, ServletUriComponentsBuilder uriComponentsBuilder) {
        var course = Course.builder()
                .title(data.name())
                .description(data.description())
                .build();
        var saved = this.courseRepository.save(course);
        var uri = uriComponentsBuilder.path("/courses/{id}").build(saved.getId());
        return created(uri).build();
    }

    @GetMapping("{id}")
    public ResponseEntity courseById(@PathVariable Long id) {
        return this.courseRepository.findById(id)
                .map(s -> ok().body(new CourseDto(s.getId(), s.getTitle(), s.getDescription())))
                .orElseThrow(() -> new CourseNotFoundException(id));
    }

    @DeleteMapping("{id}")
    public ResponseEntity deleteById(@PathVariable Long id) {
        txTemplate.executeWithoutResult(tx -> {
            var course = this.courseRepository.findById(id)
                    .orElseThrow(() -> new CourseNotFoundException(id));
            course.getStudents()
                    .forEach(
                            s -> s.getCourses().remove(course)
                    );
            this.courseRepository.delete(course);
        });
        return noContent().build();
    }

    @GetMapping("{id}/students")
    public ResponseEntity studentsByCourseId(@PathVariable Long id) {
        Course course = this.courseRepository.getById(id);
        return Optional.ofNullable(course)
                .map(
                        c -> {
                            var students = c.getStudents()
                                    .stream()
                                    .map(s -> new StudentDto(s.getId(), s.getName()))
                                    .sorted(Comparator.comparing(StudentDto::name))
                                    .toList();
                            return ok().body(students);
                        }
                )
                .orElseThrow(() -> new CourseNotFoundException(id));
    }

    @PostMapping("{id}/students/{studentId}")
    public ResponseEntity addStudentToCourse(@PathVariable Long id, @PathVariable Long studentId) {
        txTemplate.executeWithoutResult(tx -> {
            Course course = this.courseRepository.findById(id).orElseThrow(() -> new CourseNotFoundException(id));
            Student student = this.studentRepository.findById(studentId).orElseThrow(() -> new StudentNotFoundException(id));
            course.addStudent(student);
        });

        return noContent().build();
    }

    @DeleteMapping("{id}/students/{studentId}")
    public ResponseEntity removeStudentFromCourse(@PathVariable Long id, @PathVariable Long studentId) {
        txTemplate.executeWithoutResult(tx -> {
            Course course = this.courseRepository.findById(id).orElseThrow(() -> new CourseNotFoundException(id));
            Student student = this.studentRepository.findById(studentId).orElseThrow(() -> new StudentNotFoundException(id));
            course.removeStudent(student);
        });
        return noContent().build();
    }
}
