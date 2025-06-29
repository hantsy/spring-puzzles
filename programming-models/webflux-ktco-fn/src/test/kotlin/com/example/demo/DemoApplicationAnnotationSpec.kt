package com.example.demo

import io.kotest.core.spec.style.AnnotationSpec
import org.assertj.core.api.Assertions.assertThat
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import reactor.kotlin.test.test

@Import(TestcontainersConfiguration::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DemoApplicationAnnotationSpec : AnnotationSpec() {
    @LocalServerPort
    private var port: Int = 8080
    lateinit var client: WebClient

    @BeforeEach
    fun setUp() {
        client = WebClient.create("http://localhost:$port")
    }

    @Test
    fun `get all posts`() {
        client.get()
            .uri("/posts")
            .accept(MediaType.APPLICATION_JSON)
            .exchangeToFlux {
                assertThat(it.statusCode()).isEqualTo(HttpStatus.OK)
                it.bodyToFlux(Post::class.java)
            }
            .test()
            .expectNextCount(2)
            .verifyComplete()
    }
}

