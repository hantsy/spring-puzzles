package com.example.demo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.UUID;

import static io.smallrye.mutiny.converters.uni.UniReactorConverters.toMono;

@Component
@RequiredArgsConstructor
@Slf4j
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
            .doOnError(error -> log.error("Got error: "+ error.getMessage()))
            .flatMap(post -> ServerResponse.ok().body(Mono.just(post), Post.class));
        //.switchIfEmpty(Mono.error(new PostNotFoundException(id)));
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
