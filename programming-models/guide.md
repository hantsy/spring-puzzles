# Six Programming Models You Must Know When Building RESTful Services with Spring Boot

Over the past few years, [Spring Framework](https://spring.io/projects/spring-framework) has rapidly evolved, bringing a wide array of innovations that empower developers to build robust and modern RESTful services with ease. Since Spring 5.0, it has adopted new paradigms and technologies that reflect the changing landscape of software development.

One of the most notable advancements is the adoption of the **[Reactive Streams](https://www.reactive-streams.org/)** standard, which introduces a reactive programming model as a powerful alternative to the traditional servlet-based WebMvc stack. This addition enables developers to build highly scalable, event-driven web applications that can efficiently handle a large number of concurrent connections.

Meanwhile, **Kotlin**’s rise in popularity on the JVM has led Spring to offer first-class support for the language, culminating in seamless integration with [Kotlin Coroutines](https://docs.spring.io/spring-framework/reference/languages/kotlin.html#kotlin-coroutines). This allows developers to write concise, asynchronous, and non-blocking code in a style that closely resembles traditional imperative programming.

Functional programming concepts are also being incorporated into mainstream Java development. Spring has responded by introducing the **[RouterFunction](https://docs.spring.io/spring-framework/reference/web/webflux-functional.html)** API, enabling developers to define web handling in a functional, declarative style.

Additionally, Spring has even backported RouterFunction and the Kotlin DSL to the traditional WebMvc stack, bridging the gap between imperative and reactive programming approaches.

For a developer building RESTful services with Spring Boot, you now have a variety of powerful programming models to choose from, including the WebMvc or WebFlux for the tech stack, annotated controllers or functional router for code styles, etc.

In this article, we will explore **six essential programming models** that every Spring Boot developer should be familiar with when building RESTful services.

---

## Prerequisites

Before you begin, make sure you have the following software installed:

* **Apache Maven 3.9 or 4.0**: We use Maven in these examples, but you’re welcome to use Gradle if you prefer. You can easily convert the provided Maven POM files to Gradle scripts.
* **Java 21**
* **Docker** (Docker Desktop is recommended for Windows users)
* Your favorite IDE, eg, IntelliJ IDEA Community Edition, or VS Code, etc.

As usual, we’ll use the blog example project to demonstrate each feature. 

For our database, we’ll use PostgreSQL. A [Docker Compose](https://github.com/hantsy/spring-puzzles/blob/master/programming-models/docker-compose.yml) file is provided to spin up the database service during development. When running tests, we’ll rely on Testcontainers to manage the database for us. Both approaches use the same scripts to initialize the database, including schema and seed data. You can find these scripts in [schema.sql](https://github.com/hantsy/spring-puzzles/blob/master/programming-models/webmvc/src/main/resources/schema.sql) and [data.sql](https://github.com/hantsy/spring-puzzles/blob/master/programming-models/webmvc/src/main/resources/data.sql).

The REST API for managing `POST` entities will support the following operations:

| URI             | REQUEST                                         | RESPONSE                                                                |
|-----------------|-------------------------------------------------|-------------------------------------------------------------------------|
| GET /posts      | accept: application/json                        | status: 200<br>[{"id":1, "title":"post title", ...}]                    |
| POST /posts     | content-type: application/json<br>{"title":"new title", "content":"new content"} | status: 201<br>location: /posts/&lt;newid&gt;                            |
| GET /posts/{id} | accept: application/json                        | status: 200<br>{"id":1, "title":"post title", ...}                      |
| PUT /posts/{id} | content-type: application/json<br>{"title":"new title", "content":"new content"} | status: 204                                                             |
| DELETE /posts/{id} |                                               | status: 204                                                             |

The primary focus of this post is on building RESTful services using various combinations of Spring technologies, including WebMvc or WebFlux, with annotated controllers or functional routers. We’ll discuss persistence options in a future post.

---

## WebMvc + Annotated Controllers

This is the classic approach that has been part of Spring for years. Let's start by generating a project at [start.spring.io](https://start.spring.io):

* Project: **Maven**
* Java: **21** 
* Spring Boot: **3.5**
* Dependencies: *Web*, *Data JDBC*, *PostgreSQL*, *Testcontainers*, *Lombok*, etc.

Extract the project skeleton and import the code into your IDE. Then implement the table-mapped entity class [`Post`](https://github.com/hantsy/spring-puzzles/blob/master/programming-models/webmvc/src/main/java/com/example/demo/DemoApplication.java#L94-L108). Next, create a [`PostRepository`](https://github.com/hantsy/spring-puzzles/blob/master/programming-models/webmvc/src/main/java/com/example/demo/DemoApplication.java#L91) interface that extends `CrudRepository`.

Additionally, add [`schema.sql`](https://github.com/hantsy/spring-puzzles/blob/master/programming-models/webmvc/src/main/resources/schema.sql) and [`data.sql`](https://github.com/hantsy/spring-puzzles/blob/master/programming-models/webmvc/src/main/resources/data.sql) files to the project’s *src/main/resources* directory. Be sure to set `spring.sql.init.mode=always` in *application.properties* so these database scripts are always executed when the application starts.

Once that’s done, let’s move on to the [`PostController`](https://github.com/hantsy/spring-puzzles/blob/master/programming-models/webmvc/src/main/java/com/example/demo/DemoApplication.java#L44-L89) class.

```java
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
class PostController {
    private final PostRepository posts;

    @GetMapping()
    public ResponseEntity<?> getAll() {
        return ok(posts.findAll());
    }

    @PostMapping()
    public ResponseEntity<?> save(@RequestBody Post body) {
        var saved = this.posts.save(body);
        return ResponseEntity.created(URI.create("/posts/" + saved.id())).build();
    }

    @GetMapping("{id}")
    public ResponseEntity<?> getById(@PathVariable("id") Long id) {
        return this.posts.findById(id)
                .map(ResponseEntity::ok)
                .orElse(notFound().build());
    }

    @PutMapping("{id}")
    public ResponseEntity<?> update(@PathVariable("id") Long id, @RequestBody Post body) {
        return this.posts.findById(id)
                .map(existed -> new Post(existed.id(), body.title(), body.content(), existed.createdAt()))
                .map(this.posts::save)
                .map(post -> ResponseEntity.noContent().build())
                .orElse(notFound().build());
    }

    @DeleteMapping("{id}")
    public ResponseEntity<?> deletedById(@PathVariable("id") Long id) {
        return Optional.of(this.posts.existsById(id))
                .filter(it -> it)
                .map(deleted -> {
                    this.posts.deleteById(id);
                    return ResponseEntity.noContent().build();
                })
                .orElse(notFound().build());
    }
}
```

This is a classic Spring controller class, familiar to anyone who has worked with Spring before.

* The `@RestController` annotation designates this class as a RESTful API controller. It is a meta-annotation built on top of the general-purpose `@Controller`.
* The class-level `@RequestMapping` sets the base path for all endpoints in this controller.
* `@RequiredArgsConstructor` simplifies constructor injection by automatically generating a constructor for all final fields at compile time.
* HTTP method-specific annotations like `@GetMapping`, `@PostMapping`, `@PutMapping`, and `@DeleteMapping` are meta-annotations based on `@RequestMapping`, and define sub-paths relative to the class-level mapping.
* The `getAll` method retrieves all posts and returns them as a list in the response body with a 200 (OK) status code.
* The `save` method creates a new post, responds with a 201 (Created) status code, and includes the URI of the new entity in the `Location` header.
* The `getById` method retrieves a post by its ID. If the post exists, it returns the post in the response body with a 200 (OK) status; otherwise, it responds with a 404 (Not Found).
* The `update` method updates an existing post. If the post is found and updated successfully, it returns a 204 (No Content) status. If the post does not exist, it responds with a 404 (Not Found).
* The `deleteById` method deletes a post by its ID. If the deletion is successful, it returns a 204 (No Content) status; if the post does not exist, it responds with a 404 (Not Found).

---
