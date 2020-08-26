package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;

interface DataJpaPostRepository extends JpaRepository<Post, Long> {
}
