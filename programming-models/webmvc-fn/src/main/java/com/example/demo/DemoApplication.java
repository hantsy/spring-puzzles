package com.example.demo;

import jakarta.servlet.ServletException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.springframework.http.HttpMethod.*;
import static org.springframework.web.servlet.function.RequestPredicates.method;
import static org.springframework.web.servlet.function.RequestPredicates.path;
import static org.springframework.web.servlet.function.RouterFunctions.nest;
import static org.springframework.web.servlet.function.RouterFunctions.route;
import static org.springframework.web.servlet.function.ServerResponse.*;

@SpringBootApplication
@Slf4j
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

@Configuration
class WebConfig {

    @Bean
    RouterFunction<ServerResponse> routerFunction(PostHandler postsHandler) {
        var collectionRoutes = route(method(GET), postsHandler::findAll)
                .andRoute(method(POST), postsHandler::create);
        var singleRoutes = route(method(GET), postsHandler::findById)
                .andRoute(method(PUT), postsHandler::update)
                .andRoute(method(DELETE), postsHandler::deleteById);

        return route()
                .path("posts",
                        () -> nest(path("{id}"), singleRoutes)
                                .andNest(path(""), collectionRoutes)
                )
                .build();
    }
}

@Component
@RequiredArgsConstructor
class PostHandler {
    private final PostRepository posts;

    ServerResponse findAll(ServerRequest request) {
        return ok().body(posts.findAll());
    }

    ServerResponse findById(ServerRequest request) {
        var id = Long.parseLong(request.pathVariable("id"));
        return this.posts.findById(id)
                .map(p -> ok().body(p))
                .orElse(notFound().build());
    }

    ServerResponse create(ServerRequest request) throws ServletException, IOException {
        var data = request.body(Post.class);
        var savedId = this.posts.create(data);
        return ServerResponse.created(URI.create("/posts/" + savedId)).build();
    }

    ServerResponse update(ServerRequest request) throws ServletException, IOException {
        var id = Long.parseLong(request.pathVariable("id"));
        var data = request.body(Post.class);
        var updatedCount = this.posts.update(id, data);

        if (updatedCount > 0) {
            return noContent().build();
        } else {
            return notFound().build();
        }
    }

    ServerResponse deleteById(ServerRequest request) {
        var id = Long.parseLong(request.pathVariable("id"));
        var deletedCount = this.posts.deleteById(id);

        if (deletedCount > 0) {
            return noContent().build();
        } else {
            return notFound().build();
        }
    }
}

@Component
@RequiredArgsConstructor
@Slf4j
class PostRepository {
    public static final RowMapper<Post> ROW_MAPPER = (rs, i) -> new Post(
            rs.getLong("id"),
            rs.getString("title"),
            rs.getString("content"),
            rs.getObject("created_at", LocalDateTime.class)
    );
    private final JdbcClient client;

    Stream<Post> findAll() {
        var sql = """
                SELECT * FROM posts
                """;
        return client.sql(sql)
                .query(ROW_MAPPER)
                .stream();
    }

    Optional<Post> findById(Long id) {
        var sql = """
                SELECT * FROM posts where id=:id
                """;
        return client.sql(sql)
                .param("id", id)
                .query(ROW_MAPPER)
                .optional();
    }

    boolean existsById(Long id) {
        var sql = """
                SELECT EXISTS(SELECT 1 FROM posts where id=:id)
                """;
        return (boolean) client.sql(sql)
                .param("id", id)
                .query()
                .singleValue();
    }

    Long create(Post post) {
        var sql = """
                INSERT INTO posts(title, content) VALUES (:title, :content) RETURNING id
                """;

        var keyHolder = new GeneratedKeyHolder();
        var affectedRows = client.sql(sql)
                .params(Map.of("title", post.title(), "content", post.content()))
                .update(keyHolder);

        log.debug("inserting post affected row: {}", affectedRows);
        return keyHolder.getKey().longValue();
    }

    int update(Long id, Post post) {
        var sql = """
                UPDATE posts 
                SET title=:title,
                    content=:content
                WHERE id=:id   
                """;
        int updatedRow = client.sql(sql)
                .params(Map.of("title", post.title(), "content", post.content(), "id", id))
                .update();
        log.debug("updating post affected row: {}", updatedRow);
        return updatedRow;
    }

    int deleteById(Long id) {
        var sql = """
                DELETE FROM posts WHERE id=:id
                """;

        int deletedRow = client.sql(sql)
                .param("id", id)
                .update();
        log.debug("deleting post affected row: {}", deletedRow);
        return deletedRow;
    }
}

record Post(Long id, String title, String content, LocalDateTime createdAt) {
}
