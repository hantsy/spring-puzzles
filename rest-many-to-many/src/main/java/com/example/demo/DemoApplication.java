package com.example.demo;

import com.sun.istack.NotNull;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.persistence.*;
import javax.persistence.criteria.Predicate;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Stream;

import static org.springframework.http.ResponseEntity.*;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}

@Component
//@Profile("dev")
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

@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
class ReportController {
    private final CourseRepository courseRepository;

    @GetMapping()
    public ResponseEntity allCourses(@RequestParam(value = "q", required = false) String keyword) {

        Specification<Course> spec = (root, query, cb) -> {
            var predicates = new ArrayList<Predicate>();
            if (StringUtils.hasText(keyword)) {
                predicates.add(
                        cb.or(
                                cb.like(root.get(Course_.title), "%" + keyword + "%"),
                                cb.like(root.get(Course_.description), "%" + keyword + "%")
                        )
                );
            }

            root.fetch(Course_.students);

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        var courses = this.courseRepository.findAll(spec, Sort.by(Sort.Direction.ASC, "title"))
                .stream()
                .map(s -> new CourseWithStudentDto(
                                s.getId(),
                                s.getTitle(),
                                s.getDescription(),
                                s.getStudents()
                                        .stream()
                                        .map(t -> new StudentDto(t.getId(), t.getName()))
                                        .sorted(Comparator.comparing(StudentDto::name))
                                        .toList()
                        )
                )
                .toList();
        return ok().body(courses);
    }
}

@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
@Validated
class StudentController {
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
                .orElse(notFound().build());
    }

    @GetMapping("{id}/courses")
    public ResponseEntity coursesByStudentId(@PathVariable Long id) {
        var courses = this.studentRepository.getById(id).getCourses()
                .stream()
                .map(s -> new CourseDto(s.getId(), s.getTitle(), s.getDescription()))
                .toList();
        return ok().body(courses);
    }
}


@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
@Validated
class CourseController {
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
        var saved = this.courseRepository.save(Course.builder().title(data.name()).description(data.description()).build());
        var uri = uriComponentsBuilder.path("/courses/{id}").build(saved.getId());
        return created(uri).build();
    }

    @GetMapping("{id}")
    public ResponseEntity courseById(@PathVariable Long id) {
        return this.courseRepository.findById(id)
                .map(s -> ok().body(new CourseDto(s.getId(), s.getTitle(), s.getDescription())))
                .orElse(notFound().build());
    }

    @DeleteMapping("{id}")
    public ResponseEntity deleteById(@PathVariable Long id) {
        txTemplate.executeWithoutResult(tx -> {
            var course = this.courseRepository.getById(id);
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
        var courses = this.courseRepository.getById(id).getStudents()
                .stream()
                .map(s -> new StudentDto(s.getId(), s.getName()))
                .sorted(Comparator.comparing(StudentDto::name))
                .toList();
        return ok().body(courses);
    }

    @PostMapping("{id}/students/{studentId}")
    public ResponseEntity addStudentToCourse(@PathVariable Long id, @PathVariable Long studentId) {
        txTemplate.executeWithoutResult(tx -> {
            Course course = this.courseRepository.readById(id);
            Student student = this.studentRepository.readById(studentId);
            course.addStudent(student);
        });

        return noContent().build();
    }

    @DeleteMapping("{id}/students/{studentId}")
    public ResponseEntity removeStudentFromCourse(@PathVariable Long id, @PathVariable Long studentId) {
        txTemplate.executeWithoutResult(tx -> {
            Course course = this.courseRepository.readById(id);
            Student student = this.studentRepository.readById(studentId);
            course.removeStudent(student);
        });
        return noContent().build();
    }
}

record NewStudentCommand(@NotEmpty String name) {
}

record NewCourseCommand(@NotEmpty String name, String description) {
}


record StudentDto(Long id, String name) {
}

record CourseDto(Long id, String title, String description) {
}

record CourseWithStudentDto(Long id, String title, String description, List<StudentDto> students) {
}

interface StudentRepository extends JpaRepository<Student, Long> {

    @EntityGraph("studentWithCourses")
    Student getById(Long id);

    Student readById(Long studentId);
}

interface CourseRepository extends JpaRepository<Course, Long>, JpaSpecificationExecutor<Course> {
    @EntityGraph("courseWithStudents")
    Course getById(Long id);

    Course readById(Long id);
}

@Entity
@Table(name = "students")
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@NamedEntityGraph(
        name = "studentWithCourses",
        attributeNodes = {
                @NamedAttributeNode("name"),
                @NamedAttributeNode(value = "courses", subgraph = "courses")
        },
        subgraphs = {
                @NamedSubgraph(
                        name = "courses",
                        attributeNodes = {
                                @NamedAttributeNode("title")}
                )
        }
)
class Student implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    private String name;

    @ManyToMany(targetEntity = Course.class)
    @JoinTable(
            name = "course_arrangements",
            joinColumns = @JoinColumn(name = "fk_student_id"),
            inverseJoinColumns = @JoinColumn(name = "fk_course_id"))
    @Builder.Default
    @Size(max = 5)
    Set<Course> courses = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Student)) {
            return false;
        }
        Student student = (Student) o;
        return name.equals(student.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", courses=" + courses.stream().map(Course::getTitle).toList() +
                '}';
    }
}

@Entity
@Table(name = "courses")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@NamedEntityGraph(
        name = "courseWithStudents",
        attributeNodes = {
                @NamedAttributeNode("title"),
                @NamedAttributeNode("description"),
                @NamedAttributeNode(value = "students", subgraph = "students")
        },
        subgraphs = {
                @NamedSubgraph(
                        name = "students",
                        attributeNodes = {
                                @NamedAttributeNode("name")}
                )
        }
)
@Slf4j
class Course implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    private String title;

    private String description;

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, mappedBy = "courses")
    @Builder.Default
    @Size(max = 50)
    Set<Student> students = new HashSet<>();

    public synchronized void addStudent(Student student) {
        student.getCourses().add(this);
        this.students.add(student);
    }

    public synchronized void removeStudent(Student student) {
        student.getCourses().remove(this);
        this.students.remove(student);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Course)) {
            return false;
        }
        Course course = (Course) o;
        return title.equals(course.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title);
    }

    @Override
    public String toString() {
        return "Course{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", students=" + students.stream().map(Student::getName).toList() +
                '}';
    }
}