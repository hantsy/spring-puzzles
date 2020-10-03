package com.example.demo.r2dbc;

import com.example.demo.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Random;

@RestController
@RequiredArgsConstructor
public class PostCommandController {

    private final DatabaseClient databaseClient;

    @PostMapping
    public Mono<ResponseEntity<Void>> save(@RequestBody Post post) {
        return this.databaseClient.sql("insert into posts(id, title, content) values(:id, :title, :content)")
                .bind("id", new Random().nextLong())
                .bind("title", post.getTitle())
                .bind("content", post.getContent())
                .filter(statement -> statement.returnGeneratedValues("id"))
                .fetch()
                .first()
                .map(stringObjectMap -> stringObjectMap.get("id"))
                .map(id -> ResponseEntity.created(URI.create("/" + id)).build());

    }
}
