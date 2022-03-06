package com.example.demo

import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.LocalDateTime

@WebFluxTest(controllers = [PostController::class])
class PostControllerTest {

    @MockBean
    private lateinit var postRepository: PostRepository

    @Autowired
    private lateinit var client: WebTestClient

    @Test
    fun `get posts by title with invalid title`() {
        given(this.postRepository.findByTitleContains(anyString()))
            .willReturn(
                flowOf(
                    Post(
                        id = 1,
                        title = "test title",
                        content = "test content",
                        createdAt = LocalDateTime.now()
                    )
                )
            )

        this.client.get().uri("/byTitle?title=")
            .exchange()
            .expectStatus().isBadRequest

        verify(this.postRepository, times(0)).findByTitleContains(anyString());
    }
}