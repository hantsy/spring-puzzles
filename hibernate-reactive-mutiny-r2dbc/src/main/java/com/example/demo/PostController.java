package com.example.demo;

import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

import static org.springframework.http.ResponseEntity.*;

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
            .with((p, d) -> {
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
