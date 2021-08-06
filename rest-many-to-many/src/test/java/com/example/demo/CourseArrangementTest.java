package com.example.demo;

import com.example.demo.application.CourseArrangement;
import com.example.demo.domain.Course;
import com.example.demo.domain.Student;
import com.example.demo.infra.CourseRepository;
import com.example.demo.infra.StudentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class CourseArrangementTest {

    @MockBean
    CourseRepository courseRepository;
    @MockBean
    StudentRepository studentRepository;
    @MockBean
    TransactionTemplate txTemplate;
    @Autowired
    CourseArrangement courseArrangement;

    @Test
    void addStudentToCourse() {
        var dummyStudent = Student.builder()
                .id(2L)
                .name("Tom")
                .build();
        var dummyCourse = Course.builder()
                .id(1L)
                .title("Spring")
                .description("desc of Spring course")
                .build();

        when(this.courseRepository.findById(1L)).thenReturn(Optional.of(dummyCourse));
        when(this.studentRepository.findById(2L)).thenReturn(Optional.of(dummyStudent));

        var mockedTx = mock(TransactionStatus.class);
        doAnswer(answer -> {
                    var consumer = (Consumer<TransactionStatus>) answer.getArgument(0);
                    consumer.accept(mockedTx);
                    return mockedTx;
                }
        ).when(this.txTemplate).executeWithoutResult(any(Consumer.class));

        courseArrangement.addStudentToCourse(2L, 1L);

        verify(this.courseRepository, times(1)).findById(anyLong());
        verify(this.studentRepository, times(1)).findById(anyLong());
        verify(this.txTemplate, times(1)).executeWithoutResult(any(Consumer.class));
    }

    @Test
    void removeStudentFromCourse() {
        var dummyStudent = Student.builder()
                .id(2L)
                .name("Tom")
                .build();
        var dummyCourse = Course.builder()
                .id(1L)
                .title("Spring")
                .description("desc of Spring course")
                .build();

        dummyCourse.addStudent(dummyStudent);

        when(this.courseRepository.findById(1L)).thenReturn(Optional.of(dummyCourse));
        when(this.studentRepository.findById(2L)).thenReturn(Optional.of(dummyStudent));

        var mockedTx = mock(TransactionStatus.class);
        doAnswer(answer -> {
                    var consumer = (Consumer<TransactionStatus>) answer.getArgument(0);
                    consumer.accept(mockedTx);
                    return mockedTx;
                }
        ).when(this.txTemplate).executeWithoutResult(any(Consumer.class));

        courseArrangement.removeStudentFromCourse(2L, 1L);

        verify(this.courseRepository, times(1)).findById(anyLong());
        verify(this.studentRepository, times(1)).findById(anyLong());
        verify(this.txTemplate, times(1)).executeWithoutResult(any(Consumer.class));
    }
}