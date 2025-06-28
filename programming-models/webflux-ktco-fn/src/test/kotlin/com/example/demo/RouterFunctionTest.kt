package com.example.demo

import com.ninjasquad.springmockk.MockkBean
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import java.time.LocalDateTime

@WebFluxTest
class RouterFunctionTest {

    @TestConfiguration
    @Import(value = [WebConfig::class, PostHandler::class])
    class TestConfig

    @Autowired
    lateinit var routerFunction: RouterFunction<ServerResponse>

    @MockkBean
    lateinit var posts: PostRepository

    lateinit var client: WebTestClient

    @BeforeEach
    fun setUp() {
        client = WebTestClient.bindToRouterFunction(routerFunction).build()
    }

    @Test
    fun getAll() {
        val now = LocalDateTime.now()
        coEvery { posts.findAll() } returns flowOf(
            Post(1L, "test one", "content one", now),
            Post(2L, "test two", "content two", now)
        )

        client.get().uri("/posts").accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody().jsonPath("$.size()").isEqualTo(2)

        coVerify(exactly = 1) { posts.findAll() }
    }

    @Test
    fun getPostById() {
        val id = 1L
        val now = LocalDateTime.now()
        val post = Post(id, "test one", "content one", now)
        val idSlot = slot<Long>()
        coEvery { posts.findById(capture(idSlot)) } returns post

        client.get().uri("/posts/{id}", id).accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody().jsonPath("$.id").isEqualTo(id)

        idSlot.captured shouldBe id
        coVerify(exactly = 1) { posts.findById(id) }
    }

    @Test
    fun getPostById_nonExisting() {
        val id = 1L
        coEvery { posts.findById(any()) } returns null

        client.get().uri("/posts/{id}", id).accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound

        coVerify(exactly = 1) { posts.findById(id) }
    }

    @Test
    fun createPost() {
        val id = 1L
        val now = LocalDateTime.now()
        coEvery { posts.create(any()) } returns id

        val data = Post(null, "title one", "content one", null)
        client.post().uri("/posts").contentType(MediaType.APPLICATION_JSON).bodyValue(data)
            .exchange()
            .expectStatus().isCreated
            .expectHeader().location("/posts/$id")

        coVerify(exactly = 1) { posts.create(any()) }
    }

    @Test
    fun updatePost() {
        val id = 1L
        val postSlot = slot<UpdatePostRequest>()
        coEvery { posts.update(any(), capture(postSlot)) } returns  1L

        val data = Post(null, "updated test one", " updated content one", LocalDateTime.now())
        client.put().uri("/posts/{id}", id).contentType(MediaType.APPLICATION_JSON).bodyValue(data)
            .exchange()
            .expectStatus().isNoContent

        postSlot.captured.title shouldBe "updated test one"

        coVerify(exactly = 1) { posts.update(id, any()) }
    }

    @Test
    fun updatePost_nonExisting() {
        val id = 1L
        coEvery { posts.update(any(), any()) } returns 0L

        val data = Post(null, "updated test one", " updated content one", LocalDateTime.now())
        client.put().uri("/posts/{id}", id).contentType(MediaType.APPLICATION_JSON).bodyValue(data)
            .exchange()
            .expectStatus().isNotFound

        coVerify(exactly = 1) { posts.update(id, any()) }
    }

    @Test
    fun deleteById() {
        val id = 1L
        val deletedIdSlot = slot<Long>()
        coEvery { posts.deleteById(capture(deletedIdSlot)) } returns  1L

        client.delete().uri("/posts/{id}", id)
            .exchange()
            .expectStatus().isNoContent

        deletedIdSlot.captured shouldBe id

        coVerify(exactly = 1) { posts.deleteById(id) }
    }

    @Test
    fun deleteById_nonExisting() {
        val id = 1L
        coEvery { posts.deleteById(any()) } returns  0L

        client.delete().uri("/posts/{id}", id)
            .exchange()
            .expectStatus().isNotFound

        coVerify(exactly = 1) { posts.deleteById(id) }
    }
}