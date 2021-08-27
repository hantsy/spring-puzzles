# Integrating Hibernate Reactive with Spring

Hibernate started a subproject - Hibernate Reactive for Reactive Streams support, but at the moment when I wrote this post, Spring still did not embrace Hibernate Reactive. The good news is the integration work is not complex. In this post, we will attempt to integrate the latest Hibernate Reactive with Spring framework.

In the former post [Integrating Vertx with Spring framework](https://itnext.io/integrating-vertx-application-with-spring-framework-fb8fca81a357) and [the further post](https://itnext.io/building-a-vertx-application-with-smallrye-mutiny-bindings-spring-and-hibernate-reactive-5cf10b57983a) , we have integrated Hibernate Reactive with Spring IOC container, but in those the posts, the web handling is done by Vertx Web. In this post, we will use the existing Spring WebFlux instead.

Open your browser and navigate to https://start.spring.io, and generate a Spring project skeleton with the following  dependencies,

* *WebFlux*
*  *Lombok*

Extract the downloaded files into disc, and import the project into your IDE. 

Open the project *pom.xml* file, add the following dependencies.

```xml
<dependency>
    <groupId>io.vertx</groupId>
    <artifactId>vertx-pg-client</artifactId>
    <version>${vertx-pg-client.version}</version>
</dependency>

<dependency>
    <groupId>org.hibernate.reactive</groupId>
    <artifactId>hibernate-reactive-core</artifactId>
    <version>${hibernate-reactive.version}</version>
</dependency>

<dependency>
    <groupId>org.hibernate</groupId>
    <artifactId>hibernate-jpamodelgen</artifactId>
    <optional>true</optional>
</dependency>
```

In the above the codes:

* The `vertx-pg-client` is the Postgres reactive driver which is required by Hibernate Reactive.
* The `hibernate-reactive-core` is the core dependency of Hibernate Reactive.
*  Similar to the general Hibernate/JPA support, `hibernate-jpamodelgen` is used to generate entity metadata classes from the `@Entity` classes.

Add a *persistence.xml* to *src/main/resources/META-INF* folder.

```xml
<persistence xmlns="http://xmlns.jcp.org/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/persistence http://xmlns.jcp.org/xml/ns/persistence/persistence_2_2.xsd"
             version="2.2">

    <persistence-unit name="blogPU">
        <provider>org.hibernate.reactive.provider.ReactivePersistenceProvider</provider>

        <class>com.example.demo.Post</class>

        <properties>

            <!-- PostgreSQL -->
            <property name="javax.persistence.jdbc.url"
                      value="jdbc:postgresql://localhost:5432/blogdb"/>

            <!-- Credentials -->
            <property name="javax.persistence.jdbc.user"
                      value="user"/>
            <property name="javax.persistence.jdbc.password"
                      value="password"/>

            <!-- The Vert.x SQL Client connection pool size -->
            <property name="hibernate.connection.pool_size"
                      value="10"/>

            <!-- Automatic schema export -->
            <property name="javax.persistence.schema-generation.database.action"
                      value="drop-and-create"/>

            <!-- SQL statement logging -->
            <property name="hibernate.show_sql" value="true"/>
            <property name="hibernate.format_sql" value="true"/>
            <property name="hibernate.highlight_sql" value="true"/>

        </properties>

    </persistence-unit>

</persistence>
```

Note the `provider` must use the `ReactivePersistenceProvider` class which is provided in the new Hibernate Reactive. And you have to add all your entity classes in this *persistence.xml* file.

Then declare a  `Mutiny.SessionFactory` bean. The `blogPU` is the persistence unit name configured in the *persistence.xml* file.

```java
@Bean
public Mutiny.SessionFactory sessionFactory() {
    return Persistence.createEntityManagerFactory("blogPU")
        .unwrap(Mutiny.SessionFactory.class);
}
```

Create a sample entity class.

```java
@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@Builder
@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    UUID id;
    String title;
    String content;

    @Builder.Default
    @Column(name = "created_at")
    @CreationTimestamp
    LocalDateTime createdAt = LocalDateTime.now();
}
```

And then create a  `Repository` class for it.

```java
@Component
@RequiredArgsConstructor
public class PostRepository {
    private static final Logger LOGGER = Logger.getLogger(PostRepository.class.getName());

    private final Mutiny.SessionFactory sessionFactory;

    public Uni<List<Post>> findAll() {
        CriteriaBuilder cb = this.sessionFactory.getCriteriaBuilder();
        // create query
        CriteriaQuery<Post> query = cb.createQuery(Post.class);
        // set the root class
        Root<Post> root = query.from(Post.class);
        return this.sessionFactory.withSession(session -> session.createQuery(query).getResultList());
    }

    public Uni<List<Post>> findByKeyword(String q, int offset, int limit) {

        CriteriaBuilder cb = this.sessionFactory.getCriteriaBuilder();
        // create query
        CriteriaQuery<Post> query = cb.createQuery(Post.class);
        // set the root class
        Root<Post> root = query.from(Post.class);

        // if keyword is provided
        if (q != null && !q.trim().isEmpty()) {
            query.where(
                cb.or(
                    cb.like(root.get(Post_.title), "%" + q + "%"),
                    cb.like(root.get(Post_.content), "%" + q + "%")
                )
            );
        }
        //perform query
        return this.sessionFactory.withSession(session -> session.createQuery(query)
            .setFirstResult(offset)
            .setMaxResults(limit)
            .getResultList());
    }


    public Uni<Post> findById(UUID id) {
        Objects.requireNonNull(id, "id can not be null");
        return this.sessionFactory.withSession(session -> session.find(Post.class, id))
            .onItem().ifNull().failWith(() -> new PostNotFoundException(id));
    }

    public Uni<Post> save(Post post) {
        if (post.getId() == null) {
            return this.sessionFactory.withSession(session ->
                session.persist(post)
                    .chain(session::flush)
                    .replaceWith(post)
            );
        } else {
            return this.sessionFactory.withSession(session -> session.merge(post).onItem().call(session::flush));
        }
    }

    public Uni<Integer> deleteById(UUID id) {
        CriteriaBuilder cb = this.sessionFactory.getCriteriaBuilder();
        // create delete
        CriteriaDelete<Post> delete = cb.createCriteriaDelete(Post.class);
        // set the root class
        Root<Post> root = delete.from(Post.class);
        // set where clause
        delete.where(cb.equal(root.get(Post_.id), id));
        // perform update
        return this.sessionFactory.withTransaction((session, tx) ->
            session.createQuery(delete).executeUpdate()
        );
    }

    public Uni<Integer> deleteAll() {
        CriteriaBuilder cb = this.sessionFactory.getCriteriaBuilder();
        // create delete
        CriteriaDelete<Post> delete = cb.createCriteriaDelete(Post.class);
        // set the root class
        Root<Post> root = delete.from(Post.class);
        // perform update
        return this.sessionFactory.withTransaction((session, tx) ->
            session.createQuery(delete).executeUpdate()
        );
    }

}

```

Till now, we have integrated Hibernate Reactive with Spring IOC container, next we will use the `PostRepositoy` to shake hands with the backend database. Let's begin to build the web layer.

There are two different type asynchronous APIs available in Hibernate Reactive, one is based on Java 8 `CompletionStage`, another is  built on  [Smallrye Munity project](https://smallrye.io/smallrye-mutiny) . The later implements Reactive Streams specification, we use it in this post.

But unfortunately, Spring does not have a built-in Smallrye Mutiny support as RxJava 2/3. 

There are some possible solutions that can be used to overcome this barrier.

*  Convert the SmallRye APIs to Reactor APIs, then use the Reactor APIs directly in `RouterFunction` or `Controller` class.
* Similar to the existing RxJava 1/2/3, JDK 9+ Flow support in Spring WebFlux, we can add Smallry Munity as another alternative of the official Reactor.

Let's explore them one by one. 

Firstly let's try to convert the Munity APIs to Reactor APIs.  Assume we will use  `RouterFunction` to handle the web request. 

Add the following dependency to the project *pom.xml* file.

```xml
<dependency>
    <groupId>io.smallrye.reactive</groupId>
    <artifactId>mutiny-reactor</artifactId>
    <version>${mutiny-reactor.version}</version>
</dependency>
```

The `mutiny-reactor` provides some conversion utilities between SmallRye Mutiny and Reactor APIs.

The following is an example of `PostsHandler`, in which we convert all Mutiny APIs to Reactor APIs.

```java
@Component
@RequiredArgsConstructor
class PostsHandler {

    private final PostRepository posts;

    public Mono<ServerResponse> all(ServerRequest req) {
        return ServerResponse.ok().body(this.posts.findAll().convert().with(toMono()), Post.class);
    }

    public Mono<ServerResponse> create(ServerRequest req) {
        return req.bodyToMono(CreatePostCommand.class)
            .flatMap(post -> this.posts.save(
                        Post.builder()
                            .title(post.getTitle())
                            .content(post.getContent())
                            .build()
                    )
                    .convert().with(toMono())
            )
            .flatMap(p -> ServerResponse.created(URI.create("/posts/" + p.getId())).build());
    }

    public Mono<ServerResponse> get(ServerRequest req) {
        var id = UUID.fromString(req.pathVariable("id"));
        return this.posts.findById(id).convert().with(toMono())
            .flatMap(post -> ServerResponse.ok().body(Mono.just(post), Post.class))
            .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> update(ServerRequest req) {

        var id = UUID.fromString(req.pathVariable("id"));
        return Mono.zip((data) -> {
                    Post p = (Post) data[0];
                    UpdatePostCommand p2 = (UpdatePostCommand) data[1];
                    p.setTitle(p2.getTitle());
                    p.setContent(p2.getContent());
                    return p;
                },
                this.posts.findById(id).convert().with(toMono()),
                req.bodyToMono(UpdatePostCommand.class)
            )
            //.cast(Post.class)
            .flatMap(post -> this.posts.save(post).convert().with(toMono()))
            .flatMap(post -> ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> delete(ServerRequest req) {
        var id = UUID.fromString(req.pathVariable("id"));
        return this.posts.deleteById(id).convert().with(toMono())
            .flatMap(d -> ServerResponse.noContent().build());
    }
}
```

Then assemble the web handlers in a `RouterFunction` bean.

```java
@Bean
public RouterFunction<ServerResponse> routes(PostsHandler handler) {
    return route(GET("/posts"), handler::all)
        .andRoute(POST("/posts"), handler::create)
        .andRoute(GET("/posts/{id}"), handler::get)
        .andRoute(PUT("/posts/{id}"), handler::update)
        .andRoute(DELETE("/posts/{id}"), handler::delete);
}
```

Add a `DataInitializer` bean to initialize some sample data when the application is started.

```java
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final static Logger LOGGER = Logger.getLogger(DataInitializer.class.getName());

    private final Mutiny.SessionFactory sessionFactory;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        LOGGER.info("Data initialization is starting...");

        Post first = Post.of(null, "Hello Spring", "My first post of Spring", null);
        Post second = Post.of(null, "Hello Hibernate Reactive", "My second Hibernate Reactive", null);

        sessionFactory
            .withTransaction(
                (conn, tx) -> conn.createQuery("DELETE FROM Post").executeUpdate()
                    .flatMap(r -> conn.persistAll(first, second))
                    .chain(conn::flush)
                    .flatMap(r -> conn.createQuery("SELECT p from Post p", Post.class).getResultList())
            )
            .subscribe()
            .with(
                data -> LOGGER.log(Level.INFO, "saved data:{0}", data),
                throwable -> LOGGER.warning("Data initialization is failed:" + throwable.getMessage())
            );
    }
}
```

Startup a Postgres database. There is a [*docker-compose.yml*](https://github.com/hantsy/spring-puzzles/blob/master/hibernate-reactive/docker-compose.yml) file available to start a Postgres instance in Docker container.  Then run the application via Spring Boot Maven plugin.

```bash
// start postgres database
docker compose up 

// run the application
mvn clean spring-root:run
```

When the application is running, try to test http://localhost:8080/posts endpoints with `curl` command.

```
# curl http://localhost:8080/posts
[{"id":"0998578e-0553-480b-bbb7-e96fd402455f","title":"Hello Spring","content":"My first post of Spring","createdAt":"2021-08-26T22:37:02.076284"},{"id":"e09ffa71-905f-4241-9449-0860977de666","title":"Hello Hibernate Reactive","content":"My second Hibernate Reactive","createdAt":"2021-08-26T22:37:02.116677"}]

# curl http://localhost:8080/posts/0998578e-0553-480b-bbb7-e96fd402455f
{"id":"0998578e-0553-480b-bbb7-e96fd402455f","title":"Hello Spring","content":"My first post of Spring","createdAt":"2021-08-26T22:37:02.076284"}
```

Then let's discuss the second solution. 

Spring uses a `ReactiveAdapterRegistry` to register all reactive streams implementations, such as RxJava 2/3, JDK 9+ Flow, etc. When using the implementer's specific APIs, it will look up the registry and convert it into the standard ReactiveStreams APIs which can be processed by Spring framework.

We'll create a new adapter to register Mutiny APIs.  

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class MutinyAdapter {
    private final ReactiveAdapterRegistry registry;

    @PostConstruct
    public void registerAdapters(){
        log.debug("registering MutinyAdapter");
        registry.registerReactiveType(
            ReactiveTypeDescriptor.singleOptionalValue(Uni.class, ()-> Uni.createFrom().nothing()),
            uni ->((Uni<?>)uni).convert().toPublisher(),
            publisher ->  Uni.createFrom().publisher(publisher)
        );

        registry.registerReactiveType(
            ReactiveTypeDescriptor.multiValue(Multi.class, ()-> Multi.createFrom().empty()),
            multi -> (Multi<?>) multi,
            publisher-> Multi.createFrom().publisher(publisher));
    }
}

```

Then create a `@RestController` bean which invoke `PostRepository`  directly. As you see,  all methods return a `ResponseEntity` class or a  `Uni<ResponseEntity>`,  no need explicit conversion work there.

```java
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
class PostController {

    private final PostRepository posts;

    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> all() {
        return ok().body(this.posts.findAll());
    }

    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Uni<ResponseEntity<?>> create(@RequestBody CreatePostCommand data) {
        return this.posts.save(
                Post.builder()
                    .title(data.getTitle())
                    .content(data.getContent())
                    .build()
            )
            .map(p -> created(URI.create("/posts/" + p.getId())).build());
    }

    @GetMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Uni<ResponseEntity<Post>> get(@PathVariable UUID id) {
        return this.posts.findById(id)
            .map(post -> ok().body(post));
    }

    @PutMapping(value = "{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Uni<ResponseEntity<?>> update(@PathVariable UUID id, @RequestBody UpdatePostCommand data) {

        return Uni.combine().all()
            .unis(
                this.posts.findById(id),
                Uni.createFrom().item(data)
            )
            .combinedWith((p, d) -> {
                p.setTitle(d.getTitle());
                p.setContent(d.getContent());
                return p;
            })
            .flatMap(this.posts::save)
            .map(post -> noContent().build());
    }

    @DeleteMapping("{id}")
    public Uni<ResponseEntity<?>> delete(@PathVariable UUID id) {
        return this.posts.deleteById(id).map(d -> noContent().build());
    }
}
```

Run this application again, you will get the same result as the former solution.

Get the source codes of this post from my GitHub, they are available in two seperate projects, [hibernate-reactive](https://github.com/hantsy/spring-puzzles/tree/master/hibernate-reactive) and [hibernate-reactive-mutiny](https://github.com/hantsy/spring-puzzles/tree/master/hibernate-reactive-mutiny).

