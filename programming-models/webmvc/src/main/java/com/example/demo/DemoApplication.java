package com.example.demo;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.datasource.init.CompositeDatabasePopulator;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.time.LocalDateTime;

import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;

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

}

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
class PostController {
    private final PostRepository posts;

    @GetMapping()
    public ResponseEntity getAll() {
        return ok(posts.findAll());
    }

    @GetMapping("{id}")
    public ResponseEntity getById(@PathVariable("id") Long id) {
        return this.posts.findById(id)
                .map(p -> ok(p))
                .orElse(notFound().build());
    }
}


interface PostRepository extends CrudRepository<Post, Long> {
}

@Table("posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class Post {
    @Id
    @Column("id")
    private Long id;

    @Column("title")
    private String title;

    @Column("content")
    private String content;

    @Column("created_at")
    private LocalDateTime createdAt;
}