package com.example.demo

import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.context.annotation.Import

@DataR2dbcTest
@Import(TestcontainersConfiguration::class, PostRepository::class)
class PostRepositoryTest @Autowired constructor(private val posts: PostRepository) {

    @Test
    fun testCurd() = runTest {
        val id = posts.create(CreatePostRequest("new title", "new content"))

        val all = posts.findAll().toList()
        all.size shouldBe 3

        val p1 = posts.findById(id)
        p1?.id shouldBe id

        val updatedCount = posts.update(id, UpdatePostRequest("updated title", "updated content"))
        updatedCount shouldBeGreaterThan 0

        val p2 = posts.findById(id)
        p2?.title shouldBe "updated title"

        val deleteCount = posts.deleteById(id)
        deleteCount shouldBeGreaterThan 0

        val p3 = posts.findById(id)
        p3 shouldBe null
    }
}