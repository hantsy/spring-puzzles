package com.example.demo;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.LocalDateTime;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.*;

@SpringBootApplication
@Slf4j
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    ApplicationRunner runner(PostRepository posts) {
        log.info("start data initialization...");
        return args -> posts.findAll()
                .subscribe(
                        data -> log.info("initial post data: {}", data),
                        err -> log.error("err: {}", err)
                );

    }


    @Bean
    public RouterFunction<ServerResponse> routes(PostHandler postHandler) {
        return route()
                .path("/posts", () -> route()
                        .nest(
                                path(""),
                                () -> route()
                                        .GET("", postHandler::all)
                                        .POST("", postHandler::create)
                                        .build()
                        )
                        .nest(
                                path("{id}"),
                                () -> route()
                                        .GET("", postHandler::get)
                                        .build()
                        )
                        .build()
                )
                .build();
    }
}

@Component
@RequiredArgsConstructor
class PostHandler {

    private final PostRepository posts;

    public Mono<ServerResponse> all(ServerRequest req) {
        return ok().body(this.posts.findAll(), Post.class);
    }

    public Mono<ServerResponse> create(ServerRequest req) {
        return req.bodyToMono(Post.class)
                .flatMap(this.posts::save)
                .flatMap(postId -> created(URI.create("/" + postId)).build());
    }

    public Mono<ServerResponse> get(ServerRequest req) {
        return this.posts.findById(Long.valueOf(req.pathVariable("id")))
                .flatMap(post -> ok().body(Mono.just(post), Post.class))
                .switchIfEmpty(notFound().build());
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
        return this.template.select(Post.class)
                .matching(Query.query(where("title").like("%" + title + "%")))
                .all();
    }

    Mono<Post> findById(Long id) {
        return this.template.selectOne(Query.query(where("id").is(id)), Post.class);
    }

    Mono<Long> save(Post data) {
        return this.template.insert(Post.class)
                .using(data)
                .map(Post::getId);
    }
}


@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "posts")
class Post {

    @Id
    @Column("id")
    private Long id;

    @Column("title")
    private String title;

    @Column("content")
    private String content;

    @Column("created_at")
    private LocalDateTime createdAt;

}

