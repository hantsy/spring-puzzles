package com.example.demo


import io.kotest.matchers.types.shouldBeInstanceOf
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.toEntity
import reactor.kotlin.test.test
import reactor.netty.http.client.HttpClient
import kotlin.random.Random

@Import(TestcontainersConfiguration::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DemoApplicationTests {

    private lateinit var client: WebClient

    @LocalServerPort
    private var port: Int = 8080

    @BeforeEach
    fun setup() {
        val reactorHttpClient = HttpClient.create().wiretap(true)
        val reactorConnector = ReactorClientHttpConnector(reactorHttpClient)
        client = WebClient.builder()
            .baseUrl("http://localhost:$port")
            .codecs { it.defaultCodecs().enableLoggingRequestDetails(true) }
            .clientConnector(reactorConnector)
            .build()
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

    @Test
    fun `get post by id that not existed`() {
        client.get()
            .uri { uri -> uri.path("/posts/{id}").build(Random(20).nextLong(10_000)) }
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .toEntity<Post>()
            .test()
            .consumeErrorWith { it.shouldBeInstanceOf<WebClientResponseException>() }
            .verify()
    }

    @Test
    fun `delete post by id that not existed`() {
        client.delete()
            .uri { uri -> uri.path("/posts/{id}").build(Random(20).nextLong(10_000)) }
            .retrieve()
            .toEntity<Void>()
            .test()
            .consumeErrorWith { it.shouldBeInstanceOf<WebClientResponseException>()  }
            .verify()
    }
}
