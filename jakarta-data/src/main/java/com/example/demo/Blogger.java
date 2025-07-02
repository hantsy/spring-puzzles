package com.example.demo;

import com.example.demo.model.Post;
import com.example.demo.model.PostSummary;
import jakarta.data.repository.Insert;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;

import java.util.List;

@Repository
public interface Blogger {

    @Query("""
            SELECT p.id, p.title FROM Post p 
            WHERE p.status = 'PUBLISHED'
            ORDER BY p.createdAt DESC
            """)
    List<PostSummary> allPublishedPosts();

    @Insert
    Post newPost(Post post);
}
