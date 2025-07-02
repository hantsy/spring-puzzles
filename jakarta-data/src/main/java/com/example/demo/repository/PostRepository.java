package com.example.demo.repository;

import com.example.demo.model.Post;
import com.example.demo.model.Status;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Delete;
import jakarta.data.repository.Find;
import jakarta.data.repository.OrderBy;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@jakarta.data.repository.Repository
public interface PostRepository extends CrudRepository<Post, UUID> {

    @Find
    @OrderBy("createdAt")
    List<Post> byStatus(Status status);

    @Delete
    @Transactional
    long deleteAll();
}
