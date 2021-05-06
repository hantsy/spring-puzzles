package com.example.demo.jpa;

import com.example.demo.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataJpaPostRepository extends JpaRepository<Post, Long> {
}
