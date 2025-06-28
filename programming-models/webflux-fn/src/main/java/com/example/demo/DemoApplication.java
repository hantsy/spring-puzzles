package com.example.demo;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.Id;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.query.Update;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.function.BiFunction;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.web.reactive.function.server.RequestPredicates.method;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.*;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}

@Component
@RequiredArgsConstructor
@Slf4j
class SampleRunner implements ApplicationRunner {
    final PostRepository posts;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        posts.findAll()
                .subscribe(
                        data -> log.info("initial post data: {}", data),
                        err -> log.error("err: {}", err)
                );
    }
}


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

@Component
@RequiredArgsConstructor
class PostRepository {

    private final DatabaseClient client;

    public static final BiFunction<Row, RowMetadata, Post> ROW_MAPPER = (row, meta) -> new Post(
            row.get("id", Long.class),
            row.get("title", String.class),
            row.get("content", String.class),
            row.get("created_at", LocalDateTime.class)
    );

    public Flux<Post> findAll() {
        String sql = """
            SELECT id, title, content, created_at
            FROM posts
        """;
        return client.sql(sql)
                .map(ROW_MAPPER)
                .all();
    }

    public Flux<Post> findByTitleLike(String title) {
        String sql = """
            SELECT id, title, content, created_at
            FROM posts
            WHERE title LIKE :title
        """;
        return client.sql(sql)
                .bind("title", "%" + title + "%")
                .map(ROW_MAPPER)
                .all();
    }

    public Mono<Post> findById(Long id) {
        String sql = """
            SELECT id, title, content, created_at
            FROM posts
            WHERE id = :id
        """;
        return client.sql(sql)
                .bind("id", id)
                .map(ROW_MAPPER)
                .one();
    }

    public Mono<Long> create(Post data) {
        String sql = """
            INSERT INTO posts (title, content)
            VALUES (:title, :content)
            RETURNING id
        """;
        return client.sql(sql)
                .bind("title", data.title())
                .bind("content", data.content())
                .map((row, meta) -> row.get("id", Long.class))
                .one();
    }

    public Mono<Long> update(Long id, Post data) {
        String sql = """
            UPDATE posts
            SET title = :title, content = :content
            WHERE id = :id
        """;
        return client.sql(sql)
                .bind("title", data.title())
                .bind("content", data.content())
                .bind("id", id)
                .fetch()
                .rowsUpdated();
    }

    public Mono<Long> deleteById(Long id) {
        String sql = """
            DELETE FROM posts
            WHERE id = :id
        """;
        return client.sql(sql)
                .bind("id", id)
                .fetch()
                .rowsUpdated();
    }
}

@Table(value = "posts")
record Post(
        @Id
        @Column("id")
        Long id,

        @Column("title")
        String title,

        @Column("content")
        String content,

        @Column("created_at")
        LocalDateTime createdAt
) {
}

