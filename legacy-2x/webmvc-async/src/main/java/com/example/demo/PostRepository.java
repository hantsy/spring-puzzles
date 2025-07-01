package com.example.demo;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Async;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Async
    CompletableFuture<List<Post>> readAllBy();

}
