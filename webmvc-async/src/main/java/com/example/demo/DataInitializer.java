package com.example.demo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

@Component
@Slf4j
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final PostRepository posts;

    @Override
    public void run(ApplicationArguments args) {
        log.info("initializing posts data...");
        this.posts.deleteAll();
        Stream.of("Spring", "Spring WebMvc").forEach(
                title -> this.posts.save(Post.builder().title(title).content("content of " + title).build())
        );
    }
}
