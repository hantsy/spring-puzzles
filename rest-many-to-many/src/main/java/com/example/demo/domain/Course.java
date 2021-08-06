package com.example.demo.domain;

import com.example.demo.application.exception.CoursesOfStudentReachedMaxLimitationException;
import com.example.demo.application.exception.StudentAlreadyRegisteredCourseException;
import com.example.demo.application.exception.StudentNeverRegisteredCourseException;
import com.example.demo.application.exception.StudentsOfCourseReachedMaxLimitationException;
import com.sun.istack.NotNull;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

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
public class Course implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Column(name = "title", unique = true)
    private String title;

    private String description;

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, mappedBy = "courses")
    @Builder.Default
    @Size(max = 50)
    Set<Student> students = new HashSet<>();

    //requires open sessions to sync the changes.
    public synchronized void addStudent(Student student) {
        if (!this.getStudents().contains(student)) {
            if (this.getStudents().size() >= 50) {
                throw new StudentsOfCourseReachedMaxLimitationException(this.id);
            }
            if (student.getCourses().size() >= 5) {
                throw new CoursesOfStudentReachedMaxLimitationException(student.getId());
            }
            student.getCourses().add(this);
            this.students.add(student);

        } else throw new StudentAlreadyRegisteredCourseException(student.getId(), this.id);

    }

    //requires open sessions to sync the changes.
    public synchronized void removeStudent(Student student) {
        if (this.getStudents().contains(student)) {
            student.getCourses().remove(this);
            this.students.remove(student);
        } else throw new StudentNeverRegisteredCourseException(student.getId(), this.id);

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
