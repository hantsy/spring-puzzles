package com.example.demo.domain;

import com.sun.istack.NotNull;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

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
public class Student implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Column(name = "name", unique = true)
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
