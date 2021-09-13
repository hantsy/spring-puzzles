package com.example.demo;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping(value = "/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostRepository posts;

    @GetMapping
    public CompletableFuture<List<Post>> all() {
        return this.posts.readAllBy();
    }

}
