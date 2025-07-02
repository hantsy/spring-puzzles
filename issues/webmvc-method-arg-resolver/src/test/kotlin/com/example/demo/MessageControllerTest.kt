package com.example.demo

import com.ninjasquad.springmockk.MockkBean
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Import
import org.springframework.test.web.reactive.server.WebTestClient

@WebMvcTest
class MessageControllerTest {

    @TestConfiguration
    @Import(WebConfig::class)
    class TestConfig

    @Autowired
    lateinit var client: WebTestClient

    @MockkBean
    lateinit var messageRepository: MessageRepository

    @Test
    fun `get all messages`() {
        val slot = slot<Map<String, Boolean>>()
        every { messageRepository.getAll(capture(slot)) } returns
                listOf(
                    Message(1, "Hello"),
                    Message(2, "World")
                )

        client.get()
            .uri("/messages?sort=id")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(Message::class.java).hasSize(2)

        slot.captured shouldBe mapOf("id" to true)
        verify(exactly = 1) { messageRepository.getAll(any()) }
    }

}