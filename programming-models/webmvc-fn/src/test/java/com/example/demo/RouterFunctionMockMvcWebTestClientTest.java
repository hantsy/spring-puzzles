package com.example.demo;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@WebMvcTest
public class RouterFunctionMockMvcWebTestClientTest {

    @TestConfiguration
    @Import({WebConfig.class, PostHandler.class})
    static class TestConfig {

    }

    @Autowired
    RouterFunction<ServerResponse> routerFunction;

    @MockitoBean
    PostRepository posts;

    WebTestClient client;

    @BeforeEach
    void setup() {
        MockMvc mockMvc = MockMvcBuilders.routerFunctions(routerFunction).build();
        client = MockMvcWebTestClient.bindTo(mockMvc)
                .codecs(clientCodecConfigurer -> clientCodecConfigurer
                        .defaultCodecs()
                        .enableLoggingRequestDetails(true)
                )
                .build();
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

        client.get().uri("/posts").accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().jsonPath("$.size()").isEqualTo(2);

        verify(posts, times(1)).findAll();
        verifyNoMoreInteractions(posts);
    }

    @Test
    public void getPostById() throws Exception {
        var id = 1L;
        var post = new Post(id, "test one", "content one", LocalDateTime.now());
        var idCaptor = ArgumentCaptor.forClass(Long.class);
        when(posts.findById(idCaptor.capture())).thenReturn(Optional.of(post));

        client.get().uri("/posts/{id}", id).accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().jsonPath("$.id").isEqualTo(id);

        assertThat(idCaptor.getValue()).isEqualTo(id);

        verify(posts, times(1)).findById(anyLong());
        verifyNoMoreInteractions(posts);
    }

    @Test
    public void getPostById_nonExisting() throws Exception {
        var id = 1L;
        when(posts.findById(anyLong())).thenReturn(Optional.empty());

        client.get().uri("/posts/{id}", id).accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();

        verify(posts, times(1)).findById(anyLong());
        verifyNoMoreInteractions(posts);
    }

    @Test
    public void createPost() throws Exception {
        var id = 1L;
        when(posts.create(any(Post.class))).thenReturn(id);
        var data = new Post(null, "title one", "content one", null);

        client.post().uri("/posts").contentType(MediaType.APPLICATION_JSON).bodyValue(data)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().location("/posts/" + id);

        verify(posts, times(1)).create(any(Post.class));
        verifyNoMoreInteractions(posts);
    }

    @Test
    public void updatePost() throws Exception {
        var id = 1L;
        var idCaptor = ArgumentCaptor.forClass(Long.class);
        when(posts.update(idCaptor.capture(), any(Post.class))).thenReturn(1);

        var data = new Post(null, "updated test one", " updated content one", LocalDateTime.now());

        client.put().uri("/posts/{id}", id).contentType(MediaType.APPLICATION_JSON).bodyValue(data)
                .exchange()
                .expectStatus().isNoContent();

        assertThat(idCaptor.getValue()).isEqualTo(id);

        verify(posts, times(1)).update(anyLong(), any(Post.class));
        verifyNoMoreInteractions(posts);
    }

    @Test
    public void updatePost_nonExisting() throws Exception {
        var id = 1L;
        when(posts.update(anyLong(), any(Post.class))).thenReturn(0);

        var data = new Post(null, "updated test one", " updated content one", LocalDateTime.now());
        client.put().uri("/posts/{id}", id).contentType(MediaType.APPLICATION_JSON).bodyValue(data)
                .exchange()
                .expectStatus().isNotFound();

        verify(posts, times(1)).update(anyLong(), any(Post.class));
        verifyNoMoreInteractions(posts);
    }

    @Test
    public void deleteById() throws Exception {
        var id = 1L;

        var deletedIdCaptor = ArgumentCaptor.forClass(Long.class);
        when(posts.deleteById(deletedIdCaptor.capture())).thenReturn(1);

        client.delete().uri("/posts/{id}", id)
                .exchange()
                .expectStatus().isNoContent();

        assertThat(deletedIdCaptor.getValue()).isEqualTo(id);

        verify(posts, times(1)).deleteById(anyLong());
        verifyNoMoreInteractions(posts);
    }

    @Test
    public void deleteById_nonExisting() throws Exception {
        var id = 1L;
        when(posts.deleteById(anyLong())).thenReturn(0);

        client.delete().uri("/posts/{id}", id)
                .exchange()
                .expectStatus().isNotFound();

        verify(posts, times(1)).deleteById(anyLong());
        verifyNoMoreInteractions(posts);
    }


}
