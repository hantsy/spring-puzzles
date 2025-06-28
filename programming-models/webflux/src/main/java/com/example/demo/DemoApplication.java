package com.example.demo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.LocalDateTime;

import static org.springframework.http.ResponseEntity.*;

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

interface PostRepository extends R2dbcRepository<Post, Long> {
    Flux<PostSummary> findByTitleLike(String title, Pageable pageable);
}

record PostSummary(Long id, String title) {
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

