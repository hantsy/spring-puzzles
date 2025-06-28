package com.example.demo

import com.ninjasquad.springmockk.MockkBean
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.LocalDateTime


@WebFluxTest(controllers = [PostController::class])
class PostControllerTest {

    @Autowired
    lateinit var client: WebTestClient

    @MockkBean
    lateinit var posts: PostRepository

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
        coVerify(inverse = true) { posts.findById(any()) }
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
        val post = Post(id, "test one", "content one", now)
        coEvery { posts.save(any()) } returns post

        val data = Post(null, "title one", "content one", null)
        client.post().uri("/posts").contentType(MediaType.APPLICATION_JSON).bodyValue(data)
            .exchange()
            .expectStatus().isCreated
            .expectHeader().location("/posts/$id")

        coVerify(exactly = 1) { posts.save(any()) }
    }

    @Test
    fun updatePost() {
        val id = 1L
        val now = LocalDateTime.now()
        val post = Post(id, "test one", "content one", now)
        coEvery { posts.findById(any()) } returns post

        val updated = Post(id, "updated test one", " updated content one", now)
        val postSlot = slot<Post>()
        coEvery { posts.save(capture(postSlot)) } returns updated

        val data = Post(null, "updated test one", " updated content one", now)
        client.put().uri("/posts/{id}", id).contentType(MediaType.APPLICATION_JSON).bodyValue(data)
            .exchange()
            .expectStatus().isNoContent

        postSlot.captured.id shouldBe id
        coVerify(exactly = 1) { posts.findById(id) }
        coVerify(exactly = 1) { posts.save(any()) }
    }

    @Test
    fun updatePost_nonExisting() {
        val id = 1L
        coEvery { posts.findById(any()) } returns null

        val updated = Post(id, "updated test one", " updated content one", LocalDateTime.now())
        coEvery { posts.save(any()) } returns updated

        val data = Post(null, "updated test one", " updated content one", LocalDateTime.now())
        client.put().uri("/posts/{id}", id).contentType(MediaType.APPLICATION_JSON).bodyValue(data)
            .exchange()
            .expectStatus().isNotFound

        coVerify(exactly = 1) { posts.findById(id) }
        coVerify(exactly = 0) { posts.save(any()) }
    }

    @Test
    fun deleteById() {
        val id = 1L
        val existedIdSlot = slot<Long>()
        coEvery { posts.existsById(capture(existedIdSlot)) } returns true

        val deletedIdSlot = slot<Long>()
        coEvery { posts.deleteById(capture(deletedIdSlot)) } returns Unit

        client.delete().uri("/posts/{id}", id)
            .exchange()
            .expectStatus().isNoContent

        existedIdSlot.captured shouldBe id
        deletedIdSlot.captured shouldBe id

        coVerify(exactly = 1) { posts.existsById(id) }
        coVerify(exactly = 1) { posts.deleteById(id) }
    }

    @Test
    fun deleteById_nonExisting() {
        val id = 1L
        coEvery { posts.existsById(any()) } returns false
        coEvery { posts.deleteById(any()) } returns Unit

        client.delete().uri("/posts/{id}", id)
            .exchange()
            .expectStatus().isNotFound

        coVerify(exactly = 1) { posts.existsById(id) }
        coVerify(exactly = 0) { posts.deleteById(any()) }
    }
}