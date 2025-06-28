package com.example.demo;

import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@WebMvcTest
public class RouterFunctionRestAssuredMockMvcTest {

    @TestConfiguration
    @Import({WebConfig.class, PostHandler.class})
    static class TestConfig {

    }

    @Autowired
    RouterFunction<ServerResponse> routerFunction;

    @MockitoBean
    PostRepository posts;

    @BeforeEach
    void setup() {
        MockMvc mockMvc = MockMvcBuilders.routerFunctions(routerFunction).build();
        RestAssuredMockMvc.mockMvc(mockMvc);
    }

    @AfterEach
    void teardown() {
        RestAssuredMockMvc.reset();
    }

    @Test
    public void getAll() throws Exception {
        when(posts.findAll()).thenReturn(
                Stream.of(
                        new Post(1L, "test one", "content one", LocalDateTime.now()),
                        new Post(2L, "test two", "content two", LocalDateTime.now())
                )
        );

        //@formatter:off
        given()
            .accept(MediaType.APPLICATION_JSON)
        .when()
            .get("/posts")
        .then()
            .status(HttpStatus.OK)
            .contentType(ContentType.JSON)
            .body("[0].title", CoreMatchers.equalTo("test one"));
        //@formatter:on

        verify(posts, times(1)).findAll();
        verifyNoMoreInteractions(posts);
    }

    @Test
    public void getPostById() throws Exception {
        var id = 1L;
        var post = new Post(id, "test one", "content one", LocalDateTime.now());
        var idCaptor = ArgumentCaptor.forClass(Long.class);
        when(posts.findById(idCaptor.capture())).thenReturn(Optional.of(post));

        //@formatter:off
        given()
            .accept(MediaType.APPLICATION_JSON)
        .when()
            .get("/posts/{id}", id)
        .then()
            .status(HttpStatus.OK)
            .contentType(ContentType.JSON)
            .body("title", CoreMatchers.equalTo("test one"));
        //@formatter:on

        assertThat(idCaptor.getValue()).isEqualTo(id);

        verify(posts, times(1)).findById(anyLong());
        verifyNoMoreInteractions(posts);
    }

    @Test
    public void getPostById_nonExisting() throws Exception {
        var id = 1L;
        when(posts.findById(anyLong())).thenReturn(Optional.empty());

        //@formatter:off
        given()
            .accept(MediaType.APPLICATION_JSON)
        .when()
            .get("/posts/{id}", id)
        .then()
            .status(HttpStatus.NOT_FOUND);
        //@formatter:on

        verify(posts, times(1)).findById(anyLong());
        verifyNoMoreInteractions(posts);
    }

    @Test
    public void createPost() throws Exception {
        var id = 1L;
        when(posts.create(any(Post.class))).thenReturn(id);
        var data = new Post(null, "title one", "content one", null);

        //@formatter:off
        given()
            .contentType(MediaType.APPLICATION_JSON)
            .body(data)
        .when()
            .post("/posts")
        .then()
            .status(HttpStatus.CREATED)
            .header("Location", CoreMatchers.containsString("/posts/"+id));
        //@formatter:on

        verify(posts, times(1)).create(any(Post.class));
        verifyNoMoreInteractions(posts);
    }

    @Test
    public void updatePost() throws Exception {
        var id = 1L;
        var idCaptor = ArgumentCaptor.forClass(Long.class);
        when(posts.update(idCaptor.capture(), any(Post.class))).thenReturn(1);

        var data = new Post(null, "updated test one", " updated content one", LocalDateTime.now());

        //@formatter:off
        given()
            .contentType(MediaType.APPLICATION_JSON)
            .body(data)
        .when()
            .put("/posts/{id}", id)
        .then()
            .status(HttpStatus.NO_CONTENT);
        //@formatter:on

        assertThat(idCaptor.getValue()).isEqualTo(id);

        verify(posts, times(1)).update(anyLong(), any(Post.class));
        verifyNoMoreInteractions(posts);
    }

    @Test
    public void updatePost_nonExisting() throws Exception {
        var id = 1L;
        when(posts.update(anyLong(), any(Post.class))).thenReturn(0);

        var data = new Post(null, "updated test one", " updated content one", LocalDateTime.now());
        //@formatter:off
        given()
            .contentType(MediaType.APPLICATION_JSON)
            .body(data)
        .when()
            .put("/posts/{id}", id)
        .then()
            .status(HttpStatus.NOT_FOUND);
        //@formatter:on

        verify(posts, times(1)).update(anyLong(), any(Post.class));
        verifyNoMoreInteractions(posts);
    }

    @Test
    public void deleteById() throws Exception {
        var id = 1L;

        var deletedIdCaptor = ArgumentCaptor.forClass(Long.class);
        when(posts.deleteById(deletedIdCaptor.capture())).thenReturn(1);

        //@formatter:off
        given()
        .when()
            .delete("/posts/{id}", id)
        .then()
            .status(HttpStatus.NO_CONTENT);
        //@formatter:on

        assertThat(deletedIdCaptor.getValue()).isEqualTo(id);

        verify(posts, times(1)).deleteById(anyLong());
        verifyNoMoreInteractions(posts);
    }

    @Test
    public void deleteById_nonExisting() throws Exception {
        var id = 1L;
        when(posts.deleteById(anyLong())).thenReturn(0);

        //@formatter:off
        given()
        .when()
            .delete("/posts/{id}", id)
        .then()
            .status(HttpStatus.NOT_FOUND);
        //@formatter:on

        verify(posts, times(1)).deleteById(anyLong());
        verifyNoMoreInteractions(posts);
    }


}
