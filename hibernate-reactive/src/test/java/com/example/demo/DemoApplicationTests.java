package com.example.demo;

import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@SpringBootTest
class DemoApplicationTests {
    private WebTestClient client;

    @MockBean
    PostRepository posts;

    @Autowired
    RouterFunction routerFunction;

    @BeforeEach
    public void setup() {
        this.client = WebTestClient.bindToRouterFunction(routerFunction)
            .build();
    }

    @Test
    public void getAllPosts() {
        when(posts.findAll()).thenReturn(
            Uni.createFrom().item(
                List.of(
                    Post.builder().id(UUID.randomUUID()).title("my title").content("my content").build(),
                    Post.builder().id(UUID.randomUUID()).title("my title2").content("my content2").build()
                )
            )
        );

        this.client.get().uri("/posts").accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().is2xxSuccessful()
            .expectBody()
            .jsonPath("$.size()").isEqualTo(2)
            .jsonPath("$.[0].title").isEqualTo("my title");

        verify(posts, times(1)).findAll();
        verifyNoMoreInteractions(posts);
    }

    @Test
    public void getPostById() {
        when(posts.findById(any(UUID.class))).thenReturn(
            Uni.createFrom().item(
                Post.builder().id(UUID.randomUUID()).title("my title").content("my content").build()
            )
        );

        this.client.get().uri("/posts/{id}", UUID.randomUUID())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().is2xxSuccessful()
            .expectBody()
            .jsonPath("$.title").isEqualTo("my title");

        verify(posts, times(1)).findById(any(UUID.class));
        verifyNoMoreInteractions(posts);
    }

    @Test
    public void getPostById_notFound() {
        when(posts.findById(any(UUID.class))).thenThrow(new PostNotFoundException(UUID.randomUUID()));

        this.client.get().uri("/posts/{id}", UUID.randomUUID())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().is4xxClientError();

        verify(posts, times(1)).findById(any(UUID.class));
        verifyNoMoreInteractions(posts);
    }

    @Test
    public void createPost() {
        when(posts.save(any(Post.class))).thenReturn(
            Uni.createFrom().item(
                Post.builder().id(UUID.randomUUID()).title("my title").content("my content").build()
            )
        );

        this.client.post().uri("/posts")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(CreatePostCommand.of("my title", "my content"))
            .exchange()
            .expectStatus().is2xxSuccessful()
            .expectHeader().exists("Location");


        verify(posts, times(1)).save(any(Post.class));
        verifyNoMoreInteractions(posts);
    }

    @Test
    public void updatePost() {
        when(posts.findById(any(UUID.class))).thenReturn(
            Uni.createFrom().item(
                Post.builder().id(UUID.randomUUID()).title("my title").content("my content").build()
            )
        );

        when(posts.save(any(Post.class))).thenReturn(
            Uni.createFrom().item(
                Post.builder().id(UUID.randomUUID()).title("my title").content("my content").build()
            )
        );

        this.client.put().uri("/posts/{id}", UUID.randomUUID())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(UpdatePostCommand.of("my title", "my content"))
            .exchange()
            .expectStatus().is2xxSuccessful();

        verify(posts, times(1)).findById(any(UUID.class));
        verify(posts, times(1)).save(any(Post.class));
        verifyNoMoreInteractions(posts);
    }

    @Test
    public void deletePostById() {
        when(posts.deleteById(any(UUID.class))).thenReturn(
            Uni.createFrom().item(1)
        );

        this.client.delete().uri("/posts/{id}", UUID.randomUUID())
            .exchange()
            .expectStatus().is2xxSuccessful();

        verify(posts, times(1)).deleteById(any(UUID.class));
        verifyNoMoreInteractions(posts);
    }


}
