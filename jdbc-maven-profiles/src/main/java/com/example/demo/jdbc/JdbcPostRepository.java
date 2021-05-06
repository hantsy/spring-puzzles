package com.example.demo.jdbc;

import com.example.demo.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JdbcPostRepository {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public List<Post> findAll() {
        return this.namedParameterJdbcTemplate.query(
                "select * from POSTS",
                Map.of(),
                (rs, rowNum) -> Post.builder()
                        .id(rs.getLong("ID"))
                        .title(rs.getString("TITLE"))
                        .body(rs.getString("BODY"))
                        //.createdAt(rs.getTimestamp("CREATED_AT").toLocalDateTime())
                        // h2, mysql and postgres JDBC drivers have supported Java 8 DateTime.
                        .createdAt(rs.getObject("CREATED_AT", LocalDateTime.class))
                        .build()
        );
    }

    public Post findById(Long id) {
        return this.namedParameterJdbcTemplate.queryForObject(
                "select * from POSTS where id=:id",
                Map.of("id", id),
                (rs, rowNum) -> Post.builder()
                        .id(rs.getLong("ID"))
                        .title(rs.getString("TITLE"))
                        .body(rs.getString("BODY"))
                        //.createdAt(rs.getTimestamp("CREATED_AT").toLocalDateTime())
                        // h2, mysql and postgres JDBC drivers have supported Java 8 DateTime.
                        .createdAt(rs.getObject("CREATED_AT", LocalDateTime.class))
                        .build()
        );
    }
}
