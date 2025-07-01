package com.example.demo;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.init.CompositeDatabasePopulator;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@SpringBootApplication
@Slf4j
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    DataSourceInitializer initializer(DataSource ds) {
        log.info("initializing data source...");
        var initializer = new DataSourceInitializer();
        initializer.setDataSource(ds);

        var populator = new CompositeDatabasePopulator();
        populator.addPopulators(
                new ResourceDatabasePopulator(new ClassPathResource("schema.sql")),
                new ResourceDatabasePopulator(new ClassPathResource("data.sql"))
        );

        initializer.setDatabasePopulator(populator);

        return initializer;
    }


    @Bean
    ApplicationRunner runner(PostRepository posts) {
        return (args) -> posts
                .findAll()
                .forEach(
                        p -> log.info("initial post data: {}", p)
                );
    }


    @Bean
    RouterFunction<ServerResponse> routerFunction(PostRepository posts) {
        return RouterFunctions.route()
                .GET("/", serverRequest -> ServerResponse.ok().body(posts.findAll()))
                .GET("/{id}", serverRequest -> {
                    var id = Long.valueOf(serverRequest.pathVariable("id"));
                    return posts.findById(id)
                            .map(p -> ServerResponse.ok().body(p))
                            .orElse(ServerResponse.notFound().build());
                })
                .build();
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
    private final NamedParameterJdbcTemplate template;

    Stream<Post> findAll() {
        var queryAll = """
                SELECT * FROM posts
                """;
        return this.template.queryForStream(queryAll, Collections.emptyMap(), ROW_MAPPER);
    }

    Optional<Post> findById(Long id) {
        var queryById = """
                SELECT * FROM posts where id=:id
                """;
        Post result=null;
        try{
            result = this.template.queryForObject(queryById, Map.of("id", id), ROW_MAPPER);
        }catch (Exception e) {
            log.error("find by id error: {}", e.getMessage());
        }

        return Optional.ofNullable(result);
    }
}

//https://github.com/FasterXML/jackson-future-ideas/issues/46
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
record Post(
        @JsonProperty("id")
        Long id,
        @JsonProperty("title")
        String title,
        @JsonProperty("content")
        String content,
        @JsonProperty("createdAt")
        LocalDateTime createdAt
) {
}
