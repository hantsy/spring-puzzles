package com.example.demo;

import com.example.demo.domain.Course;
import com.example.demo.infra.CourseRepository;
import com.example.demo.infra.StudentRepository;
import com.example.demo.interfaces.CourseController;
import com.example.demo.interfaces.RestExceptionHandler;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// manually setup MockMvc.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class CourseControllerWithManualMockMvcTest {

    @MockBean
    CourseRepository courseRepository;

    @MockBean
    StudentRepository studentRepository;

    @MockBean
    TransactionTemplate txTemplate;

    @Autowired
    CourseController courseController;

    @Autowired
    RestExceptionHandler controllerAdvice;

    MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(courseController)
                .setControllerAdvice(controllerAdvice)
                .build();
    }

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

}
