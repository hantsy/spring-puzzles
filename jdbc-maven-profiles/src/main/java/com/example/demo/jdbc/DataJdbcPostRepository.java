package com.example.demo.jdbc;

import com.example.demo.Post;
import org.springframework.data.repository.CrudRepository;

public interface DataJdbcPostRepository extends CrudRepository<Post, Long> {
}
