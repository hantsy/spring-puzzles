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

This post will focus on building RESTful services using various Spring technologies, including WebMvc and WebFlux, leveraging both annotated controllers and functional routers. We'll demonstrate how each model can be used to achieve the same RESTful API, allowing you to choose the approach that best fits your project's needs.

---

## WebMvc + Annotated Controllers

This is the classic approach that has been part of Spring for years. Let's start by generating a project at [start.spring.io](https://start.spring.io):

* Project: **Maven**
* Language: **Java**
* Project Metadata: Java **21** 
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

This is a classic Spring controller class, familiar to anyone who has worked with the Spring framework before.

* The `@RestController` annotation designates this class as a RESTful API controller. It is a meta-annotation built on top of the general-purpose `@Controller`.
* The class-level `@RequestMapping` sets the base path for all endpoints in this controller.
* `@RequiredArgsConstructor` simplifies constructor injection by automatically generating a constructor for all final fields at compile time.
* HTTP method-specific annotations like `@GetMapping`, `@PostMapping`, `@PutMapping`, and `@DeleteMapping` are meta-annotations based on `@RequestMapping`, and define sub-paths relative to the class-level mapping.
* The `getAll` method retrieves all posts and returns them as a list in the response body with a 200 (OK) status code.
* The `save` method creates a new post, responds with a 201 (Created) status code, and includes the URI of the new entity in the `Location` header.
* The `getById` method retrieves a post by its ID. If the post exists, it returns the post in the response body with a 200 (OK) status; otherwise, it responds with a 404 (Not Found).
* The `update` method updates an existing post. If the post is found and updated successfully, it returns a 204 (No Content) status. If the post does not exist, it responds with a 404 (Not Found).
* The `deleteById` method deletes a post by its ID. If the deletion is successful, it returns a 204 (No Content) status; if the post does not exist, it responds with a 404 (Not Found).

The [complete example project](https://github.com/hantsy/spring-puzzles/tree/master/programming-models/webmvc) is available on GitHub.


---

## WebMvc + Functional Router

Let’s see how you can use a [`RouterFunction`](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/servlet/function/RouterFunction.html) bean to replace the `PostController` we built earlier.

Start by creating a new project, using the same settings as described in [WebMvc + Annotated Controllers](#webmvc--annotated-controllers). However, this time, instead of the declarative `Repository` interface, we reimplement the CRUD operations in [`PostRepository`](https://github.com/hantsy/spring-puzzles/blob/master/programming-models/webmvc-fn/src/main/java/com/example/demo/DemoApplication.java#L122-L202) using the new [JdbcClient introduced in Spring 6](https://hantsy.medium.com/an-introduction-to-spring-jdbcclient-api-20e833d7b0f3).

Next, declare a `RouterFunction` bean inside a Spring `@Configuration` class as shown below:

```java
@Configuration
class WebConfig {

    @Bean
    RouterFunction<ServerResponse> routerFunction(PostHandler postsHandler) {
        var collectionRoutes = route(method(GET), postsHandler::findAll)
                .andRoute(method(POST), postsHandler::create);
        var singleRoutes = route(method(GET), postsHandler::findById)
                .andRoute(method(PUT), postsHandler::update)
                .andRoute(method(DELETE), postsHandler::deleteById);

        return route()
                .path("posts",
                        () -> nest(path("{id}"), singleRoutes)
                                .andNest(path(""), collectionRoutes)
                )
                .build();
    }
}
```

The `RouterFunctions.route()` method lets you build up a `RouterFunction` using a clean, fluent builder API. You can use the `nest` method to define subroutes under a common path. Each route requires a `RequestPredicate`, which specifies details such as the request path, HTTP method, accepted media types, or content type, as well as a `HandlerFunction` that processes the request. The `RequestPredicates` utility class provides convenient methods for constructing these predicates. A `HandlerFunction` is just a functional interface that takes a `ServerRequest` and returns a `ServerResponse`. Once all routes are defined, calling `build()` assembles them into a `RouterFunction<ServerResponse>`.

Notice that the handler functions are provided as method references to the corresponding methods in the [`PostHandler`](https://github.com/hantsy/spring-puzzles/blob/master/programming-models/webmvc-fn/src/main/java/com/example/demo/DemoApplication.java#L76-L120) bean, allowing you to centralize your request handling logic.

Here’s what the `PostHandler` class looks like:

```java
@Component
@RequiredArgsConstructor
class PostHandler {
    private final PostRepository posts;

    ServerResponse findAll(ServerRequest request) {
        return ok().body(posts.findAll());
    }

    ServerResponse findById(ServerRequest request) {
        var id = Long.parseLong(request.pathVariable("id"));
        return this.posts.findById(id)
                .map(p -> ok().body(p))
                .orElse(notFound().build());
    }

    ServerResponse create(ServerRequest request) throws ServletException, IOException {
        var data = request.body(Post.class);
        var savedId = this.posts.create(data);
        return ServerResponse.created(URI.create("/posts/" + savedId)).build();
    }

    ServerResponse update(ServerRequest request) throws ServletException, IOException {
        var id = Long.parseLong(request.pathVariable("id"));
        var data = request.body(Post.class);
        var updatedCount = this.posts.update(id, data);

        if (updatedCount > 0) {
            return noContent().build();
        } else {
            return notFound().build();
        }
    }

    ServerResponse deleteById(ServerRequest request) {
        var id = Long.parseLong(request.pathVariable("id"));
        var deletedCount = this.posts.deleteById(id);

        if (deletedCount > 0) {
            return noContent().build();
        } else {
            return notFound().build();
        }
    }
}
```

This class is a straightforward Spring `@Component`, with each method serving as an implementation of `HandlerFunction`. The logic for handling each HTTP request type—listing, fetching, creating, updating, and deleting posts—is cleanly separated into methods.

The [complete example project](https://github.com/hantsy/spring-puzzles/tree/master/programming-models/webmvc-fn) is available on GitHub.

---

## WebFlux + Annotated Controllers

Spring 5 introduced support for the [Reactive Streams](https://www.reactive-streams.org/) specification, completely overhauling web request handling in the Spring WebFlux module using [Reactor](https://projectreactor.io). With WebFlux, you can leverage the familiar annotations from the classic WebMvc module to build RESTful services on top of the new Reactor APIs.

> [!Note]
> Reactor is a [Reactive Streams for JVM](https://github.com/reactive-streams/reactive-streams-jvm/) implementation. If you’re new to Reactor, check out InfoQ’s [Reactor by Example](https://www.infoq.com/articles/reactor-by-example/) for a solid introduction.

To get started, create a new project at [start.spring.io](https://start.spring.io) with the following settings:
- Project: **Maven**
- Language: **Java**
- Project Metadata: Java **21**
- Spring Boot: **3.5**
- Dependencies: *Reactive Web*, *Data R2dbc*, *PostgreSQL*, *Testcontainers*, *Lombok*, etc.

Choose the *Reactive Web* dependency to set up a WebFlux application, and add *Data R2dbc* for reactive database access via the R2dbc driver for PostgreSQL. 

Next, define your table-mapped entity class [`Post`](https://github.com/hantsy/spring-puzzles/blob/master/programming-models/webflux/src/main/java/com/example/demo/DemoApplication.java#L104-L119), and create a corresponding [`PostRepository`](https://github.com/hantsy/spring-puzzles/blob/master/programming-models/webflux/src/main/java/com/example/demo/DemoApplication.java#L97-L99) that extends [`R2dbcRepository`](https://docs.spring.io/spring-data/r2dbc/docs/current/api/org/springframework/data/r2dbc/repository/R2dbcRepository.html).

With these in place, you can move on to creating your `PostController` bean.

```java
@RequiredArgsConstructor
@RestController
@RequestMapping("/posts")
class PostController {

    private final PostRepository posts;

    @GetMapping("")
    public ResponseEntity<Flux<Post>> all() {
        return ok(this.posts.findAll());
    }

    @GetMapping("{id}")
    public Mono<ResponseEntity<Post>> get(@PathVariable("id") Long id) {
        return this.posts.findById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(notFound().build());
    }

    @PostMapping("")
    public Mono<ResponseEntity<?>> create(@RequestBody Post post) {
        return this.posts.save(post)
                .map(p -> created(URI.create("/posts/" + p.id())).build());
    }

    @PutMapping("{id}")
    public Mono<ResponseEntity<Object>> update(@PathVariable Long id, @RequestBody Post data) {
        return this.posts.findById(id)
                .flatMap(p -> {
                    var updated = new Post(p.id(), data.title(), data.content(), p.createdAt());
                    return this.posts.save(updated)
                            .then(Mono.fromCallable(() -> noContent().build()));
                })
                .defaultIfEmpty(notFound().build());
    }

    @DeleteMapping("{id}")
    public Mono<ResponseEntity<?>> deleteById(@PathVariable Long id) {
        return this.posts.existsById(id)
                .flatMap(b -> {
                    if (b) return this.posts.deleteById(id)
                            .then(Mono.fromCallable(() -> noContent().build()));
                    else return Mono.just(notFound().build());
                });
    }
}
```
The controller above closely mirrors a traditional WebMvc controller, but utilizes Reactor’s APIs to enable fully non-blocking, reactive data processing. Instead of returning `ResponseEntity<List<BodyType>>` or `ResponseEntity<BodyType>` as in classic controllers, this WebFlux controller returns `ResponseEntity<Flux<BodyType>>` for streaming multiple results, and `Mono<ResponseEntity<BodyType>>` for single-result or empty responses. 

The [complete example project](https://github.com/hantsy/spring-puzzles/tree/master/programming-models/webflux) is available on GitHub.

## WebFlux + Functional Router

Functional programming has gained significant traction in the development community. As an alternative to annotated controllers, Spring 5 introduced [`RouterFunction`](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/reactive/function/server/RouterFunction.html), which allows you to define web routes using builder-style, fluent APIs. This approach has also been brought back to the classic WebMVC/Servlet stack, which we have already introduced in the *WebMvc + Functional Router* section.

To get started, you can just create a new project with the same settings as described in the *WebFlux + Annotated Controllers* section. Next, prepare the entity class [`Post`](https://github.com/hantsy/spring-puzzles/blob/master/programming-models/webflux-fn/src/main/java/com/example/demo/DemoApplication.java#L211) and related [`PostRepository`](https://github.com/hantsy/spring-puzzles/blob/master/programming-models/webflux-fn/src/main/java/com/example/demo/DemoApplication.java#L125-L210) as we did in the previous sections.

Finally, define a `RouterFunction<ServerResponse>` bean within a Spring `@Configuration` class to set up your routing logic.

```java
@Configuration
class WebConfig {

    @Bean
    public RouterFunction<ServerResponse> routes(PostHandler postsHandler) {
        var collectionRoutes = route(method(GET), postsHandler::findAll)
                .andRoute(method(POST), postsHandler::create);
        var singleRoutes = route(method(GET), postsHandler::findById)
                .andRoute(method(PUT), postsHandler::update)
                .andRoute(method(DELETE), postsHandler::deleteById);

        return route()
                .path("posts",
                        () -> nest(path("{id}"), singleRoutes)
                                .andNest(path(""), collectionRoutes)
                )
                .build();
    }
}

```

The bean definition looks very similar to the one we just created for classic WebMvc.  Be sure you’re importing everything from `org.springframework.web.reactive.function.server`.

Here’s what the related [`PostHandler`](https://github.com/hantsy/spring-puzzles/blob/master/programming-models/webflux-fn/src/main/java/com/example/demo/DemoApplication.java#L85-L123) looks like.

```java
@Component
@RequiredArgsConstructor
class PostHandler {

    private final PostRepository posts;

    public Mono<ServerResponse> findAll(ServerRequest req) {
        return ok().body(this.posts.findAll(), Post.class);
    }

    public Mono<ServerResponse> create(ServerRequest req) {
        return req.bodyToMono(Post.class)
                .flatMap(this.posts::create)
                .flatMap(postId -> created(URI.create("/posts/" + postId)).build());
    }

    public Mono<ServerResponse> findById(ServerRequest req) {
        return this.posts.findById(Long.valueOf(req.pathVariable("id")))
                .flatMap(post -> ok().body(Mono.just(post), Post.class))
                .switchIfEmpty(notFound().build());
    }

    public Mono<ServerResponse> update(ServerRequest req) {
        return req.bodyToMono(Post.class)
                .flatMap(p -> this.posts.update(Long.valueOf(req.pathVariable("id")), p))
                .flatMap(d -> {
                    if (d > 0) return noContent().build();
                    else return notFound().build();
                });
    }

    public Mono<ServerResponse> deleteById(ServerRequest req) {
        return this.posts.deleteById(Long.valueOf(req.pathVariable("id")))
                .flatMap(d -> {
                    if (d > 0) return noContent().build();
                    else return notFound().build();
                });
    }
}
```

The `PostHandler` above is quite similar to its WebMvc counterpart. The key difference is that it embraces Reactive Streams, with each `HandlerFunction` following the contract `ServerRequest req -> Mono<ServerResponse>`. 

Check out the [full example project](https://github.com/hantsy/spring-puzzles/tree/master/programming-models/webflux-fn) on GitHub for a complete walkthrough.

## WebFlux + Kotlin Coroutines + Annotated Controllers

Kotlin Coroutines is a core Kotlin library that enables structured concurrency concisely and efficiently. Coroutines simplify asynchronous code by allowing you to write it imperatively, using the `suspend` keyword to mark functions as continuations that can be paused and resumed without blocking threads. For handling streams of data asynchronously, coroutines provide the `Flow` API, which supports reactive-style operations.

Since Spring 5, Kotlin has been a first-class citizen in the Spring ecosystem. Building on top of its Reactive Streams support, Spring adds seamless integration with Kotlin Coroutines:
* The coroutine context can interoperate with the Reactor context.
* In addition to the Kotlin extensions available in [`reactor-kotlin-extensions`](https://github.com/reactor/reactor-kotlin-extensions) and [`kotlinx-coroutines-reactor`](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-reactor/), Spring provides many extension functions to bridge Kotlin Coroutines and Reactive Streams APIs.
* Many Spring components, such as `@Component`, `@EventListener`, and `WebFilter`, natively support suspending functions, allowing you to use `suspend` functions just like regular Kotlin functions.

> [!NOTE]
> For more details, refer to the [Kotlin Coroutines official guide](https://kotlinlang.org/docs/coroutines-guide.html#table-of-contents).

To get started, create a new project at [start.spring.io](https://start.spring.io) with the following settings:
- Project: **Maven**
- Language: **Kotlin**
- Project Metadata: Java **21**
- Spring Boot: **3.5**
- Dependencies: *Reactive Web*, *Data R2dbc*, *PostgreSQL*, *Testcontainers*, etc. 

Here, we choose **Kotlin** as the programming language. Due to its concise syntax, we also remove **Lombok** from the dependencies. 

Create a data class [`Post`](https://github.com/hantsy/spring-puzzles/blob/master/programming-models/webflux-ktco/src/main/kotlin/com/example/demo/DemoApplication.kt#L86C1-L92C2) to present the table mapped entity, and related [`PostRepository`](https://github.com/hantsy/spring-puzzles/blob/master/programming-models/webflux-ktco/src/main/kotlin/com/example/demo/DemoApplication.kt#L84), which extends a coroutine-aware [`CoroutineCrudRepository`](https://docs.spring.io/spring-data/jpa/reference/data-commons/kotlin/coroutines.html).

Then let's move on to [`PostController`] class.

```kotlin
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
```
The controller is very similar to the WebMvc version.  It replaces the Reactor `Mono/Flux` codes with a simple `suspend` modifier on the functions.

Grab the [full example code](https://github.com/hantsy/spring-puzzles/blob/master/programming-models/webflux-ktco/) from GitHub and explore it yourself.

## WebFlux + Kotlin Coroutines + Functional Router

Based on the WebFlux [`RouterFunction`](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/reactive/function/server/RouterFunction.html), Spring provides a Kotlin DSL - [CoRouterFunctionDsl](https://docs.spring.io/spring-framework/docs/current/kdoc-api/spring-webflux/org.springframework.web.reactive.function.server/-co-router-function-dsl/index.html) and allows you to write route functions in a `coRouter{}` context block.

Create a new project using the same settings in *WebFlux + Kotlin Coroutines + Annotated Controllers* section.

Delcare a bean in the Spring `@Configuration` class..







