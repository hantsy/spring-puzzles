package com.example.demo

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.flow.toList
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.runApplication
import org.springframework.context.event.EventListener
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.kotlin.CoroutineSortingRepository
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.*
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.time.LocalDateTime

@SpringBootApplication
class DemoApplication

fun main(args: Array<String>) {
    runApplication<DemoApplication>(*args)
}

@Component
class DataInitializer(private val posts: PostRepository) {

    @EventListener(value = [ApplicationReadyEvent::class])
    suspend fun init() {
        println(" print initial data...")
        posts.findAll().collect { println("initial post: $it") }
    }
}


@RestController
@RequestMapping("/posts")
class PostController(private val posts: PostRepository) {

    @GetMapping("")
    fun findAll(): Flow<Post> = posts.findAll()

    @GetMapping("{id}")
    suspend fun findOne(@PathVariable id: Long): ResponseEntity<Post> {
        return posts.findById(id)
            ?.let { ok(it) } ?: notFound().build()
    }

    @PostMapping("")
    suspend fun save(@RequestBody post: Post): ResponseEntity<Any> {
        val saved = posts.save(post)
        return created(URI.create("/posts/" + saved.id)).build()
    }

    @PutMapping("{id}")
    suspend fun update(@PathVariable id: Long, @RequestBody post: Post): ResponseEntity<Any> {
        val existed = posts.findById(id) ?: run {
            return notFound().build()
        }

        val updated = existed.apply {
            title = post.title
            content = post.content
        }
        posts.save(updated)
        return noContent().build()
    }

    @DeleteMapping("{id}")
    suspend fun deleteById(@PathVariable id: Long): ResponseEntity<Any> {
        if (!posts.existsById(id)) {
            return notFound().build()
        }
        posts.deleteById(id)
        return noContent().build()
    }

}

interface PostRepository : CoroutineCrudRepository<Post, Long>, CoroutineSortingRepository<Post, Long>

@Table("posts")
data class Post(
    @Id val id: Long? = null,
    @Column("title") var title: String? = null,
    @Column("content") var content: String? = null,
    @Column("created_at") val createdAt: LocalDateTime? = null
)


