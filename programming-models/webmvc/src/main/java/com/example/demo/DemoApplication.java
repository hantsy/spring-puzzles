package com.example.demo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;

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
        posts.findAll().forEach(p -> log.info("initial post data: {}", p));
    }
}

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


interface PostRepository extends CrudRepository<Post, Long> {
}

@Table("posts")
record Post(
        @Id
        @Column("id")
        Long id,

        @Column("title")
        String title,

        @Column("content")
        String content,

        @Column("created_at")
        LocalDateTime createdAt) {
}