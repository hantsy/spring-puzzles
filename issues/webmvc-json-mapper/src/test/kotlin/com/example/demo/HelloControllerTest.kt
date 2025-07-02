package com.example.demo

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.client.MockMvcWebTestClient
import org.springframework.test.web.servlet.get

@WebMvcTest(controllers = [HelloController::class])
class HelloControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc
    lateinit var client: WebTestClient

    @BeforeEach
    fun setup() {
        client = MockMvcWebTestClient.bindTo(mockMvc)
            .codecs {
                it.customCodecs().register(Jackson2JsonEncoder())
                it.customCodecs().register(Jackson2JsonDecoder())
            }
            .build()
    }

    @Test
    fun `test hello endpoint`() {
        client.get()
            .uri("/hello")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(Color::class.java).hasSize(3)
    }

    @Test
    fun `test hello endpoint with MockMvc`() {
        mockMvc.get("/hello") {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            jsonPath("$[0]") { value("RED") }
            jsonPath("$[1]") { value("GREEN") }
            jsonPath("$[2]") { value("BLUE") }
        }
    }

}
