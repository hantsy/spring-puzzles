package com.example.demo;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.test.StepVerifier;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Slf4j
class PostRepositoryTests {

    @Autowired
    PostRepository postRepository;

    @Autowired
    ReactiveMongoTemplate reactiveMongoTemplate;

    @SneakyThrows
    @BeforeEach
    public void setup() {
        var latch = new CountDownLatch(1);
        this.reactiveMongoTemplate.remove(Post.class).all()
                .doOnTerminate(latch::countDown)
                .subscribe(r -> log.debug("delete all posts: " + r), e -> log.debug("error: " + e), () -> log.debug("done"));
        latch.await(5000, TimeUnit.MILLISECONDS);
    }

    @Test
    public void testSavePost() {
        var data = Post.builder().content("my test content").title("my test title").build();
        var saved = this.postRepository.save(data);
        StepVerifier.create(saved)
                .consumeNextWith(p -> assertThat(p.getTitle()).isEqualTo("my test title"))
                .expectComplete()
                .verify();
    }

}


