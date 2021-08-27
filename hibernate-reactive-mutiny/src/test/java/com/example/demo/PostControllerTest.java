package com.example.demo;

import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@WebFluxTest(controllers = PostController.class)
@Import(MutinyAdapter.class)
class PostControllerTest {

    @MockBean
    PostRepository posts;

    @Autowired
    WebTestClient client;

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

}
