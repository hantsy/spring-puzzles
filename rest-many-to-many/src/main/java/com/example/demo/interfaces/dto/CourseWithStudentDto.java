package com.example.demo.interfaces.dto;

import java.util.List;

public record CourseWithStudentDto(Long id, String title, String description, int countOfStudents,
                                   List<StudentDto> students) {
}
