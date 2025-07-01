package com.example.demo.interfaces;

import com.example.demo.domain.Course_;
import com.example.demo.domain.Course;
import com.example.demo.infra.CourseRepository;
import com.example.demo.interfaces.dto.CourseWithStudentDto;
import com.example.demo.interfaces.dto.StudentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Comparator;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportController {
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
                                s.getStudents().size(),
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
