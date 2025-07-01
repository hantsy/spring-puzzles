package com.example.demo;

import com.example.demo.application.CourseArrangement;
import com.example.demo.application.exception.CourseNotFoundException;
import com.example.demo.application.exception.CoursesOfStudentReachedMaxLimitationException;
import com.example.demo.application.exception.StudentNotFoundException;
import com.example.demo.application.exception.StudentsOfCourseReachedMaxLimitationException;
import com.example.demo.domain.Course;
import com.example.demo.domain.Student;
import com.example.demo.infra.CourseRepository;
import com.example.demo.interfaces.CourseController;
import com.example.demo.interfaces.dto.NewCourseCommand;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(controllers = CourseController.class)
public class CourseControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    private CourseRepository courseRepository;

    @MockBean
    private CourseArrangement courseArrangement;

    @MockBean
    private TransactionTemplate txTemplate;

    @SneakyThrows
    @Test
    public void testGetAllCourse() {
        var mockedData = List.of(
                Course.builder().id(1L).title("Spring").description("desc of Spring course").build(),
                Course.builder().id(2L).title("Java").description("desc of Java course").build()
        );
        when(this.courseRepository.findAll(any(Sort.class))).thenReturn(mockedData);

        //perform request
        mockMvc.perform(get("/courses").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Spring"))
                .andExpect(jsonPath("$[1].title").value("Java"));

        //verify
        verify(this.courseRepository, times(1)).findAll(any(Sort.class));
        verifyNoMoreInteractions(this.courseRepository);
    }

    @SneakyThrows
    @Test
    public void testCreateNewCourse() {
        var mockedData = Course.builder().id(1L).title("Spring").description("desc of Spring course").build();
        when(this.courseRepository.save(any(Course.class))).thenReturn(mockedData);

        //perform request
        mockMvc.perform(post("/courses")
                        .content(objectMapper.writeValueAsString(new NewCourseCommand("test course", "test description")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));

        //verify
        verify(this.courseRepository, times(1)).save(any(Course.class));
        verifyNoMoreInteractions(this.courseRepository);
    }


    @SneakyThrows
    @Test
    public void testGetCourseById() {
        var mockedData = Course.builder().id(1L).title("Spring").description("desc of Spring course").build();
        when(this.courseRepository.findById(anyLong())).thenReturn(Optional.of(mockedData));

        //perform request
        mockMvc.perform(get("/courses/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Spring"));

        //verify
        verify(this.courseRepository, times(1)).findById(anyLong());
        verifyNoMoreInteractions(this.courseRepository);
    }

    @SneakyThrows
    @Test
    public void testGetStudentsOfCourseById() {
        var mockedData = Course.builder()
                .id(1L)
                .title("Spring")
                .description("desc of Spring course")
                .students(Set.of(
                        Student.builder().id(1L).name("Jack").build(),
                        Student.builder().id(2L).name("Tom").build()
                ))
                .build();
        when(this.courseRepository.getById(anyLong())).thenReturn(mockedData);

        //perform request
        mockMvc.perform(get("/courses/1/students").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Jack"));

        //verify
        verify(this.courseRepository, times(1)).getById(anyLong());
        verifyNoMoreInteractions(this.courseRepository);
    }

    @SneakyThrows
    @Test
    public void testGetCourseByNoneExistingId_willReturn404() {
        when(this.courseRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));

        //perform request
        mockMvc.perform(get("/courses/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        //verify
        verify(this.courseRepository, times(1)).findById(anyLong());
        verifyNoMoreInteractions(this.courseRepository);
    }

    @SneakyThrows
    @Test
    public void testAddStudentToCourse() {
        doNothing().when(courseArrangement).addStudentToCourse(anyLong(), anyLong());
        //perform request
        mockMvc.perform(post("/courses/1/students/2").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        //verify
        verify(this.courseArrangement, times(1)).addStudentToCourse(anyLong(), anyLong());
        verifyNoMoreInteractions(this.courseArrangement);
    }

    @SneakyThrows
    @Test
    public void testAddStudentToCourse_WhenNoneExistingCourseId_willReturn404() {
        doThrow(new CourseNotFoundException(1L))
                .when(courseArrangement).addStudentToCourse(anyLong(), anyLong());
        //perform request
        mockMvc.perform(post("/courses/1/students/2").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        //verify
        verify(this.courseArrangement, times(1)).addStudentToCourse(anyLong(), anyLong());
        verifyNoMoreInteractions(this.courseArrangement);
    }

    @SneakyThrows
    @Test
    public void testAddStudentToCourse_WhenNoneExistingStudentId_willReturn404() {
        doThrow(StudentNotFoundException.class)
                .when(courseArrangement).addStudentToCourse(anyLong(), anyLong());
        //perform request
        mockMvc.perform(post("/courses/1/students/2").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        //verify
        verify(this.courseArrangement, times(1)).addStudentToCourse(anyLong(), anyLong());
        verifyNoMoreInteractions(this.courseArrangement);
    }

    @SneakyThrows
    @Test
    public void testAddStudentToCourse_WhenReachedCourseMaxLimit_willReturn400() {
        doThrow(CoursesOfStudentReachedMaxLimitationException.class)
                .when(courseArrangement).addStudentToCourse(anyLong(), anyLong());
        //perform request
        mockMvc.perform(post("/courses/1/students/2").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        //verify
        verify(this.courseArrangement, times(1)).addStudentToCourse(anyLong(), anyLong());
        verifyNoMoreInteractions(this.courseArrangement);
    }

    @SneakyThrows
    @Test
    public void testAddStudentToCourse_WhenReachedStudentMaxLimit_willReturn400() {
        doThrow(StudentsOfCourseReachedMaxLimitationException.class)
                .when(courseArrangement).addStudentToCourse(anyLong(), anyLong());
        //perform request
        mockMvc.perform(post("/courses/1/students/2").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        //verify
        verify(this.courseArrangement, times(1)).addStudentToCourse(anyLong(), anyLong());
        verifyNoMoreInteractions(this.courseArrangement);
    }

    @SneakyThrows
    @Test
    public void testRemoveStudentFromCourse() {
        doNothing().when(courseArrangement).removeStudentFromCourse(anyLong(), anyLong());
        //perform request
        mockMvc.perform(delete("/courses/1/students/2").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        //verify
        verify(this.courseArrangement, times(1)).removeStudentFromCourse(anyLong(), anyLong());
        verifyNoMoreInteractions(this.courseArrangement);
    }

    @SneakyThrows
    @Test
    public void testDeleteCourse() {
        var mockedStudent = Student.builder().id(2L).name("Tom").build();

        var studentSet = new HashSet<Student>();
        studentSet.add(mockedStudent);

        var mockedData = Course.builder()
                .id(1L)
                .title("Spring")
                .description("desc of Spring course")
                .students(studentSet)
                .build();

        var courseSet = new HashSet<Course>();
        courseSet.add(mockedData);
        mockedStudent.setCourses(courseSet);

        when(this.courseRepository.findById(1L)).thenReturn(Optional.of(mockedData));
        doNothing().when(this.courseRepository).delete(any(Course.class));
        var mockedTx = mock(TransactionStatus.class);
        doAnswer(invocationOnMock -> {
                    var consumer = (Consumer<TransactionStatus>) invocationOnMock.getArgument(0);
                    consumer.accept(mockedTx);
                    return null;
                }
        ).when(this.txTemplate).executeWithoutResult(any(Consumer.class));

        //perform request
        mockMvc.perform(delete("/courses/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        //verify
        verify(this.courseRepository, times(1)).findById(anyLong());
        verify(this.courseRepository, times(1)).delete(any(Course.class));
        verify(this.txTemplate, times(1)).executeWithoutResult(isA(Consumer.class));
        verifyNoMoreInteractions(this.courseRepository, this.txTemplate);
    }
}
