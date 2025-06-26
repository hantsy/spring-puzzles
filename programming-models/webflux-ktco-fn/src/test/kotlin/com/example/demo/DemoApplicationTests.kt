package com.example.demo


import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import reactor.kotlin.test.test

@Import(TestcontainersConfiguration::class)
@SpringBootTest
class DemoApplicationTests {

    private lateinit var client: WebClient

    private var port: Int = 8080

    @BeforeEach
    fun setup() {
        client = WebClient.create("http://localhost:$port")
    }

    @Test
    fun `get all posts`() {
        client.get()
                .uri("/")
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
