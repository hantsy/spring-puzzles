package com.example.demo.mybatis;

import com.example.demo.Post;
import org.springframework.data.repository.CrudRepository;

public interface MybatisJdbcPostRepository extends CrudRepository<Post, Long> {
}
