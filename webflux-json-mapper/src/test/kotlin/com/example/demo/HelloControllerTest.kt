package com.example.demo

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(controllers = [HelloController::class])
@Import(CodecsConfig::class)
class HelloControllerTest {

    @Autowired
    lateinit var client: WebTestClient

    @Test
    fun `test hello endpoint`() {
        client.get()
            .uri("/hello")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(Color::class.java).hasSize(3)
    }

}