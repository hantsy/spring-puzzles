package com.example.demo;
import org.springframework.data.repository.CrudRepository;

interface DataJdbcPostRepository extends CrudRepository<Post, Long> {
}
