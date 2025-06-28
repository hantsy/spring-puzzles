package com.example.demo;

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
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.LocalDateTime;

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

    private final R2dbcEntityTemplate template;

    Flux<Post> findAll() {
        return this.template.select(Post.class).all();
    }

    Flux<Post> findByTitleLike(String title) {
        return this.template.select(query(where("title").like("%" + title + "%")), Post.class);
    }

    Mono<Post> findById(Long id) {
        return this.template.selectOne(query(where("id").is(id)), Post.class);
    }

    Mono<Long> create(Post data) {
        return this.template.insert(data).map(Post::id);
    }

    Mono<Long> update(Long id, Post data) {
        return this.template
                .update(query(where("id").is(id)),
                        Update.update("title", data.title()).set("content", data.content()),
                        Post.class);

    }

    Mono<Long> deleteById(Long id) {
        return this.template.delete(query(where("id").is(id)), Post.class);
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

