package com.example.demo;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;

@SpringBootApplication
@Slf4j
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    ApplicationRunner runner(PostRepository posts) {
        log.info("start data initialization...");
        return args -> {
            posts.findAll()
                    .subscribe(
                            data -> log.info("initial post data: {}", data),
                            err -> log.error("err: {}", err)
                    );

        };

    }
}

@RequiredArgsConstructor
@RestController
@RequestMapping
class PostController {

    private final PostRepository posts;

    @GetMapping
    public ResponseEntity<Flux<Post>> all() {
        return ok(this.posts.findAll());
    }

    @GetMapping("{id}")
    public Mono<ResponseEntity<Post>> get(@PathVariable("id") Long id) {
        return this.posts.findById(id)
                .map(post -> ok(post))
                .defaultIfEmpty(notFound().build());
    }
}

interface PostRepository extends R2dbcRepository<Post, Long> {
    Flux<PostSummary> findByTitleLike(String title, Pageable pageable);
}

@Value
class PostSummary {
    UUID id;
    String title;
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

