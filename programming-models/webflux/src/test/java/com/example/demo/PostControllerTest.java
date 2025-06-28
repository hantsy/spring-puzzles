package com.example.demo;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@WebFluxTest(controllers = PostController.class)
public class PostControllerTest {

    @Autowired
    WebTestClient client;

    @MockitoBean
    PostRepository posts;

    @Test
    public void getAll() throws Exception {
        when(posts.findAll()).thenReturn(
                Flux.just(
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
        when(posts.findById(idCaptor.capture())).thenReturn(Mono.just(post));

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
        when(posts.findById(anyLong())).thenReturn(Mono.empty());

        client.get().uri("/posts/{id}", id).accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();

        verify(posts, times(1)).findById(anyLong());
        verifyNoMoreInteractions(posts);
    }

    @Test
    public void createPost() throws Exception {
        var id = 1L;
        var post = new Post(id, "test one", "content one", LocalDateTime.now());
        when(posts.save(any(Post.class))).thenReturn(Mono.just(post));

        var data = new Post(null, "title one", "content one", null);
        client.post().uri("/posts").contentType(MediaType.APPLICATION_JSON).bodyValue(data)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().location("/posts/" + id);

        verify(posts, times(1)).save(any(Post.class));
        verifyNoMoreInteractions(posts);
    }

    @Test
    public void updatePost() throws Exception {
        var id = 1L;
        var post = new Post(id, "test one", "content one", LocalDateTime.now());
        when(posts.findById(anyLong())).thenReturn(Mono.just(post));

        var updated = new Post(id, "updated test one", " updated content one", LocalDateTime.now());
        var postCaptor = ArgumentCaptor.forClass(Post.class);
        when(posts.save(postCaptor.capture())).thenReturn(Mono.just(updated));

        var data = new Post(null, "updated test one", " updated content one", LocalDateTime.now());
        client.put().uri("/posts/{id}", id).contentType(MediaType.APPLICATION_JSON).bodyValue(data)
                .exchange()
                .expectStatus().isNoContent();

        assertThat(postCaptor.getValue().id()).isEqualTo(id);

        verify(posts, times(1)).findById(anyLong());
        verify(posts, times(1)).save(any(Post.class));
        verifyNoMoreInteractions(posts);
    }

    @Test
    public void updatePost_nonExisting() throws Exception {
        var id = 1L;
        when(posts.findById(anyLong())).thenReturn(Mono.empty());

        var updated = new Post(id, "updated test one", " updated content one", LocalDateTime.now());
        when(posts.save(any(Post.class))).thenReturn(Mono.just(updated));

        var data = new Post(null, "updated test one", " updated content one", LocalDateTime.now());
        client.put().uri("/posts/{id}", id).contentType(MediaType.APPLICATION_JSON).bodyValue(data)
                .exchange()
                .expectStatus().isNotFound();

        verify(posts, times(1)).findById(anyLong());
        verify(posts, times(0)).save(any(Post.class));
        verifyNoMoreInteractions(posts);
    }

    @Test
    public void deleteById() throws Exception {
        var id = 1L;
        var existedIdCaptor = ArgumentCaptor.forClass(Long.class);
        when(posts.existsById(existedIdCaptor.capture())).thenReturn(Mono.just(true));

        var deletedIdCaptor = ArgumentCaptor.forClass(Long.class);
        when(posts.deleteById(deletedIdCaptor.capture())).thenReturn(Mono.empty());

        client.delete().uri("/posts/{id}", id)
                .exchange()
                .expectStatus().isNoContent();

        assertThat(existedIdCaptor.getValue()).isEqualTo(id);
        assertThat(deletedIdCaptor.getValue()).isEqualTo(id);

        verify(posts, times(1)).existsById(anyLong());
        verify(posts, times(1)).deleteById(anyLong());
        verifyNoMoreInteractions(posts);
    }

    @Test
    public void deleteById_nonExisting() throws Exception {
        var id = 1L;
        when(posts.existsById(anyLong())).thenReturn(Mono.just(false));
        when(posts.deleteById(anyLong())).thenReturn(Mono.empty());

        client.delete().uri("/posts/{id}", id)
                .exchange()
                .expectStatus().isNotFound();

        verify(posts, times(1)).existsById(anyLong());
        verify(posts, times(0)).deleteById(anyLong());
        verifyNoMoreInteractions(posts);
    }
}
