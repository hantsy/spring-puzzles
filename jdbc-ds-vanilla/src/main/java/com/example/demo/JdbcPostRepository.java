package com.example.demo;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JdbcPostRepository {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public List<Post> findAll() {
       return  this.namedParameterJdbcTemplate.query(
                "select * from POSTS",
                Map.of(),
                (rs, rowNum) -> Post.builder()
                        .id(rs.getLong("ID"))
                        .title(rs.getString("TITLE"))
                        .body(rs.getString("BODY"))
                        .createdAt(rs.getTimestamp("CREATED_AT").toLocalDateTime())
                        .build()
        );
    }
}
