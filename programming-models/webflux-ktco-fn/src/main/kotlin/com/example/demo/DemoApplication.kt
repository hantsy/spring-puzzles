package com.example.demo

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import org.springframework.data.annotation.Id
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.delete
import org.springframework.data.r2dbc.core.insert
import org.springframework.data.r2dbc.core.select
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.Query
import org.springframework.data.relational.core.query.Update
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import org.springframework.web.reactive.function.server.ServerResponse.*
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
        posts.findAll().collect { println(it) }
    }

}

@Configuration
class WebConfig {

    @Bean
    fun routes(postHandler: PostHandler) = coRouter {
        "/posts".nest {
            "{id}".nest {
                GET(accept(MediaType.APPLICATION_JSON), postHandler::get)
                PUT(contentType(APPLICATION_JSON), postHandler::update)
                DELETE("", postHandler::delete)
            }
            GET(accept(MediaType.APPLICATION_JSON), postHandler::all)
            POST(contentType(APPLICATION_JSON), postHandler::create)
        }
    }
}

@Component
class PostHandler(private val posts: PostRepository) {

    suspend fun all(req: ServerRequest): ServerResponse {
        return ok().bodyAndAwait(this.posts.findAll())
    }

    suspend fun create(req: ServerRequest): ServerResponse {
        val body = req.awaitBody<CreatePostRequest>()
        val createdPost = this.posts.create(body)
        return created(URI.create("/$createdPost")).buildAndAwait()
    }

    suspend fun get(req: ServerRequest): ServerResponse {
        println("path variable::${req.pathVariable("id")}")
        val foundPost = this.posts.findById(req.pathVariable("id").toLong())
        println("found post:$foundPost")
        return when {
            foundPost != null -> ok().bodyValueAndAwait(foundPost)
            else -> notFound().buildAndAwait()
        }
    }

    suspend fun update(req: ServerRequest): ServerResponse {
        val id = req.pathVariable("id").toLong()
        val body = req.awaitBody<UpdatePostRequest>()
        val updated = this.posts.update(id, body)
        return when {
            updated > 0 -> noContent().buildAndAwait()
            else -> notFound().buildAndAwait()
        }
    }

    suspend fun delete(req: ServerRequest): ServerResponse {
        val deletedCount = this.posts.deleteById(req.pathVariable("id").toLong())
        println("$deletedCount posts deleted")
        return when {
            deletedCount > 0 -> noContent().buildAndAwait()
            else -> notFound().buildAndAwait()
        }
    }
}

@Component
class PostRepository(private val template: R2dbcEntityTemplate) {

    suspend fun count(): Long =
        template.select<Post>().count().awaitSingle()

    fun findAll(): Flow<Post> =
        template.select<Post>()
            .all()
            .asFlow()

    suspend fun findById(id: Long): Post? =
        template.selectOne(Query.query(where("id").`is`(id)), Post::class.java)
            .awaitSingleOrNull()

    suspend fun deleteById(id: Long): Long =
        template.delete(Query.query(where("id").`is`(id)), Post::class.java)
            .awaitSingle()

    suspend fun deleteAll(): Long =
        template.delete<Post>()
            .all()
            .awaitSingle()

    suspend fun create(post: CreatePostRequest): Long {
        val data = Post(title = post.title, content = post.content)
        return template.insert<Post>()
            .using(data)
            .map { it.id!! }
            .awaitSingle()
    }


    suspend fun update(id: Long, post: UpdatePostRequest): Long =
        template.update(
            Query.query(where("id").`is`(id)),
            Update.update("title", post.title)
                .set("content", post.content),
            Post::class.java
        ).awaitSingle()

    suspend fun init() {
        //client.execute().sql("CREATE TABLE IF NOT EXISTS posts (login varchar PRIMARY KEY, firstname varchar, lastname varchar);").await()
        val deletedCount = deleteAll()
        println(" $deletedCount posts deleted!")
        create(CreatePostRequest(title = "My first post title", content = "Content of my first post"))
        create(CreatePostRequest(title = "My second post title", content = "Content of my second post"))
    }
}

data class CreatePostRequest(val title: String, val content: String)

data class UpdatePostRequest(val title: String, val content: String)

@Table("posts")
data class Post(
    @Id val id: Long? = null,
    @Column("title") val title: String? = null,
    @Column("content") val content: String? = null,
    @Column("created_at") val createdAt: LocalDateTime? = null
)


